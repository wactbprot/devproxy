(ns aoc.keys
  (:require [clojure.string :as string]
            [aoc.mem :as mem]))

(defn from-conf
  ([conf kw]
   (from-conf conf kw nil))
  ([conf kw row]
   (let [s (:sep conf)
         p (:prefix conf)
         k (get-in conf [:keys kw])]
     (if row
       (str p s row s k)
       (str p s k)))))

(defn del-pat
  [conf row]
  (let [s (:sep conf)
        p (:prefix conf)]
    (if row
      (str p s row "*")
      (str p "*"))))

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

(defn id
  [conf row]
  (str (from-conf conf :id  row)))

(defn branch
  [conf row]
  (str (from-conf conf :branch row)))

(defn fullscale
  [conf row]
  (str (from-conf conf :fullscale row)))