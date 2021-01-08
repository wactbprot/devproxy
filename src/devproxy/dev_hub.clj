(ns devproxy.dev-hub
  (:require
   [devproxy.db                :as db]
   [devproxy.conf              :as c] ;; for debug
   [org.httpkit.client    :as http]
   [cheshire.core         :as che]))

(defn measure
  "Sends the given `data` (the `task`) to the device hub. If a document
  `id` and a document `doc-paht` is given, the `result`s are saved
  via `(db/save conf id result doc-path)`"
  ([conf data row]
   (measure conf data row nil nil))
  ([conf data row doc-path id]
   (let [{body   :body
          status  :status} (deref (http/post (:conn (:dev-hub  conf)) data))
         {result :Result
          exch   :ToExchange
          err    :error} (che/decode body true)]
     (if (> 400 status)
       {:ok     true
        :row    row
        :result result
        :exch   exch
        :id     id
        :rev (when (and id result doc-path) (db/save conf id result doc-path))
        :error err}
       {:error  true
        :reason "http error"
        :status status
        :row    row}))))
