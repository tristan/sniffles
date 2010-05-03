(ns test-project.urls
;  (:require sniffles.contrib.admin.urls))
  (:use sniffles.conf.urls)
)

(def urlpatterns (patterns ""
			   ;[#"^admin/" (include "sniffles.contrib.admin.urls")]
			   [#"^test$" "test-project.views/test"]
			   [#"^abc/([\w]+)/([\w+])/([\w]+)/$" "test-project.views/abc" [:a :b :c]]
			   [#"^$" "test-project.views/default"]
			    ))