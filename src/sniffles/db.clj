(ns sniffles.db
  (:require [clojure.contrib.sql :as sql]
	    [sniffles.db.derby :as db]
	    [sniffles.fields :as fields]
	    [sniffles.utils :as utils]
	    [clojureql :as cql]
	    [clojureql.backend.derby]
	    )
  (:use clojure.contrib.str-utils
	[clojure.contrib.java-utils :only (as-str)]))

(load "/cql_patch")

(def conn-info-cache (ref nil))

(defn get-connection-info* [project]
  (let [package (symbol (as-str project ".settings"))
	conn-info (symbol (as-str project ".settings/database"))]
    (require package)
    (let [db-spec (var-get (find-var conn-info))]
	  (cond (= (:engine db-spec) "derby")
	    ;{:classname "org.apache.derby.jdbc.EmbeddedDriver"
	    ; :subprotocol "derby"
	    ; :subname (:name db-spec)
	    ; :create true}
		(do
		  (clojureql.backend.derby/load-embedded-driver)
		  (cql/make-connection-info "derby"
					    (str (:name db-spec) ";create=true")
					    (or (:user db-spec) "")
					    (or (:password db-spec) "")))
		:else
		(throw (UnsupportedOperationException. "unsupported database type"))))))

(defn get-connection-info [project]
  (or @conn-info-cache
      (let [conn-info (get-connection-info* project)]
       (dosync (ref-set conn-info-cache conn-info))
       conn-info)))

; cql
(defn create-table [model]
  (let [tablename (utils/get-tablename model)]
    (println "creating table:" tablename)
    (cql/create-table
     ~tablename
     ~(vec (mapcat list 
		   (map #(utils/coerce-symbol %) (keys model))
		   (map #(let [f (fields/db-map %)]
			   (if (= f 'serial) 'int f)) (vals model))))
     :non-nulls ~(vec (map #(utils/coerce-symbol (key %)) 
			   (filter #(false? (:null (second (val %)))) model)))
     ;:unique (vec (map #(utils/coerce-symbol (key %)) 
     ;		       (filter #(:unique (second (val %))) model)))
     ; unique broken in cql at the moment.
     :auto-inc ~(vec (map #(utils/coerce-symbol (key %)) 
			 (filter #(= 'serial (fields/db-map (val %))) model)))
     :primary-key ~(vec (map #(utils/coerce-symbol %) (:primary-key (meta model))))
     ; TODO: checks
     )
))

(defmacro build-fields [model instance]
  `(vec (apply concat
	      (map (fn [x#] (vector (utils/coerce-symbol x#) (get ~instance x#)))
		   (keys (remove (fn [x#] (= (fields/db-map (val x#)) '~'serial))
				 ~model))))))

; if instance meta has key :id, this is an update, otherwise insert
(defn insert-values [model instance]
  (let [tablename (utils/get-tablename model)
	fields (build-fields model instance)]
    (cql/run (get-connection-info (:project (meta model)))
	     rows
	     (cql/insert-into ~tablename ~fields)
	     (if (not (nil? (:generated-keys rows)))
	       (do ; get the generated keys, because this is a single insert and it doesn't make
		   ; sense to have more than one generated key per table, this isn't too complicated
		 (hash-map (key (first (filter #(= (fields/db-map (val %)) 'serial) model)))
			   (int (val (first (first (:generated-keys rows)))))))
	       rows))
    ))

(defn update-values [model instance]
  (let [tablename (utils/get-tablename model)
	fields (build-fields model instance)]
    (cql/run (get-connection-info (:project (meta model))) rows
	     (cql/update ~tablename ~fields
			 ~(cons 'and
				(map #(list '= (utils/coerce-symbol %1) %2)
				     (:primary-key (meta model))
				     (:id (meta instance)))))
	     nil))) ; return nil so it doesn't get mistaken for an insert by models/save

(defn select [model filter]
  (let [tablename (utils/get-tablename model)]
    (println "selecting from:" tablename)
    (cql/run (get-connection-info (:project (meta model))) res
;    (println ;(cql/compile-sql
	     (cql/query ~tablename * ~filter)
	     (apply list res)))
	      ;(get-connection-info (:project (meta model))))))
)