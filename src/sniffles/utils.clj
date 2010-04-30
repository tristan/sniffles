(ns sniffles.utils
  (:use clojure.contrib.str-utils
	[clojure.contrib.java-utils :only (as-str)]))

(defmacro coerce-symbol [obj]
  `(cond (symbol? ~obj) ~obj
	 (string? ~obj) (symbol ~obj)
	 (keyword? ~obj) (symbol (name ~obj))))

(defmacro coerce-keyword [obj]
  `(cond (keyword? ~obj)  ~obj
	 (or (symbol? ~obj)
	     (string? ~obj)) (keyword ~obj)))


(defmacro get-tablename [model]
  `(symbol 
    (re-gsub #"-" "_" 
	     (str-join "." 
		       (re-split #".models\/"
				 (.substring (str (:var (meta ~model))) 2))))))

(defmacro get-fields [model]
  [])