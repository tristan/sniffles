(ns sniffles.contrib.welcome.views
  (:use sniffles.conf.templates
	ring.util.response
	))

(defn welcome [req]
  (let [visits (get (get req :session) :visits 0)]
    (assoc (response (render "welcome.pt" req)) :session {:visits (inc visits)})
  ))