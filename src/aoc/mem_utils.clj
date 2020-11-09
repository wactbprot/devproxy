(ns aoc.mem-utils
  (:require [clojure.string :as string]
            [aoc.keys       :as k]
            [aoc.conf          :as c] ;; for debug
            [aoc.mem        :as mem]))

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
  (mapv mem/get-val! (mem/pat->keys (k/id conf "*"))))
