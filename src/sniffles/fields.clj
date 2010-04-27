(ns sniffles.fields
 ; (:use sniffles.fields.char-field)
)

; this contains all the base options for a field
(def field-definition
  {:db-column nil
   :default nil
   :editable true
   :help-text ""
   :primary-key false
   :null false
   :blank false
   :unique false
   :verbose-name nil
   :__valid?__ (fn [] (throw (UnsupportedOperationException. 
			      "__valid?__ has not been implemented for this field type")))
   :__cast__ (fn [] (throw (UnsupportedOperationException. 
			      "__valid?__ has not been implemented for this field type")))
   })

; :char-field
(defn char-field-validation [signature value]
  (if (and (string? value)
	   (>= (:max-length signature) (count value)))
    true
    false))

(def char-field-definition
     {:max-length 200
      :default ""
      :__valid?__ char-field-validation
      :__cast__ str})

; :date-time-field
(def date-time-field-definition {})

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

; type map
(def type-map
     {:char-field char-field-definition
      :date-time-field date-time-field-definition})

; utils
(defmacro field-valid? [signature value]
  `((:__valid?__ ~signature) ~signature ~value))

(defn create-field [type signature]
  (assoc (conj field-definition
	       (type type-map)
	       signature)
    :type type))

(defmacro field-default [signature]
  `(:default ~signature))

(defmacro field-cast [signature value]
  `((:__cast__ ~signature) ~value))