(ns aoc.db
  (:require [com.ashafa.clutch   :as couch]
            [vl-data-insert.core :as i]
            [aoc.conf            :as c]
            [clojure.string      :as string]))

(defn id->doc
  "Gets a document from the long term memory."
  [id conf]
  (try
    (couch/get-document (:conn (:couch conf)) id)
    (catch Exception e
      (println (.getMessage e)))))

(defn put-doc
  "Saves a document to the long term memory."
  [doc conf]
  (try
    (couch/put-document (:conn (:couch conf)) doc)
    (catch Exception e
      (println (.getMessage e)))))

(defn rev-refresh
  "Refreshs the revision `_rev` of the document if
  it exist."
  [doc conf]
  (if-let [db-doc (id->doc (:_id doc) conf)]
    (assoc doc :_rev (:_rev db-doc))
    doc))

(defn devices
  "Returns all devices."
  ([conf]
   (devices conf nil))
  ([conf dev]
   (let [cc   (:couch conf)
         conn (:conn cc)
         view (:devices-view cc)
         f    (first view)
         s    (second view)]
     (if dev
       (couch/get-view conn f s {:key dev})
       (couch/get-view conn f s)))))

(defn device-vec [conf] (mapv :key (devices conf)))

(defn device-defaults
  [conf dev]
  (get-in (first (devices conf dev)) [:value :DeviceClass :Defaults]))

(defn device-tasks
  ([conf]
   (get-in (first (devices conf)) [:value :DeviceClass :Task]))
  ([conf dev]
  (get-in (first (devices conf dev)) [:value :DeviceClass :Task])))


(defn cal-ids
  "Returns all calibration ids belonging to a standard and a year."
  [conf std year]
  (let [cc (:couch conf)
        view       (:calibration-ids-view cc)
        res        (couch/get-view (:conn cc) (first view) (second view)
                                   {:key (str year "_" std)})]
    (mapv :id res)))


(defn store!
  "Gets the document with the given `id`. Stores the result vector `v`
  under path `p` and saves the document. returns the revision. `:_rev`"
  [conf id v p]
  (:_rev (put-doc (i/store-results (id->doc id conf) v p) conf)))
