(ns sniffles
  (:use [sniffles.dispatch :only (dispatch)]
	sniffles.includes)
  (:require ring.middleware.session
	    ring.middleware.params)
  )

(defn application [routes config]
  (let [routes (for [r routes] (route r))]
    (-> (fn [request] ; TODO: middleware wrap
	  (dispatch routes request))
	(ring.middleware.session/wrap-session) ; add session stuff by default
	(ring.middleware.params/wrap-params) ; add parameter handling by default
	))) ; TODO: maybe these default things shouldn't be default...
