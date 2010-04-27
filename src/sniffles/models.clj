(ns sniffles.models
  (:use sniffles.fields
	clojure.contrib.str-utils)
;  (:require sniffles.compile-models
;	    sniffles.run-models)
)

(comment
(cond (true? *compile-files*)
      (do
	;(println "compiling")
	(defmacro defmodel [name & fields]
	  `(sniffles.compile-models/defmodel ~name ~@fields)))
      :else
      (do
	;(println "running")
	(defmacro defmodel [name & fields]
	  `(sniffles.run-models/defmodel ~name ~@fields))))
)

(defmacro defmodel [name & fields]
  `(def ~name
	~(reduce #(assoc %1 (keyword (first %2))
			 (create-field (second %2)
				       (last %2)))
		 {} fields)))

(defn create-object [type & constructer]
  (let [inputs (cond (and (= (count constructer) 1)
			  (map? (first constructer)))
		     (first constructer)
		     :else ; making room for future expansion.
		     (throw (UnsupportedOperationException.)))]
    (reduce #(assoc %1 (key %2)
		    (if (contains? inputs (key %2))
		      (let [value (field-cast (val %2) ((key %2) inputs))]
			(if (field-valid? (val %2) value)
			  value
			  (throw (Exception. "invalid field input"))))
		      (field-default (val %2))))
	    {} (seq type))))