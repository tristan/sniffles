(ns sniffles.contrib.auth.views)
;  (:require [sniffles.contrib.auth :as auth])
;  (:use sniffles.shortcuts))

(defn login 
  {:renderer "sniffles/contrib/auth/templates/login.pt"
   :persist [:user-id]} ; TODO: hmmmmmmmmmmmmmmmm
  [req options]
  (cond (= (:request-method req) :get)
	{}
	(= (:request-method req) :post)
	(let [form (:form-params req)] ; TODO: form validation. e.g. Don't allow "username" to be blank
	  (let [username (get form "username") 
		password (get form "password")
		user {:_id username}] ; TODO: actually get user
	    (if true
	      {:user-id (:_id user)} ; set user-id in session
	      {:errors ["invalid username or password" "and you suck!"]})))))

(defn logout [req options]
  {:user-id nil})