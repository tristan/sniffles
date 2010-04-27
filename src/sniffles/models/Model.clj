(ns sniffles.models.Model
  (:gen-class
   :implements [clojure.lang.ILookup]
   :init init
   :constructors [clojure.lang.PersistentList]
   :state state
   ))

(defn objects [#^Class this-class]
  ["getting objects for:" (.getSimpleName this-class)])

(defn -init []
  [[] (ref {})])

(defn -init [& fields]
  ; if (count fields) is not even error
  ; if = 1 and map, make that the state
  ; needs error to ensure valid fields
  [[] (ref {})])

(defn -valAt 
  ([this #^Object key]
     (get @(.state this) key nil))
  ([this #^Object key #^Object notFound]
     (get @(.state this) key notFound)))