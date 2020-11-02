(ns aoc.handler
  (:require
            [aoc.mem               :as mem]
            [aoc.keys              :as k]
            [aoc.utils             :as u]
            [org.httpkit.server    :refer [with-channel
                                           on-receive
                                           on-close]]
            [clojure.data.json     :as j]
            [clojure.tools.logging :as log]
            [clojure.string        :as string]
            [ring.util.response    :as res] ))


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

(defn store
  [key val]
  (if (and val key)
    (res/response (mem/set-val! key val))
    (res/response {:error "no key or val"})))

(defn year       [conf req] (store (k/year conf)       (u/get-val req)))
(defn standard   [conf req] (store (k/standard conf)   (u/get-val req)))
(defn mode       [conf req] (store (k/mode conf)       (u/get-val req)))
(defn gas        [conf req] (store (k/gas conf)        (u/get-val req)))
(defn maintainer [conf req] (store (k/maintainer conf) (u/get-val req)))
(defn id         [conf req] (store (k/id conf          (u/get-row req)) (u/get-val req)))
(defn branch     [conf req] (store (k/branch conf      (u/get-row req)) (u/get-val req)))
(defn fullscale  [conf req] (store (k/fullscale conf   (u/get-row req)) (u/get-val req)))

(defn device     [conf req]
  (run! mem/del-key! (mem/pat->keys (k/defaults conf (u/get-row req) "*")))
  (run! mem/del-key!  (mem/pat->keys (k/tasks conf    (u/get-row req) "*")))
  (prn "del *")
  (store (k/device conf (u/get-row req)) (u/get-val req)))

(defn reset
  [conf req]
  (when (u/get-val req)
    (res/response (mem/del-keys! (mem/pat->keys (k/del-pat conf (u/get-row req)))))))

(defn default
  [conf req]
  (store (k/defaults conf (u/get-row req) (u/get-key req)) (u/get-val req)))

(defn run
  [conf req]
  (prn "run"))
