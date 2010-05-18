(ns sniffles.persistence.middleware)

(defn wrap-persistence [app config & options]
  (let [backend (cond (= (:backend config) "couchdb")
		      (do
			(require 'sniffles.persistence.backends.couchdb)
			'sniffles.persistence.backends.couchdb)
		      :else
		      (throw (Exception. "unknown backend: " (:backend config))))]
    (fn [req]
      (let [req (assoc-in req [:settings :persistence] (assoc config :package backend))]
	(app req)))))