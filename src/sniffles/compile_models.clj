(ns sniffles.compile-models
  (:use sniffles.fields
	clojure.contrib.str-utils))
  ;(:import sniffles.models.Model))

(defmacro defmodel [name & fields]
  `(do
     (gen-class :name ~(symbol (str *ns* "." name))
		 :extends sniffles.models.Model
		 :prefix ~(str name "-")
		 :methods [;[~'toString [] String]
			   [~'objects [] clojure.lang.PersistentVector]]
		 )
     (defn ~(symbol (str name "-toString")) [this#]
       (str ~(str name ) "<" (class this#) ">"
	     ~(if (empty? fields) ""
		  (str " fields: [" 
		       (str-join ", "
				 (for [x fields]
				   (str (first x))))
		  "]"))))
;     (defn #^{:static true} ~(symbol (str name "-objects")) []
;       (sniffles.models.Model/objects ~(symbol (str *ns* "." name))))
     ))