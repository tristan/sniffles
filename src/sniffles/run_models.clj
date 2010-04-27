(ns sniffles.run-models)

(defmacro defmodel [name & fields]
  `(import '~(symbol (str "models." name))))