(ns aoc.handler
  (:require [clojure.string :as string]
            [aoc.mem :as mem]
            [clojure.data.json        :as  j]
            [clojure.tools.logging    :as log]
            [org.httpkit.server       :refer [with-channel
                                              on-receive
                                              on-close]]            
            [ring.util.response  :as res]))

(defonce ws-clients (atom {}))

(defn mesg-received
  [msg]
  (let [data (j/read-json msg)]
    (log/info "mesg received" data)))

(defn ws-handler
  [req]
  (with-channel req channel
    (log/info channel "connected")
    (swap! ws-clients assoc channel true)
    (on-receive channel #'mesg-received)
    (on-close channel (fn [status]
                        (swap! ws-clients dissoc channel)
                        (log/info channel "closed, status" status)))))

(defn year-handler
  [req]
  (when-let [year (get-in req [:body :year])] 
    
    (res/response {:ok true})))
