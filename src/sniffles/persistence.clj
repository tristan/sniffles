(ns sniffles.persistence
  (:refer-clojure :exclude [get]))

(defn get [db id backend]
  (@(ns-resolve (:package backend) 'get) db id backend))