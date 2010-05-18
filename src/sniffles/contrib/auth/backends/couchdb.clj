(ns sniffles.contrib.auth.backends.couchdb
  (:require [couchdb.client :as cdb]))

(def db-name-postfix "-sniffles-users")

(defn initialise [options]
  (println "Initialising CouchDB users database...")
  (try 
   (let [uri (or (get options :uri) "http://127.0.0.1:5984")
	 proj (or (get options :name) "unknown")
	 db-name (str proj db-name-postfix)]
     (when (nil? uri) (throw (Exception. "no :uri option set. cannot initialise couchdb")))
     (when (not (some #{db-name} (cdb/database-list uri)))
       (println "creating database: " db-name)
       (cdb/database-create uri db-name))
     (when (not (> (count (cdb/document-list uri db-name)) 0))
       (println "WARNING: no users") ; TODO: if we get here, do something useful!
     ))
   (catch IllegalStateException e ; TODO: this should never be triggered any more, remove
     (println "WARNING: settings not yet initialised. unable to initialise database"))))

(defn get-user [options uid]
  (let [uri (get options :uri "http://127.0.0.1:5984")
	proj (get options :name "unknown")
	db-name (str proj db-name-postfix)]
    (if (empty? uid)
      nil ; an empty string for the uid will return the results of GET /db-name/ so ignore this cause to avoid issues
      (try (let [user (cdb/document-get uri db-name uid)]
	     (assoc (assoc user :id (:_id user)) :authenticated? true))
	   (catch java.io.FileNotFoundException e
	     nil)))))
