(ns aoc.utils
  (:require [clojure.string :as string]
            [clojure.data.json :as json]))

(defn get-val [req] (get-in req [:body :value]))
(defn get-row [req] (get-in req [:body :row]))
(defn get-key [req] (get-in req [:body :key]))
(defn get-doc-path [req] (get-in req [:body :DocPath]))

(defn elem-id [conf a b] (str a "_" b))

(defn fill-vec
  [conf item vec]
  (into (if item [item] [(:select conf)]) vec))

(defn fill-kw
  [conf item kw]
  (into (if item [item] [(:select conf)]) (kw conf)))

(defn map->json
  "Transforms a hash-map  to a json string"
  [m]
  (json/write-str m))

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

(defn clj->str-val
  [x]
  (str (clj->val x)))

(defn replace-map
  "Replaces the tokens given as keys in the map `m` in `task`.
  "
  [m task]
  (if (map? m)
    (json->map
     (reduce
      (fn [s [k v]]
        (let [pat (re-pattern k)
              r   (clj->str-val v)]
          (string/replace s pat r)))
      (map->json task) m))
    task))

(defn body->msg-data-map
  "Avoid sending to much information back to frontend"
  [body]
  (let [{res :Result
         exc :ToExchange
         err :error} (json/read-str body :key-fn keyword)]
    {:Result res :Exchange exc :Error err}))

(defn body->msg-data
  [body]
  (json/json-str (body->msg-data-map body)))
