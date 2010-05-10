(ns sniffles.contrib.auth.views
  (:require [sniffles.contrib.auth :as auth])
  (:use sniffles.shortcuts))

(defn login [req]
  (cond (= (:request-method req) :get)
	(render-to-response (or (:template req) "login.pt") req)
	(= (:request-method req) :post)
	(let [form (:form-params req)] ; TODO: form validation. e.g. Don't allow "username" to be blank
	  (let [user (auth/authenticate (get form "username") (get form "password"))]
	    (if (and user (not (:anonymous? user)))
	      (let [r (get req :redirect (get form "redirect" nil))
		    r (if r
			(redirect r req)
			(response "<b>LOGGED IN</b>" req))]
		(assoc-in r [:session :user-id] (:_id user))) ; set user-id in session
	      (render-to-response (or (:template req) "login.pt") (assoc req :errors ["invalid username or password" "and you suck!"])))))))

(defn logout [req]
  (let [req (assoc req :user auth/anonymous-user)] ; TODO: this should probably clean the entire session map
    (let [r (if (contains? req :redirect)
	      (redirect (:redirect req) req)
	      (render-to-response (or (:template req) "logout.pt") req))]
      (assoc-in r [:session :user-id] nil)
  )))