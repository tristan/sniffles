(ns sniffles.conf.urls
  (:use clojure.contrib.str-utils))

(defn- get-fq-fn-name [base ext]
  (if (= "" base)
    ext
    (str base (if (re-find #"\/" ext) "." "/") ext)))

(defn- get-fq-fn [base ext]
  (var-get (find-var (symbol (get-fq-fn-name base ext)))))

(defn- run-pattern-query [base urlpatterns url]
  (let [f (first urlpatterns)]
    (let [match (re-find (first f) url)]
      (if match
	(if (fn? (second f))
	  ((second f) (re-gsub (first f) "" url))
	  ((get-fq-fn base (second f)) (apply hash-map (interleave (first (rest (rest f))) (rest match)))))
	(recur base (rest urlpatterns) url)))))

(defn patterns [base & urlpatterns]
  (doseq [p urlpatterns] 
    (when (not (fn? (second p))) ; TODO: make this only call once per namespace
      (let [pack (get-fq-fn-name base (second p))
	    [pack func] (re-split #"\/" pack)]
	(println "calling load" (re-gsub #"-" "_" (re-gsub #"\." "/" pack)))
	(load (str "/" (re-gsub #"-" "_" (re-gsub #"\." "/" pack)))))))
  (fn [url] (run-pattern-query base urlpatterns url))
     )

(defn include [package]
  (let [pack (get-fq-fn-name "" package)]
    (load (str "/" (re-gsub #"-" "_" (re-gsub #"\." "/" pack))))
    (let [func (get-fq-fn pack "urlpatterns")]
      (fn [url] (func url)))))