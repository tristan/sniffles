(ns sniffles.contrib.auth.middleware
  (:require [sniffles.contrib.auth :as auth]))

(defn wrap-auth [app config & options]
  (fn [req]
    (let [user-id (get-in req [:session :user-id])
	  user-obj (if (nil? user-id)
		     auth/anonymous-user
		     (or 
		      (let [u (auth/get-user-by-id req user-id)]
			(if u (assoc u :authenticated? true) nil))
		      auth/anonymous-user))]
      (app (assoc req :user user-obj)))))