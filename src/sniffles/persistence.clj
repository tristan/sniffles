(ns sniffles.persistence)

(defmacro get-backend [req]
  `(get-in ~req [:settings :persistence]))

(defn get-backend-package [name]
  (cond (= name "couchdb")
	'sniffles.persistence.backends.couchdb))

(defmacro def-basic-function [tablename prefix]
  (let [prefix (if (nil? prefix) "" prefix)
	prefix (if (or (= prefix "") (.startsWith prefix "-")) prefix (str "-" prefix))]
    `(do
       (defn ~(symbol (str "get" prefix)) [id# req#]
	 (let [backend# (get-backend req#)]
	   (@(ns-resolve (:package backend#) '~'get) ~tablename id# backend#)))
       (defn ~(symbol (str "create" prefix)) [doc# req#]
	 (let [backend# (get-backend req#)]
	   (@(ns-resolve (:package backend#) '~'create) ~tablename doc# backend#)))
       (defn ~(symbol (str "update" prefix)) [doc# req#]
	 (let [backend# (get-backend req#)]
	   (@(ns-resolve (:package backend#) '~'update) ~tablename doc# backend#)))
       (defn ~(symbol (str "all-" tablename)) [req#]
	 (let [backend# (get-backend req#)]
	   (@(ns-resolve (:package backend#) '~'all) ~tablename backend#)))
     )))