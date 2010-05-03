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

(declare queryquote)

(defn processquery [form]
  (cond (= (first form) 'contains?)
	(list `list (list 'quote "like") (queryquote (second form)) (list 'str "%" (last form) "%"))
	:else
	(apply list `list (list 'quote (first form)) (map queryquote (rest form)))))


(defn queryquote
  [form]
;  (when (symbol? form) (println "looking at:" (namespace form) (name form)))
  (cond
   ;(self-eval? form) form
   ; (unquote? form)   (second form)
    (symbol? form)    form
    (keyword? form)   (list 'quote (coerce-symbol form))
    ;(vector? form)    (vec (map queryquote form))
    ;(map? form)       (apply hash-map (map queryquote (flatten-map form)))
    ;(set? form)       (apply hash-set (map queryquote form))
    (seq? form)       (processquery form);(list* `list (map queryquote form))
    :else             (list 'quote form)))
