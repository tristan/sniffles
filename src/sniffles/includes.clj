(ns sniffles.includes)

(defn route [options]
  (conj ; route defaults
   {:name ""
    :path ""
    :xhr false
    :request-method "*"
    :path-info ""
    :request-params {}
    :header ""
    :accept "*/*"
    }
   (if (contains? options :view)
     (if (var? (:view options))
       (meta (:view options))
       (throw (Exception. "views must be passed as a var (i.e. #'view-name)")))
     nil)
   options))