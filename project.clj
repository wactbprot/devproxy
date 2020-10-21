(defproject aoc "0.1.0-SNAPSHOT"
  :description "device proxy"
  :url "https://github.com/wactbprot/aoc"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [ring/ring-json "0.5.0"]
                 [ring/ring-devel "1.7.1"]
                 [hiccup "1.0.5"]
                 [com.ashafa/clutch "0.4.0"]
                 ]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler aoc.handler/app})
