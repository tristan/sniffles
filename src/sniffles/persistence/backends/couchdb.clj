(ns sniffles.persistence.backends.couchdb
  (:refer-clojure :exclude [get])
  (:require [sniffles.persistence :as p]
	    [couchdb.client :as cdb]
	    [clojure.contrib.error-kit :as kit]))

;(def db-name-prefix "sniffles")

(defmacro get-host-and-db [options db]
  `[(or (:couchdb-uri ~options)
	"http://127.0.0.1:5984")
    (str (:couchdb-prefix ~options) "-" ~db)])

(defn get 
  ([db id] (get db id p/settings))
  ([db id options]
  (let [[host db] (get-host-and-db options db)]
    (try (cdb/document-get host db id)
	 (catch java.io.FileNotFoundException e
	   nil)))))

(defn select ; TODO: view-get requires a lot of things to go right concerning _design/ stuff
  [db & options]
  (let [[host fdb] (get-host-and-db p/settings db)]
    (if (empty? options) ; this is like a "select * from tablename;"
      (map #(cdb/document-get host fdb %) (remove #(.startsWith % "_design/") (cdb/document-list host fdb))) ; TODO: exception handling
      (let [k (first options) v (second options)] ; TODO: implement better views
	(map #(:value %) ((cdb/view-get host fdb db k {:key v}) :rows))))))

(defn create 
  ([db doc] (create db doc p/settings))
  ([db doc options]
  (let [[host db] (get-host-and-db options db)]
    (loop [count 0]
      (kit/with-handler
	(cdb/document-create host db doc)
	(kit/handle cdb/DatabaseNotFound e
		    (if (< count 2)
		      (do
			(cdb/database-create host db) ; silently create db TODO: make this an option incase this is not wanted behaviour
			(recur (inc count)))
		      (kit/raise* e))))))))

(defn update [db doc]
  (let [[host db] (get-host-and-db p/settings db)]
    (loop [count 0]
      (kit/with-handler
	(cdb/document-update host db (:_id doc) doc)
	(kit/handle cdb/DatabaseNotFound e
		    (if (< count 2)
		      (do
			(cdb/database-create host db)
			(recur (inc count)))
		      (kit/raise* e)))
	(kit/handle cdb/DocumentNotFound e ; if document isn't found, silently try create it instead
		    (create db doc))))))

(defn all [db options]
  (let [[host db] (get-host-and-db options db)]
    (cdb/document-list host db)))