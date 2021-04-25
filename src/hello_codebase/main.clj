(ns hello-codebase.main
  (:gen-class)
  (:require
   [aleph.http :as http]
   [reitit.ring :as ring]
   [ring.middleware.defaults :as defaults]))

;; Handlers

(defn home-handler [request]
  {:status 200
   :body "Hello world!"})

;; Routes and middleware

(def routes
  [["/" {:get {:handler home-handler}}]])

(def ring-opts
  {:data
   {:middleware
    [[defaults/wrap-defaults defaults/api-defaults]]}})

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
