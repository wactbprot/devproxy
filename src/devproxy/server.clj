(ns devproxy.server
  (:require [compojure.route          :as route]
            [com.brunobonacci.mulog   :as µ]
            [devproxy.views           :as v]
            [clojure.pprint           :as pp]
            [devproxy.conf            :as c]
            [devproxy.ws-server       :as ws-srv]
            [devproxy.handler         :as h]
            [compojure.core           :refer :all]
            [compojure.handler        :as handler]
            [org.httpkit.server       :refer [run-server]]
            [ring.middleware.json     :as middleware])
  (:gen-class))

(defonce server (atom nil))

(def conf (c/env-update (c/config)))

(def logger (atom nil))

(defroutes app-routes
  (GET "/"                   [:as req]     (v/index            conf req))
  (GET "/device/:row"        [row :as req] (v/device           conf req row))
  (POST "/device/:row"       [:as req]     (h/device           conf req))
  (POST "/default/:row"      [:as req]     (h/default          conf req))

  (POST "/year"              [:as req]     (h/year             conf req))
  (POST "/n"                 [:as req]     (h/n                conf req))
  (POST "/standard"          [:as req]     (h/standard         conf req))
  (POST "/mode"              [:as req]     (h/mode             conf req))
  (POST "/gas"               [:as req]     (h/gas              conf req))
  (POST "/maintainer"        [:as req]     (h/maintainer       conf req))
                                                               
  (POST "/id"                [:as req]     (h/id               conf req))
  (POST "/branch"            [:as req]     (h/branch           conf req))
  (POST "/fullscale"         [:as req]     (h/fullscale        conf req))
                                                               
  (POST "/reset"             [:as req]     (h/reset            conf req))
  (POST "/run"               [:as req]     (h/run              conf req))
  
  (POST "/target_pressure"   [:as req]     (h/target-pressure  conf req))
  (GET "/target_pressures"   [:as req]     (h/target-pressures conf req))
  (GET "/cal_ids"            [:as req]     (h/cal-ids          conf req))
  (POST "/save_dut_branch"   [:as req]     (h/save-dut-branch  conf req))
  (POST "/save_maintainer"   [:as req]     (h/save-maintainer  conf req))
  (POST "/save_gas"          [:as req]     (h/save-gas         conf req))
  (POST "/dut_max"           [:as req]     (h/dut-max          conf req))
  
  (POST "/offset_sequences"  [:as req]     (h/offset_sequences conf req))
  (POST "/offset"            [:as req]     (h/offset           conf req))
  (POST "/ind"               [:as req]     (h/ind              conf req))
  
  (GET "/ws"                 [:as req]     (ws-srv/ws          conf req))  

  (route/resources "/")
  (route/not-found (v/not-found)))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))

(defn init-log!
  [{conf :mulog }]
  (µ/set-global-context!
   {:app-name "devproxy"})
  (µ/start-publisher! conf))

(defn stop []
  (when @server (@server :timeout 100)
        (@logger)
        (reset! logger nil)
        (reset! server nil)))

(defn start []
  (reset! logger (init-log! conf))
  (pp/pprint conf)
  (µ/log ::start :message "start devproxy server")
  (reset! server (run-server app {:port 8009})))


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

(defn -main [& args]
  (pp/pprint conf)
  (ascii-logo)
  (µ/log ::start :message "call -main")
  (start))
