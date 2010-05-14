(ns sniffles.views.static
  (:require (ring.util [codec :as codec]
		       [response :as response])))

(defn serve [request options]
  (let [root (get options :root "")
	file (get options :file "")]
    (if (and (= file "") (get options :show-index false))
      (throw (Exception. "don't know how to serve indices (YET!)")) ; TODO: <--
      (let [f-url (.getResource (clojure.lang.RT/baseLoader) (codec/url-decode (str root file)))]
	(if (nil? f-url)
	  (throw (java.io.FileNotFoundException. file))
	  (java.io.File. (.getFile f-url)))))))
      