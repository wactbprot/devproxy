(ns aoc.ws-server
  (:require
   [clojure.data.json     :as j]
   [clojure.tools.logging :as log]
   [aoc.conf              :as c] ;; for debug
   [org.httpkit.server    :refer [with-channel
                                  on-receive
                                  on-close
                                  send!]]))

(defonce ws-clients (atom {}))

(defn msg-received
  [msg]
  (let [data (j/read-json msg)]
    (log/info "mesg received" data)))

(defn ws
  [conf req]
  (with-channel req channel
    (log/info channel "connected")
    (swap! ws-clients assoc channel true)
    (on-receive channel #'msg-received)
    (on-close channel (fn [status]
                        (swap! ws-clients dissoc channel)
                        (log/info channel "closed, status" status)))))

(defn send-to-ws-clients
  [conf m]
  (doseq [client (keys @ws-clients)]
    (send! client (j/json-str m))))
