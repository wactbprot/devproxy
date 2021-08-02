(ns devproxy.man-io
  (:require [devproxy.keys :as k]
            [devproxy.db :as db]
            [devproxy.mem :as mem]
            [devproxy.conf :as c] ;; for debug
            [cheshire.core :as che]))

(defonce ls (atom {}))

(defn subs-pat
  "Generates subscribe patterns which matches depending on:"
  [conf k]
  (str "__keyspace@" (get-in conf [:redis :conn :spec :db]) "*__:" k))

(defn gen-f
  "Generates a closure. Function checks if `task.Ready`is true. If so,
  delivers the promise `p` and closes the own listener."
  [conf p s]
  (fn [_]
    (let [{ok :Ready value :Value} (mem/get-val! s)]
      (when ok (deliver p value) (mem/close-listener (get (deref ls) s))))))

(defn receive
  "Generates a `promise` and sets `task.Ready` to `false`. Registers a
  listener with a [[gen-f]] produced callback which delivers the
  promise if Ready is set to `true`. If a document `id` and a document
  `doc-path` is given, the `result`s are saved via `(db/save conf id
  result doc-path)`"
  ([conf task row]
   (receive conf task row nil nil))
  ([conf {n :TaskName v :Value} row doc-path id]
   (let [p    (promise)
         s    (k/tasks conf row n)
         f    (gen-f conf p s)]
     (mem/set-val! s (assoc (mem/get-val! s) :Ready false))
     (swap! ls assoc s (mem/gen-listener conf (subs-pat conf s) f))
     (let [result (deref p)]
       {:ok     true
        :row    row
        :result result
        :id     id
        :rev (when (and id result doc-path) (db/save conf id [result] doc-path))}))))
