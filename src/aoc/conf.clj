(ns aoc.conf
  (:require [clojure.edn :as edn]))

(defn config
  "Reads a `edn` configuration in file `f`." 
  ([]
   (config "resources/conf.edn"))
  ([f]
   (-> f slurp edn/read-string)))

