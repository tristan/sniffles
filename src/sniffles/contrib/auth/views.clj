(ns sniffles.contrib.auth.views
  (:require [sniffles.contrib.auth :as auth])
  (:use sniffles.shortcuts))

(defn login [req]
  (cond (= (:request-method req) :get)
	(render-to-response (or (:template req) "login.pt") req)
	(= (:request-method req) :post)
	(let [form (:form-params req)]
	  (let [user (auth/authenticate (get form "username") (get form "password"))]
	    (if user
	      (let [r (if (contains? form "redirect") 
			(redirect (get form "redirect") req)
			(response "<b>LOGGED IN</b>" req))]
		(assoc-in r [:session :user-id] (:_id user))) ; set user-id in session
	      (render-to-response (or (:template req) "login.pt") (assoc req :errors ["invalid username or password" "and you suck!"])))))))

(defn logout [req]
  (let [req (assoc req :user auth/anonymous-user)] ; TODO: this should probably clean the entire session map
    (assoc-in (render-to-response (or (:template req) "logout.pt") req) [:session :user-id] nil)
  ))