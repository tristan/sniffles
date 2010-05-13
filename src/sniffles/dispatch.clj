(ns sniffles.dispatch
  (:require ring.util.response
	    [sniffles.templates :as templates]))

(defn dispatch [routes request]
  (let [uri (or (:uri request) "")
	uri (if (.startsWith uri "/") (.substring uri 1) uri)]
    (println "dispatching:" (str "'" uri "'") "...")
    (let [match 
	  (first 
	   (filter #(= uri (:path %))
		   routes))]
      (if (nil? match)
	{:status 404 :body "404 buddy!"}
	(let [response (@(:view match) request)] ; TODO: perhaps move this into it's own package?
	  (if false ; TODO: if the returned value is a response already
	    response ; just return the response
	    (let [renderer (get (meta (:view match))
				:renderer
				(fn [rq rs]
				  (ring.util.response/response rs)))] ; TODO: better default renderer
	      (cond (string? renderer)
		    (cond (.endsWith renderer ".pt")
			  {:status 200 :body ((templates/zpt renderer) (conj request response))}
			  :else
			  {:status 500 :body "unsupported renderer string"})
		    (fn? renderer)
		    (renderer request response)
		    :else
		    {:status 500 :body "unsupported renderer type"}))))))))