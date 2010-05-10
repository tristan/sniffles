(ns sniffles.utils.regex
  (:use clojure.contrib.str-utils))

; port of regex reverser from django's django.utils.regex_helper

(def escape-mappings
     {\d "0"
      \D "x"
      \s " "
      \S "x"
      \w "x"
      \W "!"})

(defn- pattern-seq [pattern]
  (lazy-seq
   (when (not (empty? pattern))
     (if (= (first pattern) \\)
       (let [esc (second pattern)
	     rep (get escape-mappings esc)]
	 (if (nil? rep)
	   (pattern-seq (rest (rest pattern)))
	   (cons [rep true] (pattern-seq (rest (rest pattern))))))
       (cons [(first pattern) false] (pattern-seq (rest pattern)))))))

(defn- walk-to-end [iter]
  (loop [iter iter nesting 0]
    (let [[ch, esc?] (first iter)]
      (cond (and (= ch \() (not esc?))
	    (recur (rest iter) (inc nesting))
	    (and (= ch \)) (not esc?))
	    (if (= 0 nesting)
	      (rest iter)
	      (recur (rest iter) (dec nesting)))
	    :else
	    (recur (rest iter) nesting)))))

(defn- get-quantifier [iter]
  (let [[ch esc?] (first iter)]
    (if (or (= ch \*) (= ch \?) (= ch \+))
      [(if (= (first (rest iter)) \?) (rest (rest iter)) (rest iter))
       (if (= ch \+) 1 0)]
      (loop [iter (rest iter) quant []]
	(let [[ch esc?] (first iter)]
	  (if (not (= ch \}))
	    (recur (rest iter) (conj quant ch))
	    [(if (= (first (rest iter)) \?) (rest (rest iter)) (rest iter))
	     (Integer/parseInt (first (re-split #"," (str-join "" quant))))]))))))
	

(defn normalise [pattern]
  ; get the string for the pattern rather than the regex pattern instance
  (let [pattern (if (instance? java.util.regex.Pattern pattern) (.pattern pattern) pattern)]
    (loop [result []
	   non-capturing-groups []
	   consume-next? true ; TODO: not needed, remove
	   pattern-iter (pattern-seq pattern)
	   num-args 0
	   [ch escaped?] (first pattern-iter)]
      (if (empty? pattern-iter)
	result
	(cond (or escaped? (= ch \.)) ; escaped, or "any character"
	      (recur (conj result ch)
		     non-capturing-groups
		     true
		     (rest pattern-iter)
		     num-args
		     (first (rest pattern-iter)))
	      (= ch \$) ; if end of string
	      (recur result
		     non-capturing-groups
		     true
		     nil
		     num-args
		     [nil nil])
	      (= ch \[) ; replace with first char in range
	      (let [new-iter (rest (drop-while #(or (second %) (not (= (first %) \]))) pattern-iter))]
		(recur (conj result (first (first (rest pattern-iter))))
		       non-capturing-groups
		       true
		       new-iter
		       num-args
		       (first new-iter)))
	      (or (= ch \^)) ; ignore
	      (recur result
		     non-capturing-groups
		     true
		     (if consume-next?
		       (rest pattern-iter)
		       pattern-iter)
		     num-args
		     (if consume-next?
		       (first (rest pattern-iter))
		       [ch escaped?]))
	      (= ch \() ; start of a group
	      (let [[ch escaped?] (first (rest pattern-iter))]
		(if (not (= ch \?)) ; positional group
		  (let [pattern-iter (walk-to-end (rest pattern-iter))]
		    (recur (conj result [num-args])
			   non-capturing-groups
			   true
			   pattern-iter
			   (inc num-args)
			   (first pattern-iter)))
		  (let [[ch escaped?] (first (rest (rest pattern-iter)))]
		    (cond (or (= ch \i) (= ch \L) (= ch \m) (= ch \s) (= ch \u) (= ch \#))
			  (let [pattern-iter (walk-to-end (rest (rest pattern-iter)))]
			    (recur result
				   non-capturing-groups
				   true
				   pattern-iter
				   num-args
				   (first pattern-iter)))
			  (= ch \:) ; non-capturing group
			  (recur result
				 (conj non-capturing-groups (count result))
				 true
				 (rest pattern-iter)
				 num-args
				 (first (rest pattern-iter)))
			  :else
			  (throw (Exception. (str "non-reversible regex portion: (?" ch)))))))
	      (= ch \)) ; end of non-capturing-groups
	      (let [start (last non-capturing-groups)
		    inner (drop start result)]
		(recur (conj (vec (take start result)) inner)
		       (pop non-capturing-groups)
		       true
		       (rest pattern-iter)
		       num-args
		       (first (rest pattern-iter))))
	      (or (= ch \*) (= ch \?) (= ch \+) (= ch \{))
	      (let [[pattern-iter nr] (get-quantifier pattern-iter)]
		(recur (if (zero? nr)
			 (if (vector? (last result))
			   (conj (pop result) [nil (last result)])
			   (pop result))
			 (if (> nr 1)
			   (apply conj result (repeat (dec nr) (last result)))
			   result))
			 non-capturing-groups
			 true
			 pattern-iter
			 num-args
			 (first pattern-iter)))
	      :else
	      (recur (conj result ch)
		     non-capturing-groups
		     true
		     (rest pattern-iter)
		     num-args
		     (first (rest pattern-iter)))
	      )))))


;(println (normalise #"^a/([\w]+)/([\d]+)/[\d]{4,5}/$"))

