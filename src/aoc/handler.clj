(ns aoc.handler
  (:require [compojure.core :refer :all]
            [compojure.route    :as route]
            [aoc.views          :as v]
            [aoc.config         :as c]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def conf (c/config))

(defroutes app-routes
  (GET "/std/:std" [std] (v/index std conf))
  (route/resources "/")
  (route/not-found (v/not-found)))

(def app (wrap-defaults app-routes site-defaults))

(def server (run-jetty #'app  (:server conf)))

(defn start [] (.start server))

(defn stop [] (.stop server))
