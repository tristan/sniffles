(ns sniffles.contrib.auth
  (:require [sniffles.contrib.auth.backends.couchdb :as backend]
	    [sniffles.utils.sha1 :as sha1])
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

(defn get-user [uid]
  (if (nil? uid) 
    anonymous-user
    (or 
     (backend/get-user uid)
     anonymous-user)))

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

(defn authenticate [username password]
  (let [user (get-user username)]
    (if (or (nil? user) (:anonymous? user))
      (do 
	(check-password "=00000000000000000" "sha1$00000$0000000000000000000000000000000000000000") ; do password check anyway, to avoid timebased attacks
	user) ; return anonymous user
      (if (check-password password (:password user))
	(assoc user :authenticated? true)
	anonymous-user))))

(defn initialise "initialise the backend to make sure everything is ready for use" [] (backend/initialise))

;{:remote-addr 127.0.0.1, :scheme :http, :session {}, :request-method :get, :query-string nil, :content-type nil, :cookies {ring-session {:value f932c2df-e4c4-485b-9438-85a3ed3f435b}}, :uri /, :server-name localhost, :headers {user-agent Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.3) Gecko/20100423 Ubuntu/10.04 (lucid) Firefox/3.6.3, cookie ring-session="f932c2df-e4c4-485b-9438-85a3ed3f435b", keep-alive 115, accept-charset ISO-8859-1,utf-8;q=0.7,*;q=0.7, accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8, host localhost:8008, cache-control max-age=0, accept-encoding gzip,deflate, accept-language en-us,en;q=0.5, connection keep-alive}, :content-length nil, :server-port 8008, :character-encoding nil, :settings {}, :body #<Input org.mortbay.jetty.HttpParser$Input@7e413fc6>} java.lang.String

(require 'sniffles.contrib.auth.views)

(def settings {:template-root "src/sniffles/contrib/auth/templates/"})
(def urls [:login [#"^login/$" sniffles.contrib.auth.views/login]
	   :logout [#"^logout/$" sniffles.contrib.auth.views/logout]])