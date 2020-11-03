(ns aoc.handler
  (:require
            [aoc.mem               :as mem]
            [aoc.keys              :as k]
            [aoc.utils             :as u]
            [org.httpkit.server    :refer [with-channel
                                           on-receive
                                           on-close
                                           send!]]
            [org.httpkit.client    :as http]
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
  (store (k/device conf (u/get-row req)) (u/get-val req)))

(defn reset
  [conf req]
  (when (u/get-val req)
    (res/response (mem/del-keys! (mem/pat->keys (k/del-pat conf (u/get-row req)))))))

(defn default
  [conf req]
  (store (k/defaults conf (u/get-row req) (u/get-key req)) (u/get-val req)))

(defn default-map
  [conf req]
  (let [sep-pat (re-pattern (:sep conf))
        ks      (mem/pat->keys (k/defaults conf (u/get-row req) "*"))
        f       (fn [k] {(last (string/split k sep-pat)) (str (mem/get-val! k))})]
  (into {} (mapv f ks))))

(defn get-task
  [conf req]
  (let [m    (default-map conf req)
        task (mem/get-val! (k/tasks conf (u/get-row req) (u/get-val req)))]
    (u/replace-map m task)))

(defn send-to-ws-clients
  [conf m]
  (doseq [client (keys @ws-clients)]
    (send! client (j/json-str m))))

(defn dev-hub
  [conf data row]
  (let [conn    (:conn (:dev-hub  conf))
        {body   :body
         status :status} (deref (http/post conn data))]
    (if (> 300 status)
      (send-to-ws-clients conf {:msg (u/body->msg-data body)       :row row})
      (send-to-ws-clients conf {:msg (str "error, status " status) :row row}))))

(defn run
  [conf req]
  (let [row   (u/get-row req)
        task  (get-task conf req)
        data  {:body (j/json-str task)}]
    (send-to-ws-clients conf {:msg "send to relay" :row row})
    (future (dev-hub conf data row))
    (res/response {:ok true})))

