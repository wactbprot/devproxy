(ns aoc.handler
  (:require [compojure.route          :as route]
            [aoc.views                :as v]
            [aoc.conf                 :as c]
            [compojure.core           :refer :all]
            [ring.adapter.jetty       :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def conf (c/config))

(defroutes app-routes
  (GET "/std/:std" [std] (v/index :main std conf))
  (route/resources "/")
  (route/not-found (v/not-found)))

(def app (wrap-defaults app-routes site-defaults))

(def server (run-jetty #'app  (:server conf)))

(defn start [] (.start server))

(defn stop [] (.stop server))
