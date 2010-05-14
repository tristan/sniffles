(ns sniffles.contrib.auth
  (:require [sniffles.utils.sha1 :as sha1])
  (:use clojure.contrib.str-utils))

(def anonymous-user {:id nil
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

;(defn authenticate [username password]
;  (let [user (get-user username)]
;    (if (or (nil? user) (:anonymous? user))
;      (do 
;	(check-password "=00000000000000000" "sha1$00000$0000000000000000000000000000000000000000") ; do password check anyway, to avoid timebased attacks
;	user) ; return anonymous user
 ;     (if (check-password password (:password user))
;	(assoc user :authenticated? true)
;	anonymous-user))))