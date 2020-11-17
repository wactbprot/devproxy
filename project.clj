(defproject aoc "0.3.0"
  :description "device proxy"
  :url "https://github.com/wactbprot/aoc"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [http-kit "2.5.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-devel "1.7.1"]
                 [ring/ring-json "0.5.0"]
                 [hiccup "1.0.5"]
                 [cheshire "5.10.0"]
                 [org.clojars.wactbprot/vl-data-insert "0.2.0"]
                 [com.taoensso/carmine "3.0.0"]
                 [com.ashafa/clutch "0.4.0"]
                 [org.clojure/data.json "1.0.0"]
                 [org.clojure/tools.logging "1.1.0"]
                 [clojang/codox-theme "0.2.0-SNAPSHOT"]
                 ]
  :plugins [[lein-ring "0.12.5"]
            [lein-codox  "0.10.7"]]
  :codox {:themes [:clojang]
          :metadata {:doc/format :markdown}
          :source-uri "https://github.com/wactbprot/aoc/blob/master/{filepath}#L{line}"}
  :main ^:skip-aot aoc.server
  :ring {:handler aoc.server/app})
