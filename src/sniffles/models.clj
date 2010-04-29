(ns sniffles.models
  (:use sniffles.fields
	clojure.contrib.str-utils)
  (:require [sniffles.db :as db])
)

(defmacro third [coll]
  `(first (rest (rest ~coll))))

(defmacro defmodel [name & fields]
  (let [primary-keys ; find if there are any :primary-key fields specified
	(filter #(true? (:primary-key (third %)))
		fields)]
    (if ; if there are no primary keys, make sure the user isn't trying to make an :id field
	(and (empty? primary-keys)
	     (not (empty? (filter #(= (.toLowerCase (str (first %))) "id") fields))))
      (throw (Exception. (str name ": \"id\": You can't use \"id\" as a field name, because each model automatically gets an \"id\" field if none of the fields have {:primary_key true}. You need to either remove/rename your \"id\" field or add {:primary_key true} to a field.")))
      `(def ~name
	    (with-meta
	      ~(reduce #(assoc %1 (keyword (first %2))
			       (create-field (second %2)
					     (third %2)))
		       (if (empty? primary-keys)
			 {:id (create-field :auto-field {:primary-key true})}
			 {})
		       fields)
	      {:is-model? true
	       ;:name '~name
	       :var #'~name
	       :primary-key ~(if (empty? primary-keys) 
			       [:id] 
			       (vec (map #(keyword (first %)) primary-keys)))
	       })))))

(defmacro get-primary-key [obj]
  `(map (fn [x#] (get ~obj x#)) (:primary-key (meta (var-get (:model (meta ~obj)))))))

(defn create-object [typ & constructer]
  (let [inputs (cond (and (= (count constructer) 1)
			  (map? (first constructer)))
		     (first constructer)
		     :else ; making room for future expansion.
		     (throw (UnsupportedOperationException.)))]
    (let [obj
	  (reduce #(assoc %1 (key %2)
			  (if (contains? inputs (key %2))
			    (let [value (field-cast (val %2) ((key %2) inputs))]
			      (if (field-valid? (val %2) value)
				value
				(throw (Exception. "invalid field input"))))
			    (field-default (val %2))))
		  {} (seq typ))]
      (with-meta obj
	{:model (:var (meta typ))
	 :id nil;(vec (map #(get obj %) (:primary-key (meta typ))))
       }))))

(defn save-object [obj]
  (if (or (nil? (meta obj)) (not (contains? (meta obj) :model)))
    (throw (Exception. 
	    "unable to distinguish type of this object. make sure the meta of the object is retained"))
    (let [typ (:model (meta obj))
	  update? (:exists? (meta obj))]
      ;(println typ update?)
      (if update?
	(do (db/update-values typ obj)
	    (with-meta obj
	      (conj (meta obj)
		    {:id (get-primary-key obj)}
		    )))
	(let [r (db/insert-values typ obj)
	      obj (conj obj r)]  ; add auto generated attributes to the obj
	  (with-meta
	    obj
	    (conj (meta obj)
		  {:exists? true
		   :id (get-primary-key obj)})
	))))))

     