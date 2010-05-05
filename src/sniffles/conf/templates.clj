(ns sniffles.conf.templates
  (:require clj-zpt.tal)
  (:use clojure.contrib.str-utils))

(defn render* [base-dir pt context]
  (println "rendering template:" (str base-dir pt))
  (clj-zpt.tal/compile-html-template (str base-dir pt) context)
 )

(defmacro render [pt context] `(render* (get (get ~context :settings) :template-root) ~pt ~context)) 


