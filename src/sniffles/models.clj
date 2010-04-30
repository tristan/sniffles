(ns sniffles.models
  (:use sniffles.fields
	clojure.contrib.str-utils)
  (:require [sniffles.db :as db]
	    [sniffles.utils :as utils])
)

(defmacro third [coll]
  `(first (rest (rest ~coll))))

(defmacro defmodel [name & fields]
  (let [name (utils/coerce-symbol name)  ; make sure name is a symbol!
	[fields options] (split-with list? fields) ; split the fields and options
	options (apply hash-map options) ; coerce options into a map
	[fields options] 
	(if (contains? options :primary-key) ; check if primary-key is specified
	  [fields ; if so make sure the keys are keywords
	   (assoc options :primary-key (vec (map #(utils/coerce-keyword %) (:primary-key options))))]
	  [(cons '(:id :auto-field) fields) ; if not add an id field
	   (assoc options :primary-key [:id])
	   ])
	]
    `(def ~name
	  (with-meta
	    ~(apply hash-map
		    (interleave (map #(utils/coerce-keyword (first %)) fields)
				(map #(deffield (rest %)) fields)))
	    (assoc ~options
	      :var #'~name
	      :project (symbol (re-find #"^.*(?=\.)" (str (ns-name *ns*)))))
	    ))))

(defn create [model & key-value-pairs]
  (with-meta
    (conj ; get default values and overwrite them with inputs
     (apply assoc model (interleave (map key model) (map #(get (second (val %)) :default) model))) 
     (cond (and (= (count key-value-pairs) 1) (map? (first key-value-pairs))) ; if the first entry is a map
	   (first key-value-pairs) ; assume the values have been input as a map
	   (and (even? (count key-value-pairs)) (every? true? (map keyword? (take-nth 2 key-value-pairs))))
	   (apply hash-map key-value-pairs) ; assume values have been input as list of key val pairs
	   )
     )
    {:model (:var (meta model))}))

(defmacro get-pk [obj]
  `(vec (map (fn [x#] (get ~obj x#)) (:primary-key (meta (var-get (:model (meta ~obj))))))))

(defn save [obj]
  (let [r (if (contains? (meta obj) :id)
	    (db/update-values (var-get (:model (meta obj))) obj)
	    (db/insert-values (var-get (:model (meta obj))) obj))
	obj (if (map? r) ; i.e. we have generated keys
	      (conj obj r)
	      obj)]
    (with-meta
      obj
      (assoc (meta obj) :id (get-pk obj)))))
