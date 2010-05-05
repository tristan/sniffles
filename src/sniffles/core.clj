(ns sniffles.core
  (:use clojure.contrib.str-utils))

(defn serve-error [errornr request]
  {:status errornr
   :body (str "<html><body><p>" errornr ": " (:uri request) "</p></body></html>")})

(defn dispatch [request urlpatterns]
  (println "dispatching:" request (type (:uri request)))
  ;(try
  (let [url (get request :remaining-uri (get request :uri))]
    (loop [urlpatterns urlpatterns]
      (println "... trying:" (first urlpatterns))
      (if (empty? urlpatterns)
	(serve-error 404 request)
	(let [urlpattern (first urlpatterns)
	      match (re-find (first urlpattern) url)]
	  (if match
	    (let [adds (if (vector match)
			 (apply hash-map (interleave (first (rest (rest urlpattern))) (rest match)))
			 {})
		  adds (assoc adds :remaining-uri (re-gsub (first urlpattern) "" url))]
	      (println "... found:" match)
	      ((second urlpattern) (conj request adds)))
	    (recur (rest urlpatterns))))))))

(defn create-app [#^clojure.lang.Namespace project]
  (println "creating app:" project)
  ;(if (instance? clojure.lang.Namespace package)
  (let [urls (if (contains? (ns-map project) 'urls)
	       (var-get (get (ns-map project) 'urls))
	       (throw (Exception. (str (name project) " is missing a urls definition"))))
	settings (if (contains? (ns-map project) 'settings)
		   (var-get (get (ns-map project) 'settings))
		   (throw (Exception. (str (name project) " is missing a settings definition"))))
	]
    (fn [req] ; TODO: prehaps resolve interesting things from the settings rather than just parsing the whole map
      (dispatch (assoc req :settings (conj (or (:settings req) {}) settings)) urls)
    )
  ))