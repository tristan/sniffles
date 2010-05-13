(ns sniffles.templates
  (:require clj-zpt.tal))

(defn zpt [template]
  (let [input (.getResourceAsStream (clojure.lang.RT/baseLoader) template)]
    (if (nil? input)
      (throw (java.io.FileNotFoundException. template))
      (clj-zpt.tal/compile-template input))))