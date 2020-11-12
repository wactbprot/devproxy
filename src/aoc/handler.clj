(ns aoc.handler
  (:require
   [aoc.mem-utils         :as memu]
   [aoc.mem               :as mem]
   [aoc.keys              :as k]
   [aoc.utils             :as u]
   [aoc.db                :as db]
   [aoc.conf              :as c] ;; for debug
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

(defn send-to-ws-clients
  [conf m]
  (doseq [client (keys @ws-clients)]
    (send! client (j/json-str m))))

(defn store
  [key val]
  (if (and val key)
    (res/response (mem/set-val! key val))
    (res/response {:error "no key or val"})))

(defn year       [conf req] (store (k/year conf)       (u/get-val req)))
(defn n          [conf req] (store (k/n conf)          (u/get-val req)))
(defn standard   [conf req] (store (k/standard conf)   (u/get-val req)))
(defn mode       [conf req] (store (k/mode conf)       (u/get-val req)))
(defn gas        [conf req] (store (k/gas conf)        (u/get-val req)))
(defn maintainer [conf req] (store (k/maintainer conf) (u/get-val req)))
(defn id         [conf req] (store (k/id conf          (u/get-row req)) (u/get-val req)))
(defn branch     [conf req] (store (k/branch conf      (u/get-row req)) (u/get-val req)))
(defn fullscale  [conf req] (store (k/fullscale conf   (u/get-row req)) (u/get-val req)))

(defn device
  [conf req]
  (let [device-name (u/get-val req)
        row         (u/get-row req)]
    (run! mem/del-key! (mem/pat->keys (k/defaults conf row "*")))
    (run! mem/del-key! (mem/pat->keys (k/tasks conf row "*")))
    (memu/store-device-defaults conf row (db/device-defaults conf device-name))
    (memu/store-device-tasks    conf row (db/device-tasks conf device-name))
    (store (k/device conf row) device-name)))

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

(defn target-pressure
  [conf req]
  (if-let [ids (memu/cal-ids conf)]
    (if-let [p (first
                (filter some?
                        (map (fn [id] (u/next-target-pressure (db/id->doc id conf))) ids)))]
      (res/response
       {:ToExchange {:Target_pressure.Selected p
                     :Target_pressure.Unit "Pa"
                     :Continue_mesaurement.Bool true}})
      (res/response
       {:ToExchange {:Continue_mesaurement.Bool  false}}))
    (res/response
     {:ToExchange {:Continue_mesaurement.Bool  false}})))

(defn target-pressures
  [conf req]
  (let [ids (memu/cal-ids conf)]
    (if-not (empty? ids)
      (let [v (mapv (fn [id] (u/todo-si-value-vec (db/id->doc id conf))) ids)
            c (-> v flatten distinct sort)]
        (res/response
         {:ToExchange
          {:Target_pressure
           {:Caption "target pressure", 
            :Select (mapv (fn [p] {:display (str p " Pa") :value (str p)}) c)
            :Selected (str (first c)) 
            :Unit "Pa"}}}))
      (res/response
       {:ToExchange
        {:Target_pressure
         {:Caption "target pressure", 
          :Select [{:display "1.0E-2 Pa" :value "1-0E-2"}]
          :Selected "1.0E-2" 
          :Unit "Pa"}}}))))

(defn cal-ids
  [conf req]
  (let [ids (memu/cal-ids conf)]
    (res/response
     {:ToExchange {:Ids (string/join "@" ids)}
      :ids ids})))

(defn save-dut-branch
  [conf req]
  (let [p (u/get-doc-path req)
        v (memu/id-and-branch conf)]
    (if (and (string? p) (not (empty? v)))
      (res/response
       {:ok true :revs (mapv (fn [{id :id x :branch}] (db/store! conf id [x] p)) v)})
      (res/response  {:ok true :warn "no doc selected"}))))

(defn save-maintainer
  [conf req]
  (let [p          (u/get-doc-path req)
        ids        (memu/cal-ids conf)
        maintainer (mem/get-val! (k/maintainer conf))]
    (if (and (string? p) (string? maintainer))
      (res/response
       {:ok true :revs (mapv (fn [id] (db/store! conf id [maintainer] p)) ids)})
      (res/response  {:ok true :warn "no maintainer selected"}))))

(defn save-gas
  [conf req]
  (let [p   (u/get-doc-path req)
        ids (memu/cal-ids conf)
        gas (mem/get-val! (k/gas conf))]
    (if (and (string? p) (string? gas))
      (res/response
       {:ok true :revs (mapv (fn [id] (db/store! conf id [gas] p)) ids)})
      (res/response  {:ok true :warn "no gas selected"}))))

(defn dut-max
  [conf req]
  (let [p        (u/get-doc-path req)
        v        (memu/branch-and-fullscale conf)
        mt       {:Value (u/get-target-pressure req) :Unit (u/get-target-unit req)}
        ma       (assoc (u/max-pressure conf v "dut-a") :Type "dut_max_a")
        mb       (assoc (u/max-pressure conf v "dut-b") :Type "dut_max_b")
        mc       (assoc (u/max-pressure conf v "dut-c") :Type "dut_max_c")]
    (res/response
     {:ToExchange {:Dut_A ma
                   :Dut_B mb
                   :Dut_C mc
                   :Set_Dut_A (u/open-or-close mt ma)
                   :Set_Dut_B (u/open-or-close mt mb)
                   :Set_Dut_C (u/open-or-close mt mc)}})))

(defn offset_sequences
  [conf req]
  (let [p   (u/get-doc-path req)
        ids (memu/cal-ids conf)]
    (res/response {:value ids})))
        
