(ns aoc.db-utils
  (:require [com.ashafa.clutch :as couch]
            [aoc.conf          :as c]
            [clojure.string    :as string]))

(defn next-target-pressure
  [doc]
  (let [p-tdo (get-in doc [:Calibration :ToDo :Values :Pressure])]
    (prn p-tdo)))
