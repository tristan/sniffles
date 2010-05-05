(ns test-project.views)

(defn default [request]
  "DEFAULT")

(defn abc [input]
  (str (:a input) " + " (:b input) " + " (:c input)))

(defn testv [input]
  "TEST")