(ns sniffles.conf.urls
  (:require [sniffles.core :as core])
  (:use clojure.contrib.str-utils))

; TODO: this is very similar to core/create-app except it doesn't wrap the returned fn
; maybe this should be merged to remove the redundancy
(defn include* [package] 
  ;(core/create-app package)
  (let [urlpatterns (ns-resolve package 'urls)
	urlpatterns (if (nil? urlpatterns) nil (var-get urlpatterns))
	settings (ns-resolve package 'settings)
	settings (if (nil? settings) {} (var-get settings))]
    (if (nil? urlpatterns) 
      (fn [req] nil)
      (fn [req]
	(core/dispatch (assoc req :settings (conj (or (:settings req) {}) settings)) urlpatterns)))))

(defmacro include [package]
  `(include* (find-ns '~package)))