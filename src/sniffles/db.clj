(ns sniffles.db
  (:require [clojure.contrib.sql :as sql]
	    [sniffles.db.derby :as db]
;	    [sniffles.fields :as fields]
	    )
  (:use clojure.contrib.str-utils
	[clojure.contrib.java-utils :only (as-str)]))

(defn create-table [project name model]
  (let [table-name (re-gsub #"-" "_" (str-join "." [project name]))]
    (sql/with-connection
      db/specification
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
		 