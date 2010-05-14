(ns sniffles.dispatch
  (:require ring.util.response
	    hiccup.core
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
	session (if (contains? route :persist) ; check persist options to see if any keys should be added/updated or removed from the session
		  (let [updates (select-keys response (remove #(= :test (get response % :test)) (:persist route)))]
		    (apply dissoc (conj session updates) (keys (filter #(nil? (val %)) updates))))
		  session)
	response (if false ; TODO: if the returned value is a response already
		   response ; just return the response
		   (let [renderer (get route
				       :renderer
				       (fn [rq rs]
					 (ring.util.response/response rs)))] ; TODO: better default renderer
		     (cond (string? renderer)
			   (cond (.endsWith renderer ".pt")
				 {:status 200 
				  :body ((templates/zpt renderer) (assoc request :content response))}
				 (= renderer "hiccup")
				 {:status 200 :body (hiccup.core/html response)}
				 :else
				 {:status 500 :body "unsupported renderer string"})
			   (fn? renderer)
			   (renderer request response)
			   :else
			   {:status 500 :body "unsupported renderer type"})))]
    (println "PERSISTING:" request options session)
    (assoc response :session (conj (get response :session {}) session))))

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
	(handle match (assoc request :uri uri)))))) ; make sure uri gets passed as it's matched
	