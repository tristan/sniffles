(ns sniffles.db
  (:require [clojure.contrib.sql :as sql]
	    [sniffles.db.derby :as db]
;	    [sniffles.fields :as fields]
	    [sniffles.db.utils :as utils]
	    [clojureql :as cql]
	    [clojureql.backend.derby]
	    )
  (:use clojure.contrib.str-utils
	[clojure.contrib.java-utils :only (as-str)]))

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
(defn create-table [project name model]
  (let [table-name (re-gsub #"-" "_" (str-join "." [project name]))]
    (loop [fields model
	   columns [] 
	   primary-key [] 
	   non-nulls [] 
	   uniques [] 
	   auto-inc [] 
	   defaults []]
      (if (empty? fields)
	(do
	  (println "creating table:" table-name)
	  (println columns primary-key auto-inc non-nulls defaults uniques)
	  (cql/run (get-connection-info project)
		   (cql/create-table* ; NOTE: required to use * version since macro version doesn't allow using symbols
		    table-name
		    columns
		    :primary-key primary-key
		    :auto-inc auto-inc
		    :non-nulls non-nulls
		    :defaults defaults
		    :uniques uniques)))
	(let [[name field] (first fields)
	      name (as-str name)
	      [typ params] (:db (meta field))]
	  ;(println "building field:" name "typ:" typ "params:" params)
	  (recur
	   (rest fields)
	   (conj columns name (cond (or (= typ :serial) (= typ :int))
				    "int" 
				    (= typ :varchar)
				    (str "varchar(" (get field params) ")")
				    :else
				    (throw (UnsupportedOperationException. (as-str typ " field not supported")))))
	   (if (true? (:primary-key field)) (conj primary-key name) primary-key)
	   (if (false? (:null field)) (conj non-nulls name) non-nulls)
	   (if (true? (:unique field)) (conj uniques name) uniques)
	   (if (= typ :serial) (conj auto-inc name) auto-inc)
	   (if (nil? (:default field)) defaults (conj defaults name (if (string? (:default field))
								      (str "'" (:default field) "'")
								      (:default field))))))))))

(comment ; contrib.sql
(defn create-table [project name model]
  (let [table-name (re-gsub #"-" "_" (str-join "." [project name]))]
    (sql/with-connection
      (get-connection-info project)
      (if (sql/with-query-results res [(db/table-exists? table-name)] (empty? res))
	(do
	  (println "creating table:" table-name)
	  (apply sql/create-table
		 table-name
		 (for [field model]
		   (let [db-type (:db (meta (val field)))]
		     (apply vector
			    (re-gsub #"-" "_" (as-str (key field)))
			    (cond (= (first db-type) :varchar) ; expect [:varchar length-key]
				  (db/varchar ((second db-type) (val field)))
				  (= (first db-type) :int) ; [:int]
				  (db/integer)
				  (= (first db-type) :serial)
				  (db/serial)
				  :else
				  (throw (UnsupportedOperationException.
					  (str "unknown database type " (first db-type) 
					       " <" field ">"))))
			    (map #(cond (and (= (key %) :primary-key)
					     (true? (val %)))
					(db/primary-key)
					(and (= (key %) :null)
					     (false? (val %)))
					(db/not-null)
					(and (= (key %) :unique)
					     (true? (val %)))
					(db/unique)
					:else
					nil)
				 (val field)))))))
	(println "table" table-name "already exists")))))
)
(defn select []
    (sql/with-connection
      (get-connection-info 'test-project)
      (sql/with-query-results res ["select * from test_project.Poll"] (println res))))

(comment ; contrib.sql
(defn insert-values [model-var instance]
  (let [project (re-gsub #"\.models" "" (as-str (:ns (meta model-var))))
	table-name (re-gsub #"-" "_" (str-join "." [project (as-str (:name (meta model-var)))]))
	signature (var-get model-var)
	fields (for [field (remove #(= (first (:db (meta (val %)))) :serial) signature)]
		 [(re-gsub #"-" "_" (as-str (key field))) 
		  (get instance (key field))]
		    )]
    (println fields)
    (sql/with-connection
      (get-connection-info project)
      (sql/insert-values table-name
			 (vec (map first fields))
			 (vec (map second fields)))))
  )
) ; end comment

(defn insert-values [model-var instance]
  (let [project (re-gsub #"\.models" "" (as-str (:ns (meta model-var))))
	table-name (re-gsub #"-" "_" (str-join "." [project (as-str (:name (meta model-var)))]))
	signature (var-get model-var)
	fields (vec (apply concat (for [field (remove #(= (first (:db (meta (val %)))) :serial) signature)]
				    [(re-gsub #"-" "_" (as-str (key field)))
				     (get instance (key field))]
				    )))
	auto-fields (filter #(= (first (:db (meta (val %)))) :serial) signature)]
    ;(println fields)
    (cql/run (get-connection-info project)
	     rows
	     (cql/insert-into* table-name
			       fields)
	     (cond (and (not (nil? (:generated-keys rows))) (not (empty? auto-fields)))
		   (apply hash-map
			  (seq (interleave (map key (filter #(= (first (:db (meta (val %)))) :serial) signature))
					   (map #(val %) (first (:generated-keys rows))))))
		   :else
		   nil))
    ))

(defn update-values [model-var instance]
  (let [project (re-gsub #"\.models" "" (as-str (:ns (meta model-var))))
	table-name (re-gsub #"-" "_" (str-join "." [project (as-str (:name (meta model-var)))]))
	signature (var-get model-var)
	fields (vec (apply concat (for [field (remove #(= (first (:db (meta (val %)))) :serial) signature)]
				    [(re-gsub #"-" "_" (as-str (key field)))
				     (get instance (key field))]
				    )))
	old-key-vals (map #(list '= (re-gsub #"-" "_" (as-str %1)) %2) (:primary-key (meta signature)) (:id (meta instance)))
	]
    (println (vec (take-nth 2 fields)))
    (println (vec (take-nth 2 (rest fields))))
    (println old-key-vals)
    (println (cql/build-env `(~'and ~@(map #(list '= (symbol (re-gsub #"-" "_" (as-str %1))) %2) (:primary-key (meta signature)) (:id (meta instance)))) 
			    (vec (take-nth 2 (rest fields)))))
    (println (meta instance))
    (println `(~'and ~@(map #(list '= (re-gsub #"-" "_" (as-str %1)) %2) (:primary-key (meta signature)) (:id (meta instance)))))
    ;(cql/run (get-connection-info project)
    (println (cql/compile-sql
	      (cql/update* table-name
			  fields
			  `(~'and ~@(map #(list '= (symbol (re-gsub #"-" "_" (as-str %1))) %2) (:primary-key (meta signature)) (:id (meta instance))))
			  )
	      (get-connection-info project)))
  ))