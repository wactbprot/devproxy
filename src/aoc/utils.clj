(ns aoc.utils
  (:require [clojure.string    :as string]
            [clojure.data.json :as json]
            [clojure.edn       :as edn]))

(defn map-value [m f]
  (when-let [v (:Value m)]
    (if (vector? v)
      (assoc m :Value (mapv f v))
      (assoc m :Value (f v)))))

(defn norm-unit
  [m]
  (when-let [u (:Unit m)]
    (keyword (string/lower-case u))))

(defn in-si-unit
  [m]
  (when-let [u (norm-unit m)]
    (let [f (condp = u
              :mbar  100.0
              :pa    1.0
              :torr  133.322)]
      (map-value m (fn [x] (* x f))))))


(defn parse-int [s] (Integer. (re-find  #"\d+" s )))
(defn parse-double [s] (edn/read-string s))

(defn type-filter-fn [s] (fn [m] (= (:Type m) s)))
(defn todo-pressure [d] (get-in d [:Calibration :ToDo :Values :Pressure]))

(defn operable-value
  "Ensures that all `:Value`s of `m` are numbers.

  Example:
  ```clojure
  (operable-value {:Type \"target_pressure\", :Value [\"100\" 200.3 300 \"500\" 700 1000.1  \"1000\"], :Unit \"Pa\"})
  ;; =>
  ;;
  ;; {:Type target_pressure,
  ;; :Value [100 200.3 300 500 700 1000.1 1000],
  ;; :Unit Pa}

  ```"
  [m]
  (map-value m (fn [x] (if (string? x) (parse-double x) x))))


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
  ([[x n] v ]
   (measured? [x n] v 1.01 0.99))
  ([[x n] v uf lf]
  (<= n (count (filter (fn [y] (and (< x (* y uf)) (> x (* y lf)))) v)))))

(defn target-pressure
  [d]
  (when-let [ps (get-in d [:Calibration :Measurement :Values :Pressure])]
    (let [target? (type-filter-fn "target_pressure")]
     (first (filter target? ps)))))

(defn next-target-pressure
  [d]
  (let [p-tdo (in-si-unit (operable-value (todo-pressure d)))
        p-tar (in-si-unit (operable-value (target-pressure d)))
        v     (:Value p-tar)
        x     (:Value p-tdo)
        n     (:N p-tdo)
        n     (if n n (take (count x) (repeat 1)))
        xn    (map vector x n)]
    (first (first (filter (fn [xn] (not (measured? xn v))) xn)))))


(defn get-val [req] (get-in req [:body :value]))
(defn get-row [req] (get-in req [:body :row]))
(defn get-key [req] (get-in req [:body :key]))
(defn get-doc-path [req] (get-in req [:body :DocPath]))
(defn get-target-pressure [req] (get-in req [:body :Target_pressure_value]))
(defn get-target-unit [req] (get-in req [:body :Target_pressure_unit]))

(defn fullscale-vec [conf] (get-in conf [:items :fullscale]))

(defn fullscale-for-branch
  [v branch]
  (when (and (vector? v) (string? branch))
    (->> v
         (filter (fn [x] (= branch (:branch x))))
         first ;; get min would be better
         :fullscale)))

(defn max-pressure-map-for-fullscale
  [v fs]
  (when (and (vector? v) (string? fs))
    (->> v
     (filter (fn [m] (= fs (:Display m))))
     first)))

(defn max-pressure
  [conf v branch]
  (if-let [fs (fullscale-for-branch v branch)]
    (max-pressure-map-for-fullscale (fullscale-vec conf) fs)
    {:Unit "Pa" :Value 0.0}))

(defn open-or-close
  [mt mb]
  (let [target (:Value (in-si-unit (operable-value mt)))
        branch (:Value (in-si-unit (operable-value mb)))]
    (if (>= target branch) "close" "open")))
  
(defn display-fullscale-vec
  [conf]
  (mapv :Display (fullscale-vec conf)))

(defn elem-id [conf a b] (str a "_" b))

(defn fill-vec
  [conf item vec]
  (into (if item [item] [(:select conf)]) vec))

(defn fill-kw
  [conf item kw]
  (into (if item [item] [(:select conf)]) (kw conf)))

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
