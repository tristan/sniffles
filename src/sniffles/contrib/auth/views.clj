(ns sniffles.contrib.auth.views
  (:require [sniffles.contrib.auth :as auth])
  (:use ring.util.response))

(defn login 
  {:renderer "sniffles/contrib/auth/templates/login.pt"}
  [req options]
  (cond (= (:request-method req) :get)
	{}
	(= (:request-method req) :post)
	(let [user (auth/authenticate req)]
	  (if user
	    (conj {:user user 
		   :session {:user-id (:_id user)}} ; TODO: this is very couchdb specific
		  (if (contains? options :redirect)
		    (redirect (:redirect options))
		    {}))
	    {:errors ["invalid username or password" "and you suck!"]}))))

(defn logout [req options]
  (if (contains? options :redirect)
    (assoc (redirect (:redirect options))
      :session {:user-id nil})
    {:session {:user-id nil}}))

(defn information-form [user]
  {:fields [{:name "first-name" :label "First Name" :type "text" :value (or (:first-name user) "")}
	    {:name "last-name" :label "Last Name" :type "text" :value (or (:last-name user) "")}
	    {:name "email" :label "Email" :type "text" :value (or (:email user) "")}
	    {:name "location" :label "Location" :type "text" :value (or (:location user) "")}]})

(defn profile
  [req options]
  (if (:anonymous? (:user req))
    {:status 404 :body ""}
    (cond (= (:request-method req) :get)
	  {:forms {:information (information-form (:user req))}}
	  (= (:request-method req) :post)
	  (let [type (get-in req [:form-params "id"])
		form (dissoc (get req :form-params) "id")]
	    (if (= type "information")
	      (let [updated-user (conj (:user req) (zipmap (map keyword (keys form)) (vals form)))]
		(if (auth/update-user req updated-user)
		  {:user updated-user :forms {:information (information-form updated-user)}}
		  {:forms {:information (information-form (:user req))}}))
	      {:forms {:information (information-form (:user req))}})))))
