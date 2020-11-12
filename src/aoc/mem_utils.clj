(ns aoc.mem-utils
  (:require [clojure.string :as string]
            [aoc.keys       :as k]
            [aoc.conf       :as c] ;; for debug
            [aoc.mem        :as mem]))

(defn cal-id-keys
  "Returns the vector of active cal id keys. "
  [conf]
  (into [] (mem/pat->keys (k/id conf "*"))))

(defn cal-ids
  "Returns a vector with the active calibration document ids.

  Example:
  ```clojure
  (cal-ids (c/config))
  ;; =>
  ;; [ cal-2021-se3-kk-75003_0001 cal-2021-se3-kk-75002_0001]
  ```
  "
  [conf]
  (mapv mem/get-val! (cal-id-keys conf)))

(defn id-and-branch
  [conf]
  (let [id-keys (cal-id-keys conf)]
    (when-not (empty? id-keys)
      (mapv (fn [k] {:id     (mem/get-val! k)
                     :branch (mem/get-val! (k/branch conf (k/get-row conf k)))})
            id-keys))))

(defn branch-and-fullscale
  [conf]
  (let [id-keys (cal-id-keys conf)]
    (when-not (empty? id-keys)
      (mapv (fn [k]
              (let [row (k/get-row conf k)]
                {:branch    (mem/get-val! (k/branch conf row))
                 :fullscale (mem/get-val! (k/fullscale conf row))}))
            id-keys))))

(defn store-device-defaults
  "Store `defaults` to mem when not already there.
  `defaults` is a map `m`. The keys of `m` become part
  of the mem path `p`."
  [conf row defaults]
  (run! (fn [[dk dv]]
          (let [p (k/defaults conf row (name dk))]
            (when-not (mem/get-val! p) (mem/set-val! p dv))))
        (seq defaults)))

(defn store-device-tasks
  "Store `tasks` to mem when not already there."
  [conf row tasks]
  (run! (fn [m]
          (let [p (k/tasks conf row (:TaskName m))]
            (when-not (mem/get-val! p) (mem/set-val! p m))))
        tasks))
