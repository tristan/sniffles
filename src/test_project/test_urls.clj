(ns test-project.test-urls
  (:require sniffles.conf.urls))


(def urlpatterns (sniffles.conf.urls/patterns "test-project.views"
					      [#"^test$" "testv"]
					      [#"^abc/([\w]+)/([\w+])/([\w]+)/$" "abc" [:a :b :c]]
					      [#"^$" "default"]
					      [#"^admin/" (sniffles.conf.urls/include "sniffles.contrib.admin.urls")]
					      )
     )

(println urlpatterns)

(println (urlpatterns "test"))
(println (urlpatterns ""))
(println (urlpatterns "abc/a/b/c/"))
(println (urlpatterns "admin/"))