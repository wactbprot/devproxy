(ns devproxy.dev-hub
  (:require
   [devproxy.db           :as db]
   [devproxy.conf         :as c] ;; for debug
   [org.httpkit.client    :as http]
   [cheshire.core         :as che]))

(defn measure
  "Sends the `task` to the device hub. If a document `id` and a document
  `doc-path` is given, the `result`s are saved via
  `(db/save conf id result doc-path)`"
  ([conf task row]
   (measure conf task row nil nil))
  ([conf task row doc-path id]
   (let [req  (assoc (get-in conf [:dev-hub :request]) :body (che/encode task))
         res  (deref (http/post (c/devhub-conn conf) req))
         {body   :body status :status} res 
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
