(ns sniffles.shortcuts
  (:require ring.util.response
	    sniffles.conf.templates))

(defn response [body req]
  (let [res (ring.util.response/response body)]
    (if (contains? req :session) ; ensure the session key is kept in place
      (assoc res :session (:session req))
      res)))

(defn render-to-response [template req]
  (response (sniffles.conf.templates/render template req) req))

(defn redirect [uri req]
  (let [res (ring.util.response/redirect uri)]
    (if (contains? req :session)
      (assoc res :session (:session req))
      res)))
