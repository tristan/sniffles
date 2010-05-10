(ns sniffles.shortcuts
  (:require ring.util.response
	    sniffles.active-project
	    sniffles.conf.templates
	    sniffles.conf.urls))

(defn response [body req]
  (let [res (ring.util.response/response body)]
    (if (contains? req :session) ; ensure the session key is kept in place
      (assoc res :session (:session req))
      res)))

(defn render-to-response [template req]
  (response (sniffles.conf.templates/render template req) req))

(defn redirect [uri req & options]
  (if (vector? uri)
    (recur (first uri) req (second uri))
    (let [uri (or (sniffles.conf.urls/reverse uri (first options)) uri)]
      (if (string? uri)
	(let [res (ring.util.response/redirect uri)] 
	  (println res)
	  (if (contains? req :session)
	    (assoc res :session (:session req))
	    res))
	(throw (Exception. (str "uri needs to resolve to a named view or be a string. got " uri " <" (type uri) "," (class uri) ">")))))))
