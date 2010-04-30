
(clojure.core/in-ns 'clojureql)

; modifications for clojureql to enable required functionality

(defn prepare-statement
  "Return a prepared statement for the given SQL statement in the
  context of the given connection."
  {:tag PreparedStatement}
  [sql-stmt #^Connection conn]
  (let [compiled-stmt (compile-sql sql-stmt conn)]
    (when compiled-stmt
      (doto (if (.startsWith (.toUpperCase (str compiled-stmt)) "INSERT")
	      (do (println "including rgk in stmt") (.prepareStatement conn compiled-stmt PreparedStatement/RETURN_GENERATED_KEYS))
	      (.prepareStatement conn compiled-stmt))
        (set-env (sql-stmt :env))))))

(defmethod execute-sql ::ExecuteUpdate
  [sql-stmt conn]
  (let [stmt ((get-method execute-sql ::Execute) sql-stmt conn)]
    (let [update-count (.getUpdateCount #^PreparedStatement stmt)
	  generated-keys (.getGeneratedKeys #^PreparedStatement stmt)]
      {:update-count update-count
       :generated-keys (when (not (nil? generated-keys)) (resultset-seq generated-keys))})))
