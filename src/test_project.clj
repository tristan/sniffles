(ns test-project
  (:require sniffles
	    sniffles.contrib.welcome
	    sniffles.contrib.auth;.views
	    [sniffles.conf.urls :as urls]
	    [test-project.views :as views])
  )

(def debug true)
(def development true)

(def settings {
	       :couchdb-uri "http://localhost:5984/"
	       :template-root "src/test_project/templates/"
})

(def urls [;[#"^admin/" (urls/include sniffles.contrib.admin)]
	   ;:login [#"^login/" sniffles.contrib.auth.views/login {:template "login.pt" :redirect [:welcome]}]
	   ;:logout [#"^logout/" sniffles.contrib.auth.views/logout {:redirect [:welcome]}]
	   [#"^auth/" (urls/include sniffles.contrib.auth/urls)]
	   [#"" (urls/include sniffles.contrib.welcome/urls)]
	   ])

(sniffles/manage *command-line-args*)
