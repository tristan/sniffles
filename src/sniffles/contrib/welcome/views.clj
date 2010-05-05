(ns sniffles.contrib.welcome.views
  (:use sniffles.conf.templates
	ring.util.response
	))

(defn welcome [req]
  (response (render "welcome.pt" req))
  )