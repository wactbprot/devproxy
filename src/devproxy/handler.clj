(ns devproxy.handler
  (:require
   [devproxy.mem-utils         :as memu]
   [devproxy.mem               :as mem]
   [devproxy.keys              :as k]
   [devproxy.utils             :as u]
   [devproxy.db                :as db]
   [devproxy.dev-hub           :as dev-hub]
   [devproxy.conf              :as c] ;; for debug
   [devproxy.ws-server         :as ws-srv]
   [cheshire.core         :as che]
   [clojure.tools.logging :as log]
   [clojure.string        :as string]
   [ring.util.response    :as res] ))


(defn year       [conf req] (res/response (mem/set-val! (k/year conf)       (u/get-val req))))
(defn n          [conf req] (res/response (mem/set-val! (k/n conf)          (u/get-val req))))
(defn standard   [conf req] (res/response (mem/set-val! (k/standard conf)   (u/get-val req))))
(defn mode       [conf req] (res/response (mem/set-val! (k/mode conf)       (u/get-val req))))
(defn gas        [conf req] (res/response (mem/set-val! (k/gas conf)        (u/get-val req))))
(defn maintainer [conf req] (res/response (mem/set-val! (k/maintainer conf) (u/get-val req))))

(defn id         [conf req] (res/response (mem/set-val! (k/id conf          (u/get-row req)) (u/get-val req))))
(defn branch     [conf req] (res/response (mem/set-val! (k/branch conf      (u/get-row req)) (u/get-val req))))
(defn fullscale  [conf req] (res/response (mem/set-val! (k/fullscale conf   (u/get-row req)) (u/get-val req))))

(defn device
  [conf req]
  (let [device-name (u/get-val req)
        row         (u/get-row req)]
    (run! mem/del-key! (mem/pat->keys (k/defaults conf row "*")))
    (run! mem/del-key! (mem/pat->keys (k/tasks conf row "*")))
    (memu/store-device-defaults conf row (db/device-defaults conf device-name))
    (memu/store-device-tasks    conf row (db/device-tasks conf device-name))
    (res/response (mem/set-val! (k/device conf row) device-name))))

(defn reset
  [conf req]
  (when (u/get-val req)
    (res/response (mem/del-keys! (mem/pat->keys (k/del-pat conf (u/get-row req)))))))

(defn default
  [conf req]
  (res/response (mem/set-val! (k/defaults conf (u/get-row req) (u/get-key req)) (u/get-val req))))

(defn run
  [conf req]
  (let [row       (u/get-row req)
        task-name (u/get-val req)
        task      (memu/get-task conf row task-name)
        res       (dev-hub/measure conf {:body (che/encode task)} row)]
    (ws-srv/send-to-ws-clients conf res)
    (res/response res)))


;;----------------------------------------------------------
;; target pressure 
;;----------------------------------------------------------
(defn target-pressure
  [conf req]
  (if-let [ids (memu/cal-ids conf)]
    (if-let [p (first
                (filter some?
                        (map (fn [id] (u/next-target-pressure (db/id->doc id conf))) ids)))]
      (res/response
       {:ToExchange {:revs (mapv (fn [id] (db/save conf id [(u/target-pressure-map conf p)] (u/get-doc-path req))) ids)
                     :Target_pressure.Selected p
                     :Target_pressure.Unit "Pa"
                     :Continue_mesaurement.Bool true}})
      (res/response
       {:ToExchange {:Continue_mesaurement.Bool false}}))
    (res/response
     {:ToExchange {:Continue_mesaurement.Bool false}})))


;;----------------------------------------------------------
;; target pressures
;;----------------------------------------------------------
(defn target-pressures
  [conf req]
  (let [ids (memu/cal-ids conf)]
    (if-not (empty? ids)
      (let [f (fn [id] (u/todo-si-value-vec (db/id->doc id conf)))
            v (mapv f ids)
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

;;----------------------------------------------------------
;; calibration ids
;;----------------------------------------------------------
(defn cal-ids
  [conf req]
  (let [ids (memu/cal-ids conf)]
    (res/response
     {:ToExchange {:Ids (string/join "@" ids)}
      :ids ids})))


;;----------------------------------------------------------
;; device under test branch
;;----------------------------------------------------------
(defn save-dut-branch
  [conf req]
  (let [p (u/get-doc-path req)
        v (memu/id-and-branch conf)]
    (if (and (string? p) (not (empty? v)))
      (res/response
       {:ok true :revs (mapv (fn [{id :id x :branch}] (db/save conf id [x] p)) v)})
      (res/response  {:ok true :warn "no doc selected"}))))

;;----------------------------------------------------------
;; maintainer 
;;----------------------------------------------------------
(defn save-maintainer
  [conf req]
  (let [p          (u/get-doc-path req)
        ids        (memu/cal-ids conf)
        maintainer (mem/get-val! (k/maintainer conf))]
    (if (and (string? p) (string? maintainer))
      (res/response
       {:ok true :revs (mapv (fn [id] (db/save conf id [maintainer] p)) ids)})
      (res/response  {:ok true :warn "no maintainer selected"}))))

;;----------------------------------------------------------
;; gas
;;----------------------------------------------------------
(defn save-gas
  [conf req]
  (let [p   (u/get-doc-path req)
        ids (memu/cal-ids conf)
        gas (mem/get-val! (k/gas conf))]
    (if (and (string? p) (string? gas))
      (res/response
       {:ok true :revs (mapv (fn [id] (db/save conf id [gas] p)) ids)})
      (res/response  {:ok true :warn "no gas selected"}))))

;;----------------------------------------------------------
;; device under test maximum
;;----------------------------------------------------------
(defn dut-max
  [conf req]
  (let [p  (u/get-doc-path req)
        v  (memu/branch-and-fullscale conf)
        mt {:Value (u/get-target-pressure req) :Unit (u/get-target-unit req)}
        ma (assoc (u/max-pressure conf v "dut-a") :Type "dut_max_a")
        mb (assoc (u/max-pressure conf v "dut-b") :Type "dut_max_b")
        mc (assoc (u/max-pressure conf v "dut-c") :Type "dut_max_c")]
    (res/response
     {:ToExchange {:Dut_A ma
                   :Dut_B mb
                   :Dut_C mc
                   :Set_Dut_A (u/open-or-close mt ma)
                   :Set_Dut_B (u/open-or-close mt mb)
                   :Set_Dut_C (u/open-or-close mt mc)}})))

;;----------------------------------------------------------
;; start offset_sequences, offset, ind save results
;;----------------------------------------------------------
(defn launch-task
  [conf task row]
  (if task
    (let [id     (mem/get-val! (k/id conf row))
          p      (:DocPath task)
          data   {:body (che/encode task)}
          result (dev-hub/measure conf data row p id)]
      (ws-srv/send-to-ws-clients conf result)
      result)
    {:ok true :warn "no task"}))

(defn launch-tasks
  [conf tasks row]
  (doall (map (fn [task] (launch-task conf task row)) tasks)))

(defn launch-tasks-vec
  [conf v]
  (let [mode (mem/get-val! (k/mode conf))
        f    (fn [{row :row tasks :tasks}] (launch-tasks conf tasks row))]
    (when (= mode "sequential") (doall (map f v)))
    (when (= mode "parallel")   (doall (pmap f v)))))

(defn get-task-vec
  [conf k mt kind]
  (let [row   (k/get-row conf k)
        fs    (mem/get-val! k)
        mm    (u/max-pressure-by-fullscale conf fs)
        tasks (condp = kind
                :sequences (memu/sequences-tasks conf row)
                :ind       (when (u/measure? mt mm)
                             [(u/suitable-task conf (memu/auto-init-tasks    conf row) mt mm)
                              (u/suitable-task conf (memu/range-ind-tasks    conf row) mt mm)
                              (u/suitable-task conf (memu/ind-tasks          conf row) mt mm)])
                :offset    (when (u/measure? mt mm)
                             [(u/suitable-task conf (memu/auto-init-tasks    conf row) mt mm)
                              (u/suitable-task conf (memu/range-offset-tasks conf row) mt mm)
                              (u/suitable-task conf (memu/offset-tasks       conf row) mt mm)]))]
    {:tasks tasks :row row}))

;;----------------------------------------------------------
;; exec indication mean value
;;----------------------------------------------------------
(defn ind
  [conf req]
  (let [mt {:Value (u/get-target-pressure req) :Unit (u/get-target-unit req)}
        ks (mem/pat->keys (k/fullscale conf "*"))
        v  (mapv (fn [k] (get-task-vec conf k mt :ind)) ks)
        r  (launch-tasks-vec conf v)]
    (res/response {:ok true})))


;;----------------------------------------------------------
;; exec offset samples
;;----------------------------------------------------------
(defn offset_sequences
  [conf req]
  (let [mt {:Value (u/get-target-pressure req) :Unit (u/get-target-unit req)}
        ks (mem/pat->keys (k/fullscale conf "*"))
        v  (mapv (fn [k] (get-task-vec conf k mt :sequences)) ks)
        r  (launch-tasks-vec conf v)]
    (res/response {:ok true})))
        

;;----------------------------------------------------------
;; exec offset mean value 
;;----------------------------------------------------------
(defn offset
  [conf req]
  (let [mt {:Value (u/get-target-pressure req) :Unit (u/get-target-unit req)}
        ks (mem/pat->keys (k/fullscale conf "*"))
        v  (mapv (fn [k] (get-task-vec conf k mt :offset)) ks)
        r  (launch-tasks-vec conf v)]
    (res/response {:ok true})))
