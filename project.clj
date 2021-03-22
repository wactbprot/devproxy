(defproject devproxy "0.8.0"
  :description "device proxy"
  :url "https://github.com/wactbprot/devproxy"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure                  "1.10.2"]
                 [org.clojure/tools.cli                "1.0.194"]
                 [com.taoensso/carmine                 "3.0.0"]
                 [compojure                            "1.6.1"]
                 [http-kit                             "2.5.0"]
                 [ring/ring-defaults                   "0.3.2"]
                 [ring/ring-core                       "1.7.1"]
                 [ring/ring-devel                      "1.7.1"]
                 [ring/ring-json                       "0.5.0"]
                 [hiccup                               "1.0.5"]
                 [cheshire                             "5.10.0"]
                 [org.clojars.wactbprot/vl-data-insert "0.2.0"]
                 [com.brunobonacci/mulog               "0.6.0"]
                 [com.brunobonacci/mulog-elasticsearch "0.6.0"]
                 [com.ashafa/clutch                    "0.4.0"]
                 ]
  :plugins [[lein-ring "0.12.5"]
            [lein-codox  "0.10.7"]]
  :codox {:metadata {:doc/format :markdown}
          :source-uri "https://github.com/wactbprot/devproxy/blob/master/{filepath}#L{line}"}
  :ring {:handler devproxy.server/app}
  :resource-paths ["resources"]
  :repl-options {:init-ns devproxy.server}
  :main devproxy.server
  :aot [devproxy.server]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
