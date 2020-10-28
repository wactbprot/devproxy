(ns aoc.server
  (:require [compojure.route          :as route]
            [clojure.tools.logging    :as log]
            
            [aoc.views                :as v]
            [aoc.conf                 :as c]
            [aoc.handler              :as h]
            [compojure.core           :refer :all]
            [compojure.handler        :as handler]
            [org.httpkit.server       :refer [run-server]]
            [ring.middleware.json     :as middleware]
            ))


(defonce server (atom nil))
(defroutes app-routes
  (GET "/ws"       []    h/ws-handler)
  (GET "/std/:std" [std] (v/index :main std (c/config)))
  (POST "/year"    []    h/year-handler) 
  (route/resources "/")
  (route/not-found (v/not-found)))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))

(defn stop-server
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server []
  (reset! server (run-server app {:port 8009})))
