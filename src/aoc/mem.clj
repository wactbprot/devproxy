(ns aoc.mem
  (:require [taoensso.carmine :as car :refer (wcar)]
            [aoc.utils :as u]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [aoc.conf           :as c]
            ))

(def conn (get-in (c/config) [:redis :conn]))

;;------------------------------
;; store
;;------------------------------
(defn set-val!
  "Sets the value `v` for the key `k`."
  [k v]
  (when (and (string? k) (some? v))
    (wcar conn (car/set k (u/clj->val v)))))
;;------------------------------
;; del
;;------------------------------
(defn del-key!
  "Delets the key `k`."
  [k]
  (wcar conn (car/del k)))

;;------------------------------
;; get value(s)
;;------------------------------
(defn get-val!
  "Returns the value for the given key (`k`) and cast it to a clojure
  type."
  [k]
  (u/val->clj (wcar conn (car/get k))))

