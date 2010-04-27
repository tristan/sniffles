(ns test-project.manage
  (:use clojure.contrib.command-line))

(with-command-line *command-line-args*
  "manages your project!"
  [[compile? "Compiles models"]
   [sync-db? "sync"]]
  (cond compile?
	(compile 'test-project.models)
	:else
	nil))