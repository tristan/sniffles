(ns sniffles.contrib.auth
  (:require [sniffles.utils.sha1 :as sha1]
	    [sniffles.persistence :as persistence])
  (:use clojure.contrib.str-utils))

(def anonymous-user {:_id nil
		     :username "anonymous"
		     :first-name "Anonymous"
		     :last-name ""
		     :email ""
		     :password ""
		     :staff? false
		     :superuser? false
		     :anonymous? true
		     :active? false
		     :authenticated? false})

(defmacro authenticated? [req]
  `(get-in req [:user :authenticated?]))

(defn get-user-by-id [req uid]
  (persistence/get "users" uid))

; TODO: on couchdb, this depends on the username view being present. need a db initialisation step which adds this for new dbs
(defn get-user-by-username [req username]
  (first (persistence/select "users" "username" username)))

(defn update-user [req user]
  (persistence/update "users" user))

(defn- hex-digest [algo salt pass]
  (cond (= algo "sha1")
	(sha1/hexdigest salt pass)
	:else
	(throw (Exception. "Got unknown password algorithm type in password."))))

(defn create-password [pass]
  (let [salt (.substring (sha1/hexdigest (str (rand)) (str (rand))) 0 5)
	pass (sha1/hexdigest salt pass)]
    (str-join "$" ["sha1" salt pass])))

(defn- check-password [raw encrypted]
  (let [[algo salt hash] (re-split #"\$" encrypted)
	newhash (hex-digest algo salt raw)]
    (= hash newhash)))

(defn authenticate [req]
  (let [form (:form-params req) ; TODO: form validation. e.g. Don't allow "username" to be blank
	username (get form "username") 
	password (get form "password")]
    (let [user (get-user-by-username req username)]
      (if (or (nil? user) (:anonymous? user))
	(do 
	  (check-password "=00000000000000000" "sha1$00000$0000000000000000000000000000000000000000") ; do password check anyway, to avoid timebased attacks
	  nil) ; return nil since we cannot authenticate this user
	(if (check-password password (:password user))
	  (assoc user :authenticated? true)
	  nil)))))