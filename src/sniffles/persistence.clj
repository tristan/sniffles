(ns sniffles.persistence
  (:refer-clojure :exclude [get]))

(def backend 'sniffles.persistence.backends.couchdb)
(def settings {})

(defn set-backend [bkend]
  (def backend bkend))

(defn set-settings [sets]
  (def settings sets))

(defn get [tablename id]
  (@(ns-resolve backend 'get) tablename id))

(defn create [tablename doc]
  (@(ns-resolve backend 'create) tablename doc))

(defn update [tablename doc]
  (@(ns-resolve backend 'create) tablename doc))

(defn select [tablename & options]
  (apply @(ns-resolve backend 'select) tablename options))