(ns sniffles
  (:use [sniffles.dispatch :only (dispatch reverse-route)]
	sniffles.includes)
  (:require ring.middleware.session
	    ring.middleware.params
	    sniffles.persistence.middleware
	    [clj-zpt.tales :as tales])
  )

(defn application* [routes config]
  (let [routes (for [r routes] (route r))]
    (tales/register-extension ; register reverse extention for tales
     "reverse"
     (fn tales-reverse [string context]
       (reverse-route routes string context)))
    (-> (fn [request] ; TODO: middleware wrap
	  (dispatch routes request))
	(ring.middleware.params/wrap-params) ; add parameter handling by default
	))) ; TODO: maybe these default things shouldn't be default...


(defmacro application [routes config & middleware]
  `(sniffles.persistence.middleware/wrap-persistence
    (-> (application* ~routes ~config)
	~@(for [x middleware] (cons (first x) (cons config (rest x)))))
    ~config))