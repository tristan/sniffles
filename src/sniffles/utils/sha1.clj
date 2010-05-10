(ns sniffles.utils.sha1
  (:import java.security.MessageDigest)
  (:use clojure.contrib.str-utils))

(defn hexdigest [salt string]
  (let [digest (MessageDigest/getInstance "SHA1")]
    (.reset digest)
    (.update digest (.getBytes salt))
    (let [bytes (.digest digest (.getBytes string "UTF-8"))]
      (str-join "" (map #(format "%02x" %) bytes)))))
