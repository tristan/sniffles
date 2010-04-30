(ns test-project.test-models
  (:use test-project.models
	sniffles.models)
  (:require sniffles.db))

(println Poll)
(println (meta Poll))

(def x (create Poll :question "Kann ich bitte ein bier haben?"))
(println "before save....")
(println x)
(println (meta x))
(def x (save x))
(println "after save....")
(println x)
(println (meta x))
;(println (:primary-key (meta (var-get (:model (meta x))))))
(def x (save (assoc x :question "Wie geht es dir?")))
(println "after update....")
(println x)
(println (meta x))