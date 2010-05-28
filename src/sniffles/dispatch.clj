(ns sniffles.dispatch
  (:use clojure.contrib.str-utils)
  (:require ring.util.response
	    hiccup.core
	    [sniffles.utils.regex :as regex]
	    [sniffles.templates :as templates]))

; TODO: fn's don't have meta info in clojure 1.1.0. so views need to be passed to routes as vars.
; this can be changed once 1.2.0 is stable

(defn handle [route request]
  (let [view (:view route)
	view (cond (var? view)
		   view
		   :else
		   (throw (Exception. "don't yet no how to handle views that aren't vars")))
	options (conj (get route :options {})
		      (if (contains? route :keys)
			(zipmap (:keys route) (rest (re-find (:path route) (:uri request))))
			{}))
	response (@view request options)
	session (get request :session {}) ; TODO: note that this causes the session to stay the same unless specifically specified
	response (if (map? response)
		   (assoc response :session
			  (if (contains? response :session)
			    (apply dissoc (conj session (:session response)) (keys (filter #(nil? (val %)) (:session response))))
			    session))
		   response)
	response (if (and (map? response) (contains? response :status)) ; TODO: if the returned value is a response already
		   response ; just return the response
		   (let [renderer (get route
				       :renderer
				       (fn [rq rs]
					 (ring.util.response/response rs)))] ; TODO: better default renderer
		     (cond (string? renderer)
			   (cond (.endsWith renderer ".pt")
				 {:status 200 
				  :body ((templates/zpt renderer) (conj request response))}
				 (= renderer "hiccup")
				 {:status 200 :body (hiccup.core/html response)}
				 :else
				 {:status 500 :body "unsupported renderer string"})
			   (fn? renderer)
			   (renderer request response)
			   :else
			   {:status 500 :body "unsupported renderer type"})))]
    response))

(defn reverse-route [routes name options]
  (let [route (first (filter #(= (:name %) name) routes))]
    (if route 
      (let [norm (regex/normalise (:path route))]
	(str-join "" (map #(if (vector? %) (get options (nth (:keys route) (first %))) %) norm)))
      nil)))

(defn dispatch [routes request]
  (let [uri (or (:uri request) "/")]
    (print "dispatching:" (str "'" uri "'") "... ")
    (let [match 
	  (first 
	   (filter #(re-find (:path %) uri)
		   routes))]
      (println "got match with name:" (str "'" (:name match) "'"))
      (if (nil? match)
	{:status 404 :body "404 buddy!"}
	(let [res (handle match (assoc request :uri uri))] ; make sure uri gets passed as it's matched
	  (if (= (:status res) 302) ; redirect!
	    (let [r-uri (reverse-route routes (get-in res [:headers "Location"]) (get res :options))]
	      (println "reversing to:" r-uri)
	      (if r-uri
		(assoc-in res [:headers "Location"] r-uri)
		res)) ; TODO: make sure this is url like, and not just a failed lookup
	    res))))))