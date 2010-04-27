(ns test-project.models
  (:use sniffles.models))

(defmodel Poll
  (question :char-field {:max-length 200})
  (pub_date :date-time-field {:comment "date published"})
)

(def Poll
     {:question {:type :char-field
		 :max-length 200
		 :default ""
		 :__valid?__ (fn []) }}
     {:pub-date {:type :date-time-field
		 :comment "date published"
		 :default nil
		 :__valid?__ (fn [])}})

; Poll object
{:type Poll
 :question "What's up?"
 :pub-date (java.util.Date.)}

(models/create-object Poll {:question "What's up?"})



(defmodel Choice
  (poll foreign-key Poll)
  (choice char-field :max_length 200)
  (votes integer-field)
)