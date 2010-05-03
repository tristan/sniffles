(ns sniffles.contrib.admin.urls
  (:use [sniffles.conf.urls :only (patterns)])
;  (:require [sniffles.contrib.admin.views :as views])
)

(def urlpatterns 
     (patterns "sniffles.contrib.admin.views"
	       [#"^$" "main"]))