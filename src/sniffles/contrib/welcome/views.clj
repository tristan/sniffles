(ns sniffles.contrib.welcome.views
  (:use sniffles.shortcuts
	))

(defn welcome [req]
  (let [visits (get (get req :session) :visits 0)]
    (assoc-in (render-to-response "welcome.pt" req) [:session :visits] (inc visits))
  ))