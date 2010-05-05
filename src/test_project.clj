(ns test-project
  (:require sniffles
	    sniffles.contrib.welcome
	    [sniffles.conf.urls :as urls]
	    [test-project.views :as views])
  )

(def settings {
})

(def urls [;[#"^admin/" (urls/include sniffles.contrib.admin)]
	   [#"" (urls/include sniffles.contrib.welcome)]
	   ])

(sniffles/manage *command-line-args*)