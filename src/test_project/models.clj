(ns test-project.models
  (:use sniffles.models))

(defmodel Poll
  (question :char-field {:max-length 200})
;  (pub_date :date-time-field {:comment "date published"})
)

(defmodel Choice
  ;(poll :foreign-key Poll)
  (choice :char-field {:max-length 200})
  (votes :integer-field)
)