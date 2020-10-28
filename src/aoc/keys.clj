(ns aoc.keys
  (:require [clojure.string :as string]
            [aoc.mem :as mem]))

(defn from-conf
  [conf kw]
  (str (:prefix conf) (:sep conf) (get-in conf [:keys kw])))

(defn year
  [conf]
  (from-conf conf :year))

(defn gas
  [conf]
  (from-conf conf :gas))

(defn mode
  [conf]
  (from-conf conf :mode))
 
(defn maintainer
  [conf]
  (from-conf conf :maintainer))

(defn standard
  [conf]
  (from-conf conf :standard))
