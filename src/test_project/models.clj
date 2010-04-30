(ns test-project.models
  (:use sniffles.models))

(defmodel Poll
  (question :char-field {:max-length 200})
  (nummer :char-field {:max-length 200 :default "ABC"})
  (otherthing :integer-field {:default 100})
  (id :auto-field)
;  (pub_date :date-time-field {:comment "date published"})
  :primary-key [id question]
)

(defmodel Choice
  ;(poll :foreign-key Poll)
  (choice :char-field {:max-length 200})
  (votes :integer-field)
)