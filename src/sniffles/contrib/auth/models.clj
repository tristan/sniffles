(ns sniffles.contrib.auth.models
  (:use sniffles.models)

(defmodel User
  (:username :char-field {:max-length 30
			  :unique true})
  (:first-name :char-field {:max-length 30
			    :blank true})
  (:last-name :char-field {:max-length 30
			    :blank true})
  (:email :email-field {:blank true})
  (:password :char-field {:max-length 128})
  (:superuser? :boolean-field {:default false})
)