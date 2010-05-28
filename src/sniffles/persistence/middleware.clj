(ns sniffles.persistence.middleware
  (:require sniffles.persistence))

(defn wrap-persistence [app config & options]
  (println "wrapping persistence!")
  (sniffles.persistence/set-backend
   (cond (= (:persistence-backend config) "couchdb")
	 (do
	   (println "loading backend")
	   (require 'sniffles.persistence.backends.couchdb)
	   'sniffles.persistence.backends.couchdb)
	 :else
	 (throw (Exception. (str "unknown backend: " (:persistence-backend config))))))
  (sniffles.persistence/set-settings config)
  app) ; no need to do anything else