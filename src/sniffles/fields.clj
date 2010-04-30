(ns sniffles.fields
 ; (:use sniffles.fields.char-field)
)

; this contains all the base options for a field
(def field-defaults
     {:db-column nil
      :default nil
      :editable true
      :help-text ""
      :null false
      :blank false
      :unique false
      :verbose-name nil
      })

(def char-field-defaults
     {:max-length 200
      :default ""})

(def integer-field-defaults
     {})
     
; :date-time-field
(def date-time-field-defaults
     {})

(defn date-time-field-validation [signature value]
  (cond (instance? java.util.Date value)
	true
	(and (string? value) ; TODO: this is not flexible at all
	     (try (.parse (java.text.DateFormat/getDateTimeInstance) value)
		  true
		  (catch java.text.ParseException e
		    false)))
	true
	:else
	false))

(def auto-field-defaults
     integer-field-defaults)

; type map
(defn type-map [typ]
  (cond (= typ :char-field)
	char-field-defaults
	(= typ :date-time-field)
	date-time-field-defaults
	(= typ :integer-field)
	integer-field-defaults
	(= typ :auto-field)
	auto-field-defaults))

(defn db-map [field]
  (cond (= (first field) :char-field)
	(str "varchar(" (:max-length (second field)) ")")
	(= (first field) :date-time-field)
	'date
	(= (first field) :integer-field)
	'int
	(= (first field) :auto-field)
	'serial))


; maps the field (:type {:options}) to db friendly opts
(defn deffield [opts]
  [(first opts)
   (conj field-defaults
	 (type-map (first opts))
	 (second opts))])