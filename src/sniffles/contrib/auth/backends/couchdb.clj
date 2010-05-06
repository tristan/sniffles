(ns sniffles.contrib.auth.backends.couchdb
  (:use [sniffles.active-project :only (settings)])
  (:require [couchdb.client :as cdb]))

(def db-name-postfix "-sniffles-users")

(defn initialise []
  (println "Initialising CouchDB users database...")
  (try 
   (let [uri (get settings :couchdb-uri)
	 proj (get settings :project-name)
	 db-name (str proj db-name-postfix)]
     (when (nil? uri) (throw (Exception. "no :couchdb-uri setting set. cannot initialise")))
     (when (not (some #{db-name} (cdb/database-list uri)))
       (println "creating database: " db-name)
       (cdb/database-create uri db-name))
     (when (not (> (count (cdb/document-list uri db-name)) 0))
       (println "WARNING: no users") ; TODO: if we get here, do something useful!
     ))
   (catch IllegalStateException e
     (println "WARNING: settings not yet initialised. unable to initialise database"))))

(defn get-user [uid]
  (let [uri (get settings :couchdb-uri)
	proj (get settings :project-name)
	db-name (str proj db-name-postfix)]
    (try (cdb/document-get uri db-name uid)
	 (catch java.io.FileNotFoundException e
	   nil))))
