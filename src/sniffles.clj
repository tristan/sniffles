(ns sniffles
  (:require [sniffles.management :as management])
  (:use clojure.contrib.command-line
	clojure.contrib.str-utils))

(defn manage* [clargs ns file]
  ;(System/setProperty "user.dir" (.getParent (java.io.File. file)))

  ; push values into the active-project namespace
  (println "Initialising project settings...")
  (let [settings
	(try (var-get (get (ns-map ns) 'settings))
	     (catch NullPointerException e 
	       (throw (Exception. (str "no settings defined in " (ns-name ns))))))]
    (if (not (map? settings))
      (throw (Exception. (str "settings defined in " (ns-name ns) " are not defined as a map")))
;      nil)
      (intern 'sniffles.active-project 'settings 
      ;(var-set #'project-settings
	       (conj settings {:project-name (ns-name ns)
			       :project-ns ns}))) ; add some other settings
    )
  ;(println "project-settings:" project-settings (meta project-settings))

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