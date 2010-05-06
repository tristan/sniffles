(ns sniffles.contrib.auth.views
  (:require [sniffles.contrib.auth :as auth])
  (:use sniffles.shortcuts))

(defn login [req]
  (cond (= (:request-method req) :get)
	(render-to-response (or (:template req) "login.pt") req)
	(= (:request-method req) :post)
	(let [form (:form-params req)]
	  (let [user (auth/do-login (get form "username") (get form "password"))]
	    (if user
	      (assoc-in (response "<b>LOGGED IN</b>" req) [:session :user-id] (:_id user))
	      (response "<b>FAILED LOGIN</b>" req))))))