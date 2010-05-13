(ns sniffles.includes)

(defmacro route [& options]
  (let [new-route   
	(conj ; route defaults with
	 {:name ""
	  :path ""
	  :xhr false
	  :request-method "*"
	  :path-info ""
	  :request-params {}
	  :header ""
	  :accept "*/*"
	  }
	 (apply hash-map options))
	new-route (if (contains? new-route :view)
		    (do
		      (require (symbol (namespace (:view new-route))))
		      (assoc new-route :view (find-var (:view new-route))))
		    new-route)]
    `(do
       (println "creating route:" ~(:name new-route) "...")
;       ~(if (contains? new-route :view)
;	  `(require ~(symbol (namespace (:view new-route))))
       (intern *ns* '~'__routes__
	       (if (resolve '~'__routes__) 
		 (conj @(resolve '~'__routes__)
		   ~new-route)
		 [~new-route])))))