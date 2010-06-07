(ns sniffles
  (:use [sniffles.dispatch :only (dispatch reverse-route)]
	sniffles.includes
	clojure.contrib.str-utils)
  (:require ring.middleware.session
	    ring.middleware.params
	    sniffles.persistence.middleware
	    sniffles.contrib.auth.middleware
	    [sniffles.persistence :as persistence]
	    [clj-zpt.tales :as tales])
  (:import java.util.UUID)
  )

(def settings {})

(defn application* [routes config]
  (def settings config)
  (let [routes (for [r routes] (route r))]
    (tales/register-extension ; register reverse extention for tales
     "reverse"
     (fn tales-reverse [string context]
       (let [s (re-split #" " string)
	     name (first s)
	     opts (apply hash-map (rest s))
	     ks (map keyword (keys opts))
	     vs (map #(tales/evaluate % context) (vals opts))
	     opts (zipmap ks vs)]
       (reverse-route routes name opts))))
    (fn [request] ; TODO: middleware wrap
      (dispatch routes request))))

; :authentication assumes session
(def couch-db-session-manager
     {:cookie-attrs {:path "/"
		     :domain "127.0.0.1" ; TODO: un-hardcode these...
		     :port "8005"}
      :store
      {:read (fn [sess-key]
	       (get (persistence/get "sessions" sess-key) :session {}))
      :write (fn [sess-key* sess*]
	       (let [sess-key (or sess-key* (str (UUID/randomUUID)))
		     sess (persistence/get "sessions" sess-key)
		     sess (assoc sess :session sess*)]
		 (if (contains? sess :_id)
		   (persistence/update "sessions" sess)
		   (persistence/create "sessions" (assoc sess :_id sess-key)))
		 sess-key))
      :delete (fn [sess-key] nil)} ; TODO: impl delete
     })

(defn apply-auth [app config]
  (if (contains? config :authentication)
    (sniffles.contrib.auth.middleware/wrap-auth app config)
    app))

(defn apply-session [app config] 
  (if (or (and (contains? config :use-session?)
	       (= (config :use-session?) true))
	  (contains? config :authentication))
    (ring.middleware.session/wrap-session app couch-db-session-manager)
    app))

(defn apply-persistence [app config]
  (if (or (contains? config :persistence-backend) 
	  (and (contains? config :use-session?)
	       (= (config :use-session?) true)))
    (sniffles.persistence.middleware/wrap-persistence app config)
    app))

(defn apply-debug [app config]
  (if (and (contains? config :debug?)
	   (true? (config :debug?)))
    (fn [req]
      (println "DEBUG: REQ:" req)
      (let [res (app (assoc req :debug? true))]
	(println "DEBUG: RES:" res)
	res))
    app))

(defmacro application [routes config & middleware]
  `(-> (application* ~routes ~config)
       ~@middleware
       (ring.middleware.params/wrap-params)
       (apply-auth ~config)
       (apply-session ~config)
       (apply-persistence ~config)
       (apply-debug ~config)
       ))