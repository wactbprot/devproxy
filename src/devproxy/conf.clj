(ns devproxy.conf
  (:require [clojure.edn     :as edn]
            [clojure.java.io :as io]))

(defn config-file [] (io/resource "conf.edn"))

(defn config
  "Reads a `edn` configuration in file `f`." 
  ([]
   (config (config-file)))
  ([f]
   (-> f slurp edn/read-string)))

(defn couch-conn
  [c]
  (let [c (:couch c)
        usr (System/getenv "CAL_USR")
        pwd (System/getenv "CAL_PWD")]
    (str (:prot c)"://"
         (if (and usr pwd) (str usr":"pwd"@") "")
         (:host c)":"(:port c) "/" (:db c))))

(defn devhub-conn
  [c]
  (let [c (:dev-hub c)]
    (str (:prot c)"://" (:host c)":"(:port c) "/")))

(defn env-update
  [c]
  (let [kr-h [:redis :conn :spec :host]
        kc-h [:couch :host]
        kd-h [:dev-hub :host]
        kd-p [:dev-hub :port]]
    (-> c
        (assoc-in kd-h (or  (System/getenv "DEVHUB_HOST") (get-in c kd-h)))
        (assoc-in kd-p (or  (System/getenv "DEVHUB_PORT") (get-in c kd-p)))
        (assoc-in kr-h (or  (System/getenv "REDIS_HOST")  (get-in c kr-h)))
        (assoc-in kc-h (or  (System/getenv "COUCH_HOST")  (get-in c kc-h))))))
