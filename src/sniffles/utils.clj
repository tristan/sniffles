(ns sniffles.utils
  (:use clojure.contrib.str-utils
	[clojureql.util :only (flatten-map)]
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


(defn queryquote
  [form]
;  (when (symbol? form) (println "looking at:" (namespace form) (name form)))
  (cond
   ;(self-eval? form) form
   ; (unquote? form)   (second form)
    (symbol? form)    ;(let [vn (symbol (str (namespace form) "/" (name form)))
			;    v (find-var (symbol vn))
			 ;   r (if v (var-get v) v)]
			;(cond (and v r (not (fn? r))) (list 'quote r)
			 ;     :else                   (list 'quote form)))
    (if (find-var (symbol (str "clojure.core/" (name form))))
      (list 'quote form)
      form)
    (keyword? form)   (list 'quote (coerce-symbol form))
    (vector? form)    (vec (map queryquote form))
    (map? form)       (apply hash-map (map queryquote (flatten-map form)))
    (set? form)       (apply hash-set (map queryquote form))
    (seq? form)       (list* `list (map queryquote form))
    :else             (list 'quote form)))
