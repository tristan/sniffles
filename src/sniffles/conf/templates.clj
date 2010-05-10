(ns sniffles.conf.templates
  (:require clj-zpt.tal)
  (:use clojure.contrib.str-utils))

; TODO: link functions, such as redirect to context

(defn render* [base-dir pt context]
  (println "rendering template:" (str base-dir pt))
  (let [f (java.io.File. (str base-dir pt))]
    (if (.exists f)
      (clj-zpt.tal/compile-html-template f context)
      (throw (java.io.FileNotFoundException. (.getCanonicalPath f)))))
 )

(defmacro render [pt context] `(render* (get (get ~context :settings) :template-root) ~pt ~context)) 


