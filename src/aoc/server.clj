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

  (GET "/"            [:as req] (v/index :main req (c/config)))
  (POST "/year"       [:as req] (h/year        req (c/config)))
  (POST "/standard"   [:as req] (h/standard    req (c/config)))
  (POST "/mode"       [:as req] (h/mode        req (c/config)))
  (POST "/gas"        [:as req] (h/gas         req (c/config)))
  (POST "/maintainer" [:as req] (h/maintainer  req (c/config)))
  (POST "/reset"      [:as req] (h/reset       req (c/config))) 
  (GET "/ws"          [:as req] (h/ws          req (c/config)))
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
