(defproject hello-codebase "0.0.1-SNAPSHOT"
  :description "Simple webapp"
  :url ""
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [metosin/reitit "0.5.5"]
                 [ring "1.8.1"]
                 [ring/ring-defaults "0.3.2"]
                 [aleph "0.4.7-alpha5"]]
  :main ^:skip-aot hello-codebase.main
  :profiles {:uberjar {:aot :all
                       :uberjar-name "hello-codebase.jar"}})
