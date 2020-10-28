(ns aoc.utils
  (:require [clojure.string :as string]
            [clojure.data.json :as json]))

(defn elem-id
  [conf a b]
  (str a (:sep conf) b))

(defn json->map
  "Transforms a json object to a map."
  [j]
  (json/read-str j :key-fn keyword))

(defn str->clj
  [s]
  {:pre [(string? s)]}
  (let [s-pat #"^-?\d+\.?\d*([Ee]\+\d+|[Ee]-\d+|[Ee]\d+)?$"
        c-pat #"^[\[\{]"]
    (cond
      (re-find s-pat s) (read-string s)
      (re-find c-pat s) (json->map s)
      :else s)))

(defn val->clj
  "Parses value `v` and returns a
  clojure type of it.
  "
  [v]
  (cond
    (string? v)  (str->clj v)
    (boolean? v) v
    :else v))


(defn clj->val
  "Casts the given (complex) value `x` to a writable
  type. `json` is used for complex data types.

  Example:
  ```clojure
  (clj->val {:foo \"bar\"})
  ;; \"{\"foo\":\"bar\"}\"
  (clj->val [1 2 3])
  ;; \"[1,2,3]\"
  ```
  "
  [x]
  (condp = (class x)
    clojure.lang.PersistentArrayMap (json/write-str x)
    clojure.lang.PersistentVector   (json/write-str x)
    clojure.lang.PersistentHashMap  (json/write-str x)
    x))
