(ns sniffles.management
  (:require [sniffles.db :as db])
  (:use clojure.contrib.str-utils))

(defn syncdb [project]
  (let [package (symbol (str project ".models"))]
    (require package)
    (let [ns (find-ns package)]
      (if (nil? ns)
	(throw (java.io.FileNotFoundException.
		(str "Could not find namespace " package
		     (if (re-find #"_" (str package))
		       (str " if your namespace is defined with '-'"
			    "make sure you call this with '-' and not '_'")
		       ""))))
	(let [interns (ns-interns ns)
	      models (filter #(get (meta (var-get (val %))) :is-model?)
			     interns)]
	  (doseq [[name model-var] models]
	    (db/create-table project name (var-get model-var)))
	  )
	)
      )
    )
  )