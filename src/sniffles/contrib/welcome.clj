(ns sniffles.contrib.welcome
  (:use sniffles.conf.templates
	ring.util.response
	)
  (:require [sniffles.contrib.welcome.views :as views]))

;(println *file*)
;(println (type *file*))

(def settings {:template-root "src/sniffles/contrib/welcome/templates/"})

(def urls [[#"" views/welcome]])


;(println "getr:" (.getResource (.getClass *ns*) "/welcome/templates/welcome.pt"))