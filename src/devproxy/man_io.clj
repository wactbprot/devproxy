(ns devproxy.man-io
  (:require [devproxy.keys :as k]
            [devproxy.db :as db]
            [devproxy.mem :as mem]
            [devproxy.conf :as c] ;; for debug
            [cheshire.core :as che]))

(comment {:redis {:conn {:pool {}
                         :spec {:host "127.0.0.1"
                                :port 6379
                                :db 1}}}})
(comment {:ok     true
          :row    row
          :result result
          :exch   exch
          :id     id
          :rev (when (and id result doc-path) (db/save conf id result doc-path))})

(defonce ls (atom {}))

(defn subs-pat
  "Generates subscribe patterns which matches depending on:"
  [conf k]
  (str "__keyspace@" (get-in conf [:redis :conn :spec :db]) "*__:" k))

(defn receive
  " ...
  If a document `id` and a document `doc-path` is given, the `result`s
  are saved via `(db/save conf id result doc-path)`"
  ([conf task row]
   (receive conf task row nil nil))
  ([conf {v :Value} row doc-path id]
   (let [p (k/manio conf row)
         _ (mem/set-val! p v)
         f (fn [x]
             (prn x)
             ;; (close-listener (x->l x))
             )]
     (swap! ls assoc p (mem/gen-listener conf (subs-pat conf p) f)))))
