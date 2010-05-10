(ns sniffles.management
  (:use clojure.contrib.str-utils)
  (:require ring.adapter.jetty
	    ring.middleware.session
	    ring.middleware.stacktrace
	    [sniffles.core :as core]))

(defn runserver 
  ([project] (runserver project "localhost" 8008))
  ([project host port]
     (println "STARTING SERVER" host port "...")
     (ring.adapter.jetty/run-jetty 
      (core/create-app project)
      {:join? false :host host :port port})
     ))

