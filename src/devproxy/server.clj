(ns devproxy.server
  (:require [compojure.route          :as route]
            [clojure.tools.logging    :as log]
            [clojure.tools.cli        :refer [parse-opts]]
            [devproxy.views           :as v]
            [devproxy.conf            :as c]
            [devproxy.ws-server       :as ws-srv]
            [devproxy.handler         :as h]
            [compojure.core           :refer :all]
            [compojure.handler        :as handler]
            [org.httpkit.server       :refer [run-server]]
            [ring.middleware.json     :as middleware])
  (:gen-class))

(defonce server (atom nil))

(defroutes app-routes
  (GET "/"                   [:as req]     (v/index            (c/config) req))
  (GET "/device/:row"        [row :as req] (v/device           (c/config) req row))
  (POST "/device/:row"       [:as req]     (h/device           (c/config) req))
  (POST "/default/:row"      [:as req]     (h/default          (c/config) req))

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
  
  (POST "/target_pressure"   [:as req]     (h/target-pressure  (c/config) req))
  (GET "/target_pressures"   [:as req]     (h/target-pressures (c/config) req))
  (GET "/cal_ids"            [:as req]     (h/cal-ids          (c/config) req))
  (POST "/save_dut_branch"   [:as req]     (h/save-dut-branch  (c/config) req))
  (POST "/save_maintainer"   [:as req]     (h/save-maintainer  (c/config) req))
  (POST "/save_gas"          [:as req]     (h/save-gas         (c/config) req))
  (POST "/dut_max"           [:as req]     (h/dut-max          (c/config) req))
  
  (POST "/offset_sequences"  [:as req]     (h/offset_sequences (c/config) req))
  (POST "/offset"            [:as req]     (h/offset           (c/config) req))
  (POST "/ind"               [:as req]     (h/ind              (c/config) req))
  
  (GET "/ws"                 [:as req]     (ws-srv/ws          (c/config) req))  

  (route/resources "/")
  (route/not-found (v/not-found)))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))

(defn stop [] (when @server (@server :timeout 100) (reset! server nil)))

(defn start [] (reset! server (run-server app {:port 8009})))


(defn ascii-logo
  []
  (println "\n")
  (println "     d)                                                          ")
  (println "     d)                                                          ")
  (println " d)DDDD e)EEEEE v)    VV p)PPPP   r)RRR   o)OOO  x)   XX y)   YY ")
  (println "d)   DD e)EEEE   v)  VV  p)   PP r)   RR o)   OO   x)X   y)   YY ")
  (println "d)   DD e)        v)VV   p)   PP r)      o)   OO   x)X   y)   YY ")
  (println " d)DDDD  e)EEEE    v)    p)PPPP  r)       o)OOO  x)   XX  y)YYYY ")
  (println "                         p)                                   y) ")
  (println "                         p)                              y)YYYY  ")
  (println "\n"))

(def cli-options
  ;; An option with a required argument
  [["-p" "--port PORT" "Port number of devproxy."
    :default (get-in (c/config) [:server :port])
    :parse-fn (fn [p] (Integer/parseInt p))
    :validate [(fn [p](< 0 p 0x10000)) "Must be a number between 0 and 65536"]]
   ;; A non-idempotent option (:default is applied first)
   ["-r" "--rhost REDISHOST" "Redis host name."
    :parse-fn str
    :default (get-in (c/config) [:redis :conn :spec :host])]
   ["-c" "--chost COUCHHOST" "Couch host name."
    :parse-fn str
    :default (get-in (c/config) [:redis :conn :spec :host])]
   ["-h" "--help"]])

(defn -main [& args]
  (ascii-logo)
  (c/opt-config (parse-opts args cli-options))
  (start))
