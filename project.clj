(defproject aoc "0.1.0-SNAPSHOT"
  :description "device proxy"
  :url "https://github.com/wactbprot/aoc"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [http-kit "2.5.0"]
                 [hiccup "1.0.5"]
                 [com.ashafa/clutch "0.4.0"]
                 [org.clojure/data.json "1.0.0"]
                 [org.clojure/tools.logging "1.1.0"]
                 ]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler aoc.handler/app})
