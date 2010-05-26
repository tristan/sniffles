(ns sniffles
  (:use [sniffles.dispatch :only (dispatch reverse-route)]
	sniffles.includes
	clojure.contrib.str-utils)
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
       (let [s (re-split #" " string)
	     name (first s)
	     opts (apply hash-map (rest s))
	     ks (map keyword (keys opts))
	     vs (map #(tales/evaluate % context) (vals opts))
	     opts (zipmap ks vs)]
       (reverse-route routes name opts))))
    (-> (fn [request] ; TODO: middleware wrap
	  (dispatch routes request))
	(ring.middleware.params/wrap-params) ; add parameter handling by default
	))) ; TODO: maybe these default things shouldn't be default...


(defmacro application [routes config & middleware]
  `(sniffles.persistence.middleware/wrap-persistence
    (-> (application* ~routes ~config)
	~@(for [x middleware] (cons (first x) (cons config (rest x)))))
    ~config))