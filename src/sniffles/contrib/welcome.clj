(ns sniffles.contrib.welcome
  (:use sniffles.conf.templates
	ring.util.response
	[sniffles.conf.urls :only (include)]
	)
  (:require [sniffles.contrib.welcome.views :as views]
	    sniffles.contrib.auth))

;(println *file*)
;(println (type *file*))

(def settings {:template-root "src/sniffles/contrib/welcome/templates/"})

(def urls [:welcome [#"^$" views/welcome]
	   [#"" (include sniffles.contrib.auth/urls)]])

;(println "getr:" (.getResource (.getClass *ns*) "/welcome/templates/welcome.pt"))