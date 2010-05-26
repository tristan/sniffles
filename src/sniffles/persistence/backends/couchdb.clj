(ns sniffles.persistence.backends.couchdb
  (:refer-clojure :exclude [get])
  (:require [couchdb.client :as cdb]
	    [clojure.contrib.error-kit :as kit]))

;(def db-name-prefix "sniffles")

(defmacro get-host-and-db [options db]
  `[(or (:db-uri ~options)
	"http://127.0.0.1:5984")
    (str (:db-prefix ~options) "-" ~db)])

(defn get [db id options]
  (let [[host db] (get-host-and-db options db)]
    (try (cdb/document-get host db id)
	 (catch java.io.FileNotFoundException e
	   nil))))

(defn select [db k v options] ; TODO: view-get requires a lot of things to go right concerning _design/ stuff
  (let [[host fdb] (get-host-and-db options db)
	r (cdb/view-get host fdb db k {:key v})]
    (map #(:value %) (:rows r))))

(defn create [db doc options]
  (let [[host db] (get-host-and-db options db)]
    (loop [count 0]
      (kit/with-handler
	(cdb/document-create host db doc)
	(kit/handle cdb/DatabaseNotFound e
		    (if (< count 2)
		      (do
			(cdb/database-create host db)
			(recur (inc count)))
		      (kit/raise* e)))))))

(defn update [db doc options]
  (let [[host db] (get-host-and-db options db)]
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
		    (create db doc options))))))

(defn all [db options]
  (let [[host db] (get-host-and-db options db)]
    (cdb/document-list host db)))