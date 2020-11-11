(ns aoc.db-utils
  (:require [com.ashafa.clutch :as couch]
            [aoc.conf          :as c]
            [clojure.string    :as string]))

(defn map-value [m f] (when-let [v (:Value m)] (assoc m :Value (mapv f v))))
(defn norm-unit
  [m]
  (when-let [u (:Unit m)]
    (keyword (string/lower-case u))))

(defn to-si
  [m]
  (when-let [u (norm-unit m)]
    (let [f (condp = u
              :mbar  100.0
              :pa    1.0
              :torr  133.322)]
      (map-value m (fn [x] (* x f))))))
  
  
(defn parse-int [s] (Integer. (re-find  #"\d+" s )))
(defn type-filter-fn [s] (fn [m] (= (:Type m) s))) 
(defn todo-pressure [d] (get-in d [:Calibration :ToDo :Values :Pressure]))

(defn ensure-number 
  "Ensures that all `:Value`s of `m` are numbers.
  
  Example:
  ```clojure
  (ensure-number {:Type \"target_pressure\", :Value [\"100\" 200.3 300 \"500\" 700 1000.1  \"1000\"], :Unit \"Pa\"})
  ;; =>
  ;;
  ;; {:Type target_pressure,
  ;; :Value [100 200.3 300 500 700 1000.1 1000],
  ;; :Unit Pa}
  
  ```"
  [m]
  (map-value m (fn [x] (if (string? x) (parse-int x) x))))


(defn measured? 
  "Checks if number `x` is in vector `v` `n` times. The value in `v` must fit within 1%.
  Example:
  ```clojure
  (def mv (mapv vector  [200.0 300.0 500.0 700.0 1000.0] [1 1 1 1 3]))
  mv
  ;; =>
  ;; [[200.0 1] [300.0 1] [500.0 1] [700.0 1] [1000.0 3]]

  (def v [100.0 200.0 300.0])

  (measured? (first mv) v) ;; 200 1x
  ;; =>
  ;; true
  (def mv (mapv vector  [200.0 300.0 500.0 700.0 1000.0] [2 1 1 1 3]))
  (measured? (first mv) v) ;; 200 2x
  ;; =>
  ;; false
  (measured? (last mv) v) ;; 1000 3x
  ;; =>
  ;; false  
  "

  [[x n] v]
  (<= n (count (filter (fn [y] (and (< x (* y 1.01)) (> x (* y 0.99)))) v))))

(defn target-pressure
  [d]
  (when-let [ps (get-in d [:Calibration :Measurement :Values :Pressure])]
    (let [target? (type-filter-fn "target_pressure")]
     (first (filter target? ps))))) 

(defn next-target-pressure
  [d]
  (let [p-tdo (to-si (ensure-number (todo-pressure d)))
        p-tar (to-si (ensure-number (target-pressure d)))
        v     (:Value p-tar)
        x     (:Value p-tdo)
        n     (:N p-tdo)
        n     (if n n (take (count x) (repeat 1)))
        xn    (map vector x n)]
    (first (first (filter (fn [xn] (not (measured? xn v))) xn)))))

