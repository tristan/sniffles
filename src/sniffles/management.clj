(ns sniffles.management
  (:use clojure.contrib.str-utils)
  (:require ring.adapter.jetty
	    ring.middleware.session
	    ring.middleware.stacktrace
	    [sniffles.core :as core]))

(defn runserver [project host port]
  (ring.adapter.jetty/run-jetty 
   (core/create-app project)
   {:host host :port port})
  )

