(ns sniffles.conf.urls
  (:refer-clojure :exclude [reverse])
  (:require [sniffles.utils.regex :as regex]
	    [sniffles.active-project :as proj])
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
       ;{:dispatch 
      (fn [req] nil) 
      ;:get-view-named (fn [req] nil)}
       ;{:dispatch
	(fn [req]
;	  (core/dispatch (assoc req :settings (conj (or (:settings req) {}) settings)) urlpatterns)
	  )
;	:get-view-named
;	(fn [name]
;	  (core/get-view-named name urlpatterns))}
	)))

(defmacro include [package]  
  `(var ~package))

(defn- get-named-url-mapping [prefix name urlpatterns]
  (if (empty? urlpatterns)
    nil
    (let [f (first urlpatterns)]
      (cond (= f name)
	    [prefix (second urlpatterns)]
	    (and (vector? f) (var? (second f)))
	    (let [r (get-named-url-mapping (conj prefix (first f)) name (var-get (second f)))]
	      (if r
		r
		(recur prefix name (rest urlpatterns))))
	    :else
	    (recur prefix name (rest urlpatterns))))))

(defn reverse 
  ([name] (reverse nil))
  ([name options] (reverse name options proj/urls))
  ([name options urls]
    (let [[prefix mapping] (get-named-url-mapping ["/"] name urls)
	  match-keys (second (rest mapping))]
      (if mapping
	(if (= (count (keys options)) (count match-keys)) ; make sure the options we were given matches the expected inputs for the mapping
	  (let [normal (regex/normalise (first mapping)) ; for prefix, conviniently assume that there are no parameters included: TODO fix this
		prefix (str-join "" (map #(if (instance? java.util.regex.Pattern %) (str-join "" (regex/normalise %)) %) prefix))]
	    (str prefix (str-join "" (map #(if (vector? %) (get options (nth match-keys (first %))) %) normal)))))))))

