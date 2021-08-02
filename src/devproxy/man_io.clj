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

(defn gen-f
  "Generates a function that checks the io structur at key `s`."
  [s]
  (fn [x]
    (prn x)
    ;; if val ok (:Ready true):
    ;; (deliver p)
    ;; (close-listener (x->l x))
    ))

(defn receive
  " ...
  If a document `id` and a document `doc-path` is given, the `result`s
  are saved via `(db/save conf id result doc-path)`"
  ([conf task row]
   (receive conf task row nil nil))
  ([conf {n :TaskName v :Value} row doc-path id]
   (let [p (promise)
         s (k/tasks conf row n)
         f (gen-f s)]
     (swap! ls assoc s (mem/gen-listener conf (subs-pat conf s) f))
     (let [result (deref p)]
       ;; --> reset :Ready
       {:ok     true
        :row    row
        :result result
        :id     id
        :rev (when (and id result doc-path) (db/save conf id result doc-path))}))))
