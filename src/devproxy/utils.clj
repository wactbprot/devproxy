(ns devproxy.utils
  (:require  [devproxy.conf :as c] ;; for debug
             [cheshire.core :as che]
             [clojure.edn :as edn]
             [clojure.string :as string]))

(defn map-value [m f]
  (when-let [v (:Value m)]
    (if (vector? v)
      (assoc m :Value (mapv f v))
      (assoc m :Value (f v)))))

(defn norm-unit [m]
  (when-let [u (:Unit m)]
    (keyword (string/lower-case u))))

(defn in-si-unit [m]
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
  (operable-value {:Type \"target_pressure\",
                   :Value [\"100\" 200.3 300 \"500\" 700 1000.1  \"1000\"],
                   :Unit \"Pa\"})
  ;; =>
  ;;
  ;; {:Type target_pressure,
  ;; :Value [100 200.3 300 500 700 1000.1 1000],
  ;; :Unit Pa}
  ```"
  [m]
  (map-value m (fn [x] (if (string? x) (parse-double x) x))))

(defn todo-si-value-vec [d]
  (-> d todo-pressure operable-value in-si-unit :Value))

(defn compare-value [m](-> m operable-value in-si-unit :Value))

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

(defn target-pressure [d]
  (when-let [ps (get-in d [:Calibration :Measurement :Values :Pressure])]
    (let [target? (type-filter-fn "target_pressure")]
     (first (filter target? ps)))))

(defn next-target-pressure [d]
  (let [m-tdo (todo-pressure d)
        m-tar (target-pressure d)
        v     (compare-value m-tar)
        x     (compare-value m-tdo)
        n     (:N m-tdo)
        n     (if n n (take (count x) (repeat 1)))
        xn    (map vector x n)]
    (first (first (filter (fn [xn] (not (measured? xn v))) xn)))))

(defn target-pressure-map [conf p] {:Type "target_pressure" :Value p :Unit "Pa"})

(defn get-val [req] (get-in req [:body :value]))
(defn get-taskname [req] (get-in req [:body :taskname]))
(defn get-row [req] (get-in req [:body :row]))
(defn get-key [req] (get-in req [:body :key]))
(defn get-doc-path [req] (get-in req [:body :DocPath]))
(defn get-target-pressure [req] (get-in req [:body :Target_pressure_value]))
(defn get-target-unit [req] (get-in req [:body :Target_pressure_unit]))

(defn fullscale-vec [conf] (:fullscale conf))

(defn fullscale-of-branch [v branch]
  (when (and (vector? v) (string? branch))
    (->> v
         (filter (fn [x] (= branch (:branch x))))
         first ;; get min would be better
         :fullscale)))

(defn max-pressure-by-fullscale
  "Returns a `map` with at least `:Type` and `Unit` belonging to the given fullscale (`string`).

  Example:
  ```clojure
  (max-pressure-by-fullscale (c/config) \"1mbar\")
  ;; =>
  ;; {:Unit Pa :Display 1mbar :Value 100}
  ```
  "
  [conf fs]
  (when (string? fs)
    (first (filter #(= fs (:Display %)) (fullscale-vec conf)))))

(defn max-pressure
  "Returns a map containing at least `:Value` and `:Unit` for the given
  `branch`." 
  [conf v branch]
  (if-let [fs (fullscale-of-branch v branch)]
    (max-pressure-by-fullscale conf fs)
    {:Unit "Pa" :Value 0.0}))

(defn measure?
  "Compares the `tar`get `x` with the `max`imum `x`. Returns `true` if
  the (maximum + 1%) is greater or equal the target."
  [m-tar m-max]  
  (>= (* 1.01 (compare-value m-max)) (compare-value m-tar)))

(defn open-or-close
  "Returns the string `open` or `close` depending on the values given
  with `mt` (target pressure) and `mb` (fullscale of device at branch.

  Example:   
  ```clojure
  (open-or-close {:Value 0.099, :Unit \"Pa\"} {:Value 0.099, :Unit \"Pa\"})
  ;; =>
  ;; close
  (open-or-close {:Value 0.09, :Unit \"Pa\"} {:Value 0.099, :Unit \"Pa\"})
  ;; =>
  ;; open
  (open-or-close {:Value 1 :Unit \"mbar\"} {:Value 1 :Unit \"Pa\"})
  ;; =>
  ;; close
  (open-or-close {:Value 1 :Unit \"Pa\"} {:Value 1 :Unit \"mbar\"} )
  ;; =>
  ;; open
  ```"
  [m-tar m-bra]
  (if (measure? m-tar m-bra) "open" "close" ))


(defn display-fullscale-vec [conf] (mapv :Display (fullscale-vec conf)))

(defn range-factor [conf s] (get (:range-factor conf) s)) 

(defn range-ok?
  "Checks for task `range-ok?`. The task ranges are given in the form:
  `fullscale/100`. This has to be converted by means of the fullscale
  value `m-f` (form is `{:Value x :Unit y}). The resolved task range
  (e.g. `(* fs (range-factor conf from))`) is compared to the target
  pressure `t` by `(from >= t >= to)`.

  Example:
  ```clojure
  (def from  \"fullscale/100000\")
  (def to \"fullscale/10\")
  
  (range-ok? (c/config) from to  {:Value 14. :Unit \"Pa\"} {:Value 133. :Unit \"Pa\"})
  ;; =>
  ;; false
  (range-ok? (c/config) from to {:Value 13. :Unit \"Pa\"} {:Value 133. :Unit \"Pa\"})
  ;; =>
  ;; true
  ```"
  [conf from to m-t m-f]
  (if (and from to)
    (let [fs (compare-value m-f)]
      (<= (* fs (range-factor conf from))
          (compare-value m-t)
          (* fs (range-factor conf to))))
    true))

(defn suitable-task [conf tasks m-t m-f]
  (first
   (filter (fn [{from :From to :To}] (range-ok? conf from to m-t m-f)) tasks)))
  
(defn elem-id [conf a b] (str a "_" b))

(defn fill-vec [conf item vec]
  (into (if item [item] [(:select conf)]) vec))

(defn fill-kw [conf item kw]
  (into (if item [item] [(:select conf)]) (kw conf)))

(defn map->json
  "Transforms a hash-map  to a json string"
  [m]
  (che/generate-string m))

(defn json->map
  "Transforms a json object to a map."
  [j]
  (che/parse-string j true))

(defn replace-map
  "Replaces the tokens given as keys in the map `m` in `task`."
  [m task]
  (if (map? m)
    (json->map
     (reduce (fn [s [k v]] (string/replace s (re-pattern k) (str v)))
             (map->json task) m))
    task))

(defn body->msg-data-map
  "Avoid sending to much information back to frontend"
  [body]
  (let [{res :Result exc :ToExchange err :error} (che/parse-string body true)]
    {:Result res :Exchange exc :Error err}))

(defn body->msg-data [body] (che/generate-string (body->msg-data-map body)))

  
