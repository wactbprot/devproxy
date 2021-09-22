(ns devproxy.conf
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "get configuration from conf.edn. assoc env vars."}
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn config-file [] (io/resource "conf.edn"))

(defn config
  "Reads a `edn` configuration in file `f`." 
  ([] (config (config-file)))
  ([f] (-> f slurp edn/read-string)))

(defn couch-conn [{c :couch}]
  (let [usr (System/getenv "CAL_USR")
        pwd (System/getenv "CAL_PWD")]
    (str (:prot c) "://" (if (and usr pwd) (str usr":"pwd"@") "")
         (:host c)":"(:port c) "/" (:db c))))

(defn devhub-conn [{c :dev-hub}] (str (:prot c)"://" (:host c)":"(:port c) "/"))

(defn env-update [c]
  (let [kl-c [:log-context :facility]
        kr-h [:redis :conn :spec :host]
        kc-h [:couch :host]
        kd-h [:dev-hub :host]
        kd-p [:dev-hub :port]]
    (-> c
        (assoc-in kl-c (or  (System/getenv "DEVPROXY_FACILITY")
                            (System/getenv "DEVHUB_FACILITY")
                            (System/getenv "METIS_FACILITY")))
        (assoc-in kd-h (or  (System/getenv "DEVHUB_HOST") (get-in c kd-h)))
        (assoc-in kd-p (or  (System/getenv "DEVHUB_PORT") (get-in c kd-p)))
        (assoc-in kr-h (or  (System/getenv "REDIS_HOST")  (get-in c kr-h)))
        (assoc-in kc-h (or  (System/getenv "COUCH_HOST")  (get-in c kc-h))))))
