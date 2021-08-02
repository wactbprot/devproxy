(ns devproxy.mem-utils
  (:require [clojure.string :as string]
            [devproxy.keys       :as k]
            [devproxy.utils      :as u] ;; for debug
            [devproxy.conf       :as c] ;; for debug
            [devproxy.mem        :as mem]))

;;----------------------------------------------------------
;; id, ids
;;----------------------------------------------------------
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

;;----------------------------------------------------------
;; id and fullscale
;;----------------------------------------------------------
(defn get-id-and-fullscale
  [conf k]
  {:id        (mem/get-val! k)
   :fullscale (mem/get-val! (k/fullscale conf (k/get-row conf k)))})

;;----------------------------------------------------------
;; id and branch
;;----------------------------------------------------------
(defn get-id-and-branch
  [conf k]
  {:id     (mem/get-val! k)
   :branch (mem/get-val! (k/branch conf (k/get-row conf k)))})

(defn id-and-branch
  "Returns a `map` containing `id` and `branch`

  Example:
  ```clojure
  (id-and-branch (c/config))
  ;; =>
  
  ;; [{:id cal-2020-se3-pn-4025_0012 :branch dut-a}]
  ```
  "
  [conf]
  (let [id-keys (cal-id-keys conf)]
    (when-not (empty? id-keys)
      (mapv (fn [id] (get-id-and-branch conf id)) id-keys))))

;;----------------------------------------------------------
;; id and opx
;;----------------------------------------------------------
(defn get-id-and-opx
  [conf k]
  {:id  (mem/get-val! k)
   :opx (mem/get-val! (k/opx conf (k/get-row conf k)))})

(defn id-and-opx
  "Returns a `map` containing `id` and `opx`."
  [conf]
  (let [id-keys (cal-id-keys conf)]
    (when-not (empty? id-keys)
      (mapv (fn [id] (get-id-and-opx conf id)) id-keys))))

;;----------------------------------------------------------
;; id and port
;;----------------------------------------------------------
(defn get-id-and-port
  [conf k]
  {:id  (mem/get-val! k)
   :port (mem/get-val! (k/opx conf (k/get-row conf k)))})

(defn id-and-port
  "Returns a `map` containing `id` and `port`."
  [conf]
  (let [id-keys (cal-id-keys conf)]
    (when-not (empty? id-keys)
      (mapv (fn [id] (get-id-and-port conf id)) id-keys))))

;;----------------------------------------------------------
;; branch and fullscale
;;----------------------------------------------------------
(defn get-branch-and-fullscale
  [conf k]
  (let [row (k/get-row conf k)]
    {:branch    (mem/get-val! (k/branch conf row))
     :fullscale (mem/get-val! (k/fullscale conf row))}))

(defn branch-and-fullscale
  "Returns a `map` containing `branch` and `fullscale` (a `string`).

  Example:
  ```clojure
  (branch-and-fullscale  (c/config))
  ;; =>
  ;; [{:branch dut-a :fullscale 1.1mbar}]

  ```"
  [conf]
  (let [id-keys (cal-id-keys conf)]
    (when-not (empty? id-keys)
      (mapv (fn [id] (get-branch-and-fullscale conf id)) id-keys))))

;;----------------------------------------------------------
;; device
;;----------------------------------------------------------
(defn store-device-defaults
  "Store `defaults` to mem.  `defaults` is a map `m`. The keys of `m`
  become part of the mem path `p`."
  [conf row defaults]
  (run!
   (fn [[dk dv]] (mem/set-val! (k/defaults conf row (name dk)) dv))
   (seq defaults)))

(defn default-map
  "Returns a `map` with the defauls key value pairs.

  ```clojure
  (default-map (c/config) 0)
  ;; =>
  ;; {@channel 101 @device gpib0,8 @host e75416}
  ```
  "
  [conf row]
  (let [pat (re-pattern (:sep conf))
        ks  (mem/pat->keys (k/defaults conf row "*"))
        f   (fn [k] {(last (string/split k pat)) (str (mem/get-val! k))})]
  (into {} (map f ks))))

;;----------------------------------------------------------
;; tasks
;;----------------------------------------------------------
(defn store-device-task [conf row {taskname :TaskName :as task}]
  (mem/set-val! (k/tasks conf row taskname) task))

(defn store-device-tasks 
 "Store `tasks` to mem."
  [conf row tasks]
  (run! #(store-device-task conf row %) tasks))

(defn update-task
  "Updates the task with the active defaults."
  [conf row task]
  (when task
    (u/replace-map (default-map conf row) task)))

(defn get-task
  "Returns the task active for the given `row` with the given `task-name`.

  Example:
  ```clojure
  (get-task (c/config) 0 \"ind\")
  ;; =>
  ;; [{:TaskName ind ... }]
  ```
  "
  [conf row task-name]
  (update-task conf row (mem/get-val! (k/tasks conf row task-name))))

(defn tasks-by-pat
  "Returns the `tasks` (`vector` of `map`s) belonging to
  the given `pat`ern.
  
  Example:
  ```clojure
  (tasks-by-pat (c/config) 0 \"ind*\")
  ;; =>
  ;; [{:TaskName ind ... }]
  ```
  "
  [conf row pat]
  (let [ks (sort (mem/pat->keys (k/tasks conf row pat)))]
    (mapv (fn [k] (update-task conf row (mem/get-val! k))) ks)))

(defn auto-init-tasks    [conf row] (tasks-by-pat conf row "auto_init*"))
(defn range-ind-tasks    [conf row] (tasks-by-pat conf row "range_ind*"))
(defn ind-tasks          [conf row] (tasks-by-pat conf row "ind*"))
(defn range-offset-tasks [conf row] (tasks-by-pat conf row "range_offset*"))
(defn offset-tasks       [conf row] (tasks-by-pat conf row "offset*"))
(defn auto-offset-tasks  [conf row] (tasks-by-pat conf row "auto_offset*"))

(defn sequences-tasks
  [conf row]
  (let [i (auto-init-tasks    conf row)
        r (range-offset-tasks conf row)
        o (auto-offset-tasks  conf row)]
  (if (empty? r) (interleave i o) (interleave i r o))))


