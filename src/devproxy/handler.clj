(ns devproxy.handler
  (:require
   [devproxy.mem-utils         :as memu]
   [devproxy.mem               :as mem]
   [devproxy.keys              :as k]
   [devproxy.utils             :as u]
   [devproxy.db                :as db]
   [devproxy.dev-hub           :as dev-hub]
   [devproxy.man-io            :as man-io]
   [devproxy.conf              :as c] ;; for debug
   [devproxy.ws-server         :as ws-srv]
   [cheshire.core              :as che]
   [com.brunobonacci.mulog     :as µ]
   [clojure.string             :as string]
   [ring.util.response         :as res] ))

(defn year       [conf req] (res/response (mem/set-val! (k/year conf)       (u/get-val req))))
(defn n          [conf req] (res/response (mem/set-val! (k/n conf)          (u/get-val req))))
(defn standard   [conf req] (res/response (mem/set-val! (k/standard conf)   (u/get-val req))))
(defn mode       [conf req] (res/response (mem/set-val! (k/mode conf)       (u/get-val req))))
(defn gas        [conf req] (res/response (mem/set-val! (k/gas conf)        (u/get-val req))))
(defn maintainer [conf req] (res/response (mem/set-val! (k/maintainer conf) (u/get-val req))))

(defn id         [conf req] (res/response (mem/set-val! (k/id conf          (u/get-row req)) (u/get-val req))))
(defn branch     [conf req] (res/response (mem/set-val! (k/branch conf      (u/get-row req)) (u/get-val req))))
(defn fullscale  [conf req] (res/response (mem/set-val! (k/fullscale conf   (u/get-row req)) (u/get-val req))))
(defn port       [conf req] (res/response (mem/set-val! (k/port conf        (u/get-row req)) (u/get-val req))))
(defn opx        [conf req] (res/response (mem/set-val! (k/opx conf         (u/get-row req)) (u/get-val req))))

(defn device [conf req]
  (let [device-name (u/get-val req)
        row         (u/get-row req)]
    (run! mem/del-key! (mem/pat->keys (k/defaults conf row "*")))
    (run! mem/del-key! (mem/pat->keys (k/tasks conf row "*")))
    (memu/store-device-defaults conf row (db/device-defaults conf device-name))
    (memu/store-device-tasks    conf row (db/device-tasks conf device-name))
    (res/response (mem/set-val! (k/device conf row) device-name))))

(defn reset [conf req]
  (when (u/get-val req)
    (res/response (mem/del-keys! (mem/pat->keys (k/del-pat conf (u/get-row req)))))))

(defn default [conf req]
  (res/response (mem/set-val! (k/defaults conf (u/get-row req) (u/get-key req)) (u/get-val req))))

(defn run [conf req]
  (let [row       (u/get-row req)
        task-name (u/get-val req)
        task      (memu/get-task conf row task-name)
        res       (dev-hub/measure conf task row)]
    (µ/log ::target-pressure :TaskName (:TaskName task))
    (ws-srv/send-to-ws-clients conf res)
    (res/response res)))
;;----------------------------------------------------------
;; manual input 
;;----------------------------------------------------------
(defn man-input [conf req] 
  (let [row       (u/get-row req)
        task-name (u/get-taskname req)
        k         (keyword (u/get-key req))
        v         (u/get-val req)
        task      (memu/get-task conf row task-name)]
    (res/response (memu/store-device-task conf row (assoc-in task [:Value k] v)))))

(defn ready-button [conf req] 
  (let [row       (u/get-row req)
        task-name (u/get-taskname req)
        task      (memu/get-task conf row task-name)]
    (res/response (memu/store-device-task conf row (assoc task :Ready true)))))

;;----------------------------------------------------------
;; target pressure 
;;----------------------------------------------------------
(defn remove-rows
  "Returns a vector containing the `rows` to remove from mem because next presssure is above FS.
  
  Example:
  ```clojure
  (remove-rows (c/config) {:Value 1E-6 :Unit Pa})
  ;; =>
  ;; []
  (remove-rows (c/config) {:Value 1E6 :Unit Pa}) 
  ;; =>
  ;; [0 1]
  ```"
  [conf m]
  (let [ks (memu/cal-id-keys conf)
        v  (mapv
            #(assoc (memu/get-id-and-fullscale conf %)
                    :row (k/get-row conf %))
            ks)
        v (filterv
           #(not (u/measure? m (u/max-pressure-by-fullscale conf (:fullscale %))))
           v)]
    (mapv :row v)))

;;----------------------------------------------------------
;; target pressure
;;----------------------------------------------------------
(defn target-pressure
  "Returns the next `Target_pressure` in `Pa`. Checks if this pressure
  exceeds a fullscale of a initialized device and removes the
  corresponding `row` if so. Saves the `target-pressure-map` to the
  remaining documents. Sets `:Continue_mesaurement` to `false` if no
  next pressure can be determined."
  [conf req]
  (let [next-ps (filter some? (map
                               #(u/next-target-pressure (db/id->doc % conf))
                               (memu/cal-ids conf)))]
    (if-not (empty? next-ps)
      (let  [next-p  (apply min next-ps)
             next-m  (u/target-pressure-map conf next-p)
             rm-rows (remove-rows conf {:Value next-p :Unit "Pa"})]
        (doall
         (mapv
          #(mem/del-keys! (mem/pat->keys (k/del-pat conf %)))
          rm-rows))
        (µ/log ::target-pressure :message "next pressure"
                :pressure next-p :unit "Pa")
        (res/response {:ToExchange
                       {:revs (mapv
                               #(db/save conf % [next-m] (u/get-doc-path req))
                               (memu/cal-ids conf))
                        :Target_pressure {:Selected next-p :Unit "Pa"}
                        :Continue_mesaurement {:Bool true}}}))
      (res/response {:ToExchange
                     {:Continue_mesaurement {:Bool false}}}))))

;;----------------------------------------------------------
;; target pressures
;;----------------------------------------------------------
(defn target-pressures
  "A post request to `target-pressures` removes the devices with `fs < next-p`
  from the `mem`."
  [conf req]
  (let [ids (memu/cal-ids conf)]
    (if-not (empty? ids)
      (let [f       (fn [id] (u/todo-si-value-vec (db/id->doc id conf)))
            v       (mapv f ids)
            c       (-> v flatten distinct sort)
            next-p  (first c)]
        (res/response {:ToExchange
                       {:Target_pressure
                        {:Caption "target pressure", 
                         :Select (mapv (fn [p] {:display (str p " Pa")
                                                :value (str p)}) c)
                         :Selected (str next-p) 
                         :Unit "Pa"}}}))
      (res/response {:ToExchange
                     {:Target_pressure
                      {:Caption "target pressure", 
                       :Select [{:display "1.0E-2 Pa"
                                 :value "1-0E-2"}]
                       :Selected "1.0E-2" 
                       :Unit "Pa"}}}))))

;;----------------------------------------------------------
;; calibration ids
;;----------------------------------------------------------
(defn cal-ids [conf req]
  (µ/log ::cal-ids)
  (let [ids (memu/cal-ids conf)]
    (res/response {:ToExchange {:Ids (string/join "@" ids)} :ids ids})))

;;----------------------------------------------------------
;; device under test branch  (se3)
;;----------------------------------------------------------
(defn save-dut-branch [conf req]
  (µ/log ::save-dut-branch)
  (let [p (u/get-doc-path req)
        v (memu/id-and-branch conf)]
    (if (and (string? p) (not (empty? v)))
      (res/response {:ok true
                     :revs (mapv
                            (fn [{id :id x :branch}] (db/save conf id [x] p))
                            v)})
      (res/response {:ok true :warn "no doc selected"}))))

;;----------------------------------------------------------
;; maintainer 
;;----------------------------------------------------------
(defn save-maintainer [conf req]
  (µ/log ::save-maintainer)
  (let [p          (u/get-doc-path req)
        ids        (memu/cal-ids conf)
        maintainer (mem/get-val! (k/maintainer conf))]
    (if (and (string? p) (string? maintainer))
      (res/response {:ok true
                     :revs (mapv
                            (fn [id] (db/save conf id [maintainer] p))
                            ids)})
      (res/response {:ok true :warn "no maintainer selected"}))))

;;----------------------------------------------------------
;; gas
;;----------------------------------------------------------
(defn save-gas [conf req]
  (µ/log ::save-gas)
  (let [p   (u/get-doc-path req)
        ids (memu/cal-ids conf)
        gas (mem/get-val! (k/gas conf))]
    (if (and (string? p) (string? gas))
      (res/response {:ok true
                     :revs (mapv
                            (fn [id] (db/save conf id [gas] p))
                            ids)})
      (res/response {:ok true :warn "no gas selected"}))))

;;----------------------------------------------------------
;; opx (ce3)
;;----------------------------------------------------------
(defn save-opx [conf req]
  (µ/log ::save-opx)
  (let [p (u/get-doc-path req)
        v (memu/id-and-opx conf)]
    (if (and (string? p) (not (empty? v)))
      (res/response {:ok true
                     :revs (mapv
                            (fn [{id :id x :opx}] (db/save conf id [x] p))
                            v)})
      (res/response {:ok true :warn "no doc selected"}))))

;;----------------------------------------------------------
;; port (ce3)
;;----------------------------------------------------------
(defn save-port [conf req]
  (µ/log ::save-port)
  (let [p (u/get-doc-path req)
        v (memu/id-and-port conf)]
    (if (and (string? p) (not (empty? v)))
      (res/response {:ok true
                     :revs (mapv
                            (fn [{id :id x :port}] (db/save conf id [x] p))
                            v)})
      (res/response {:ok true :warn "no doc selected"}))))

;;----------------------------------------------------------
;; device under test maximum
;;----------------------------------------------------------
(defn dut-max [conf req]
  (µ/log ::dut-max)
  (let [p   (u/get-doc-path req)
        v   (memu/branch-and-fullscale conf)
        ids (memu/cal-ids conf)
        mt  {:Value (u/get-target-pressure req) :Unit (u/get-target-unit req)}
        ma  (assoc (u/max-pressure conf v "dut-a") :Type "dut_max_a")
        mb  (assoc (u/max-pressure conf v "dut-b") :Type "dut_max_b")
        mc  (assoc (u/max-pressure conf v "dut-c") :Type "dut_max_c")
        oca (u/open-or-close mt ma)
        ocb (u/open-or-close mt mb)
        occ (u/open-or-close mt mc)]
    (res/response
     {:ToExchange {:revs
                   (mapv
                    (fn [id] (db/save conf id [{:Type "dut_a" :Value oca}
                                               {:Type "dut_b" :Value ocb}
                                               {:Type "dut_c" :Value occ}] p))
                    ids)
                   :Dut_A ma      :Dut_B mb      :Dut_C mc
                   :Set_Dut_A oca :Set_Dut_B ocb :Set_Dut_C occ}})))

;;----------------------------------------------------------
;; start offset-sequences, offset, ind save results
;;----------------------------------------------------------
(defn launch-task [conf {p :DocPath action :Action :as task} row]
  (µ/log ::launch-task)
  (if task
    (let [id     (mem/get-val! (k/id conf row))
          result (condp = (keyword action)
                   :manualInput (man-io/receive conf task row p id)
                   ;; what else
                   (dev-hub/measure conf task row p id))]
      (ws-srv/send-to-ws-clients conf result)
      (Thread/sleep (:seq-delay conf))
      result)
    {:ok true :warn "no task"}))

(defn launch-tasks [conf tasks row]
  (µ/log ::launch-tasks)
  (doall (map (fn [task] (launch-task conf task row)) tasks)))

(defn launch-tasks-vec [conf v]
  (µ/log ::launch-tasks-vec)
  (let [mode (mem/get-val! (k/mode conf))
        f    (fn [{row :row tasks :tasks}]
               (Thread/sleep (* (Integer/parseInt row)
                                (:par-delay conf)))
               (launch-tasks conf tasks row))]
    (if (= mode "parallel") (doall (pmap f v)) (doall (map f v)))))

(defn get-task-vec [conf k mt kind]
  (µ/log ::get-task-vec)
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
(defn ind [conf req]
  (let [p  (u/get-target-pressure req)
        u  (u/get-target-unit req)
        mt {:Value p :Unit u}
        ks (mem/pat->keys (k/fullscale conf "*"))
        v  (mapv (fn [k] (get-task-vec conf k mt :ind)) ks)
        r  (launch-tasks-vec conf v)]
    (µ/log ::ind :pressure p :unit u)
    (res/response {:ok true})))

;;----------------------------------------------------------
;; exec offset samples
;;----------------------------------------------------------
(defn offset-sequences [conf req]
  (let [p  (u/get-target-pressure req)
        u  (u/get-target-unit req)
        mt {:Value p :Unit u}
        ks (mem/pat->keys (k/fullscale conf "*"))
        v  (mapv (fn [k] (get-task-vec conf k mt :sequences)) ks)
        r  (launch-tasks-vec conf v)]
    (µ/log ::offset-sequences :pressure p :unit u)
    (res/response {:ok true})))

;;----------------------------------------------------------
;; exec offset mean value 
;;----------------------------------------------------------
(defn offset [conf req]
  (let [p  (u/get-target-pressure req)
        u  (u/get-target-unit req)
        mt {:Value p :Unit u}
        ks (mem/pat->keys (k/fullscale conf "*"))
        v  (mapv (fn [k] (get-task-vec conf k mt :offset)) ks)
        r  (launch-tasks-vec conf v)]
    (µ/log ::offset :pressure p :unit u)
    (res/response {:ok true})))
