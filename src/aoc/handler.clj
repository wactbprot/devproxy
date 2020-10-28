(ns aoc.handler
  (:require [clojure.string :as string]
            [aoc.mem :as mem]
            [clojure.data.json        :as  j]
            [aoc.keys :as k]
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

(defn ws
  [req conf]
  (with-channel req channel
    (log/info channel "connected")
    (swap! ws-clients assoc channel true)
    (on-receive channel #'mesg-received)
    (on-close channel (fn [status]
                        (swap! ws-clients dissoc channel)
                        (log/info channel "closed, status" status)))))

(defn year
  [req conf]
  (when-let [year (get-in req [:body :year])]
    (prn "y")
    (res/response (mem/set-val! (k/year conf) year))))

(defn standard
  [req conf]
  (when-let [standard (get-in req [:body :standard])]
    (prn "y")
    (res/response (mem/set-val! (k/standard conf) standard))))

(defn mode
  [req conf]
  (when-let [mode (get-in req [:body :mode])]

    (prn "y")
    (res/response (mem/set-val! (k/mode conf) mode))))

(defn gas
  [req conf]
  (when-let [gas (get-in req [:body :gas])]
    (res/response (mem/set-val! (k/gas conf) gas))))

(defn maintainer
  [req conf]
  (when-let [maintainer (get-in req [:body :maintainer])]
    (res/response (mem/set-val! (k/maintainer conf) maintainer))))

(defn reset
  [req conf]
  (when (get-in req [:body :reset])
    (res/response (mem/del-keys! (mem/pat->keys (str (:prefix conf) "*"))))))
