(ns aoc.server
  (:require [compojure.route          :as route]
            [clojure.tools.logging    :as log]
            [clojure.data.json        :as  j]
            [aoc.views                :as v]
            [aoc.conf                 :as c]
            [compojure.core           :refer :all]
            [compojure.handler :as handler]
            [org.httpkit.server       :refer :all]
            [ring.middleware.json :as middleware]
            [ring.util.response :as res]
            ))

(defonce ws-clients (atom {}))
(defonce server (atom nil))

(defn mesg-received
  [msg]
  (let [data (j/read-json msg)]
    (log/info "mesg received" data)))

(defn ws-handler
  [req]
  (with-channel req channel
    (log/info channel "connected")
    (swap! ws-clients assoc channel true)
    (on-receive channel #'mesg-received)
    (on-close channel (fn [status]
                        (swap! ws-clients dissoc channel)
                        (log/info channel "closed, status" status)))))

(defn year-handler
  [req]
  (if-let [year (get-in req [:body :year])] 
    (res/response {:ok true})
    (res/response {:error true}))

(defroutes app-routes
  (GET "/ws" []  ws-handler)
  (GET "/std/:std" [std] (v/index :main std (c/config)))
  (POST "/year" []  year-handler) 
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
