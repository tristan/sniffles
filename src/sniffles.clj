(ns sniffles
  (:use [sniffles.dispatch :only (dispatch)])
  )

(defmacro create-app [configuration]
  `(do
     (require ~configuration)
     (let [routes# (if (ns-resolve ~configuration '~'__routes__)
		     @(ns-resolve ~configuration '~'__routes__)
		     [])] ; TODO: do we need default routes?
       (println "ROUTES:" routes# (type routes#))
       (fn [request#] ; TODO: middleware wrap
	 (dispatch routes# request#)))))