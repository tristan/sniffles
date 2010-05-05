(ns sniffles
  (:require [sniffles.management :as management])
  (:use clojure.contrib.command-line
	clojure.contrib.str-utils))


(defn manage* [clargs ns file]
  ;(System/setProperty "user.dir" (.getParent (java.io.File. file)))
  (with-command-line clargs
    "manages your project!"
    [[runserver? "run server"]
     [host "host address" "localhost"]
     [port "server port" "8008"]
     ]
    (cond runserver?
	  (management/runserver ns host (Integer/parseInt port))
	  :else
	  nil)))


(defmacro manage [clargs]
  `(manage* ~clargs *ns* *file*))