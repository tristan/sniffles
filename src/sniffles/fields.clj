(ns sniffles.fields
 ; (:use sniffles.fields.char-field)
)

; this contains all the base options for a field
(def field-definition
     (with-meta
       {:db-column nil
	:default nil
	:editable true
	:help-text ""
	:primary-key false
	:null false
	:blank false
	:unique false
	:verbose-name nil
	}
       {
	:valid? (fn [] (throw (UnsupportedOperationException. 
			       "__valid?__ has not been implemented for this field type")))
	:cast (fn [] (throw (UnsupportedOperationException. 
			     "__valid?__ has not been implemented for this field type")))
	}))

; :char-field
(defn char-field-validation [signature value]
  (if (and (string? value)
	   (>= (:max-length signature) (count value)))
    true
    false))

(def char-field-definition
     (with-meta
       {:max-length 200
	:default ""}
       {:db [:varchar :max-length]
	:valid? char-field-validation
	:cast (fn [sign value] (str value))}))

; :integer-field
(def integer-field-definition
     (with-meta
       {}
       {:db [:int]
	:valid? (fn [sign value] (integer? value))
	:cast (fn [value] 
		(cond (integer? value)
		      value
		      (string? value) 
		      (Integer/parseInt value)
		      (true? value)
		      1
		      (false? value)
		      0
		      :else
		      (throw (Exception. (str "unable to cast " (type value) " to an integer")))))
	}))
		      

; :date-time-field
(def date-time-field-definition 
     (with-meta
       {} {}))

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

; TODO: :auto-field
(def auto-field-definition
     (with-meta
       integer-field-definition
       (conj (meta integer-field-definition)
	     {:db [:serial]})))

; type map
(def type-map
     {:char-field char-field-definition
      :date-time-field date-time-field-definition
      :integer-field integer-field-definition
      :auto-field auto-field-definition})

; utils
(defmacro field-valid? [signature value]
  `((:valid? (meta ~signature)) 
    ~signature ~value))

(defn create-field [type signature]
  (with-meta
    (conj field-definition
	  (type type-map)
	  signature)
    (conj
     (meta field-definition)
     (meta (type type-map))
     {:type type})))

(defmacro field-default [signature]
  `(:default ~signature))

(defmacro field-cast [signature value]
  `((:cast (meta ~signature))
    ~signature ~value))