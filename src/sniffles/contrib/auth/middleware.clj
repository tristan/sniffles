(ns sniffles.contrib.auth.middleware
  (:require [sniffles.contrib.auth :as auth]
	    [sniffles.persistence :as db])
  )

(defn wrap-auth [app config & options]
  (println config)
  (ring.middleware.session/wrap-session ; add session stuff ; TODO: maybe the user should be responsible for this
   (fn [req]
     (let [user-id (get-in req [:session :user-id])
	   user-obj (if (nil? user-id)
		      auth/anonymous-user
		      (or 
		       (let [u (auth/get-user-by-id req user-id)]
			 (if u (assoc u :authenticated? true) nil))
		       auth/anonymous-user))]
       (app (assoc req :user user-obj))))
   (if (= (:backend config) "couchdb")
     {:store 
      {:read (fn [sess-key] (get (@(ns-resolve (db/get-backend-package "couchdb") 'get) "sessions" sess-key config) :session {}))
       :write (fn [sess-key sess*]
		(let [sess (@(ns-resolve (db/get-backend-package "couchdb") 'get) "sessions" sess-key config)
		      sess (assoc sess :session sess*)]
		  (get (@(ns-resolve (db/get-backend-package "couchdb") 'update) "sessions" sess config) :session)))
       :delete (fn [sess-key] {})} ; TODO: impl delete
      }
     {})))