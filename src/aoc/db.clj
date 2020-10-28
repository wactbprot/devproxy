(ns aoc.db
  (:require [com.ashafa.clutch :as couch]
            [aoc.conf          :as c]
            [clojure.string    :as string]))

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
  [conf]
  (let [couch-conf (:couch conf)
        view       (:devices-view couch-conf)]
    (couch/get-view (:conn couch-conf) (first view) (second view))))


(defn cal-ids
  "Returns all calibration ids belonging to a standard and a year."
  [conf std year]
  (let [couch-conf (:couch conf)
        view       (:calibration-ids-view couch-conf)
        res        (couch/get-view (:conn couch-conf) (first view) (second view)
                                   {:key (str year "_" std)})]
    (mapv :id res)))
