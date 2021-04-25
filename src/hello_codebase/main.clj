(ns hello-codebase.main
  (:gen-class)
  (:require [aleph.http :as http]
            [reitit.ring :as ring]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [byte-streams :as bs]
            [jsonista.core :as j]))

;; Handlers

(defn home-handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello world!"})

;; Routes and middleware

(def routes
  [["/" {:get {:handler home-handler}}]])

(def ring-opts
  {:data
   {:muuntaja m/instance
    :middleware [muuntaja/format-middleware]}})

(def app
  (ring/ring-handler
   (ring/router routes ring-opts)))

;; Web server

(defonce server (atom nil))

(def port
  (-> (System/getenv "PORT")
      (or "3000")
      (Integer/parseInt)))

(defn start-server []
  (println "Starting server on port" port)
  (reset! server (http/start-server #'app {:port port})))

(defn stop-server []
  (when @server
    (.close ^java.io.Closeable @server)))

(defn restart-server []
  (stop-server)
  (start-server))

;; Application entrypoint

(defn -main [& args]
  (println (format "Starting webserver on port: %s." port))
  (start-server))
