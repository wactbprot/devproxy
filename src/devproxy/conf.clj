(ns devproxy.conf
  (:require [clojure.edn :as edn]))

(defn config
  "Reads a `edn` configuration in file `f`." 
  ([]
   (config "resources/conf.edn"))
  ([f]
   (-> f slurp edn/read-string)))

(defn couch-conn
  [c]
  (let [c (:couch c)
        usr (System/getenv "CAL_USR")
        pwd (System/getenv "CAL_PWD")]
    (str (:prot c)"://" (if (and usr pwd) (str usr":"pwd"@")  "") (:srv c)":"(:port c) "/" (:db c))))
