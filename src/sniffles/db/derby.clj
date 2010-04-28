(ns sniffles.db.derby
  (:use clojure.contrib.str-utils))

(def specification
     {:classname "org.apache.derby.jdbc.EmbeddedDriver"
      :subprotocol "derby"
      :subname "database.derby"
      :create true})

(defmacro varchar [length]
  `(str "VARCHAR(" ~length ")"))

(defmacro integer []
  "INTEGER")

(defmacro auto-increment []
  "GENERATED ALWAYS AS IDENTITY")

(defmacro serial []
  `(str (integer) " " (auto-increment)))

(defmacro primary-key []
  "PRIMARY KEY")

(defmacro foreign-key []
  "FOREIGN KEY")

(defmacro unique []
  "UNIQUE")

(defmacro not-null []
  "NOT NULL")

(defmacro table-exists? [name]
  `(let [[schema# table#] (re-split #"\." ~name)]
     (str 
      "select sys.systables.tablename "
      "from sys.sysschemas, sys.systables "
      "where sys.sysschemas.schemaname='" (.toUpperCase schema#)
      "' and sys.systables.schemaid=sys.sysschemas.schemaid "
      "and sys.systables.tablename='" (.toUpperCase table#) "'"
      )))