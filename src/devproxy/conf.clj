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
    (str (:prot c)"://" (if (and usr pwd) (str usr":"pwd"@")  "") (:srv c)":"(:port c) "/" (:db c))))

(defn opt-config
  [opt]
  (let [m  (config)
        rv [:redis :conn :spec :host]
        cv [:couch :srv]
        pv [:server :port]
        r  (get-in opt [:options :rhost] (get-in m rv))
        c  (get-in opt [:options :chost] (get-in m cv))
        p  (get-in opt [:options :port]  (get-in m pv))]
    (-> m (assoc-in rv r) (assoc-in cv c) (assoc-in pv p)))))))
