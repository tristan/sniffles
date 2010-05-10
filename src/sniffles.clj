(ns sniffles
  (:require [sniffles.management :as management])
  (:use clojure.contrib.command-line
	clojure.contrib.str-utils))

(defn manage* [clargs ns file]
  (with-command-line clargs
    "manages your project!"
    [[runserver? "run server"]
     [host "host address" "localhost"]
     [port "server port" "8008"]
     ]
    (cond runserver?
	  (.join (management/runserver ns host (Integer/parseInt port)))
	  :else
	  nil)))

(defn initialise-project* [ns]
  ; push values into the active-project namespace
  (println "Initialising project settings...")
  (let [settings
	(try (var-get (get (ns-map ns) 'settings))
	     (catch NullPointerException e 
	       (throw (Exception. (str "no settings defined in " (ns-name ns))))))
	urls
	(try (var-get (get (ns-map ns) 'urls))
	     (catch NullPointerException e 
	       (throw (Exception. (str "no urls defined in " (ns-name ns))))))
	]
    (if (not (map? settings))
      (throw (Exception. (str "settings defined in " (ns-name ns) " are not defined as a map")))
      (intern 'sniffles.active-project 'settings 
	      (conj settings {:project-name (ns-name ns)
			      :project-ns ns}))) ; add some other settings
    (if (not (vector? urls))
      (throw (Exception. (str "urls defined in " (ns-name ns) " are not defined as a vector")))
      (intern 'sniffles.active-project 'urls
	      urls))
    ))

(defmacro manage [clargs]
  `(do
     (initialise-project* *ns*)
     (when ~clargs (manage* ~clargs *ns*))))