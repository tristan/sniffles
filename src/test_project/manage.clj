(ns test-project.manage
  (:use clojure.contrib.command-line
	clojure.contrib.str-utils)
  (:require [sniffles.management :as management])
  )

(with-command-line *command-line-args*
  "manages your project!"
  [[syncdb? "sync models"]]
  (cond syncdb?
	(management/syncdb (re-gsub #"\.manage" "" (str *ns*)))
	:else
	nil))