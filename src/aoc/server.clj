(ns aoc.server
  (:require [compojure.route          :as route]
            [clojure.tools.logging    :as log]
            [aoc.views                :as v]
            [aoc.conf                 :as c]
            [aoc.handler              :as h]
            [compojure.core           :refer :all]
            [compojure.handler        :as handler]
            [org.httpkit.server       :refer [run-server]]
            [ring.middleware.json     :as middleware]))

(defonce server (atom nil))
(defroutes app-routes
  (GET "/"                   [:as req]     (v/index            (c/config) req))
  (GET "/device/:row"        [row :as req] (v/device           (c/config) req row))
  (POST "/device/:row"       [:as req]     (h/device           (c/config) req))
  (POST "/default/:row"      [:as req]     (h/default          (c/config) req))
                                                               
  (GET "/ws"                 [:as req]     (h/ws               (c/config) req))
  (POST "/year"              [:as req]     (h/year             (c/config) req))
  (POST "/n"                 [:as req]     (h/n                (c/config) req))
  (POST "/standard"          [:as req]     (h/standard         (c/config) req))
  (POST "/mode"              [:as req]     (h/mode             (c/config) req))
  (POST "/gas"               [:as req]     (h/gas              (c/config) req))
  (POST "/maintainer"        [:as req]     (h/maintainer       (c/config) req))
                                                               
  (POST "/id"                [:as req]     (h/id               (c/config) req))
  (POST "/branch"            [:as req]     (h/branch           (c/config) req))
  (POST "/fullscale"         [:as req]     (h/fullscale        (c/config) req))
                                                               
  (POST "/reset"             [:as req]     (h/reset            (c/config) req))
  (POST "/run"               [:as req]     (h/run              (c/config) req))
  
  (GET "/target_pressure"    [:as req]     (h/target-pressure  (c/config) req))
  (GET "/target_pressures"   [:as req]     (h/target-pressures (c/config) req))
  (GET "/cal_ids"            [:as req]     (h/cal-ids          (c/config) req))
  (POST "/save_dut_branch"   [:as req]     (h/save-dut-branch  (c/config) req))
  (POST "/save_maintainer"   [:as req]     (h/save-maintainer  (c/config) req))
  (POST "/save_gas"          [:as req]     (h/save-gas         (c/config) req))
  (POST "/dut_max"           [:as req]     (h/dut-max          (c/config) req))
  (POST "/offset_sequences"  [:as req]     (h/offset_sequences (c/config) req))
  (POST "/offset"            [:as req]     (h/offset           (c/config) req))
  (POST "/ind"               [:as req]     (h/ind              (c/config) req))
  
  (route/resources "/")
  (route/not-found (v/not-found)))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))

(defn stop
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn start
  []
  (reset! server (run-server app {:port 8009})))
