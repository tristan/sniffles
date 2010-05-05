(ns sniffles.conf.urls
  (:require [sniffles.core :as core])
  (:use clojure.contrib.str-utils))


(defn include* [package]
  (core/create-app package)
)

(defmacro include [package]
  `(include* (find-ns '~package)))