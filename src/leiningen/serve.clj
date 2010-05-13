(ns leiningen.serve
  (:use [leiningen.compile :only [eval-in-project]]
	clojure.contrib.str-utils))

(defn serve
  ([project app port host]
     (println project (type project) app)
     (let [ns (symbol (first (re-split #"\/" app)))
	   app (symbol (last (re-split #"\/" app)))]
       (println ns app)
       (eval-in-project 
	project
	`(do (try (require '~'ring.adapter.jetty)
		  (require '~ns)
		  (println (ns-resolve '~ns
				       '~app))
		  (@(ns-resolve '~'ring.adapter.jetty
				'~'run-jetty)
				 (@(ns-resolve '~ns
					      '~app))
				 {:port (Integer. ~port)
				  :host ~host})
                                (catch Exception e#
                                  (.printStackTrace e#)
                                  (println "oh noes an error!")))))))
  ([project port host] (serve project (str (:name project) "/app") port host))
  ([project port] (serve project port "localhost"))
  ([project] (serve project 8005))) ; TODO: cannot provide app and not port host. fix this