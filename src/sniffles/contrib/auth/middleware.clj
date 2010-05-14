(ns sniffles.contrib.auth.middleware
  )

(defn wrap-auth [app & options]
  (let [options (apply hash-map options)
	backend (cond (= (:backend options) "couchdb")
		      (do
			(use 'sniffles.contrib.auth.backends.couchdb)
			(@(resolve 'initialise) options)
			'sniffles.contrib.auth.backends.couchdb)
		      :else
		      (throw (Exception. "unknown backend: " (:backend options))))
		      ]
    (fn [req]
      (let [req (assoc-in req [:settings :backend] backend)
	    user-id (get-in req [:session :user-id])
	    user-obj (@(ns-resolve backend 'get-user) options user-id)
	    response (app (assoc req :user user-obj))]
	response ; no need to do anything TODO: but maybe there is!
	))))