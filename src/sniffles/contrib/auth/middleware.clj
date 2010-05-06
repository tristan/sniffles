(ns sniffles.contrib.auth.middleware
  (:use sniffles.contrib.auth)
  )

(defn wrap-auth [app]
  (initialise) ; initialise the backend
  (fn [req]
    (let [user-id (get-in req [:session :user-id])
	  user-obj (get-user user-id)
	  response (app (assoc req :user user-obj))]
	response ; no need to do anything
	)))