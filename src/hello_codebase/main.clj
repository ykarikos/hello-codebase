(ns hello-codebase.main
  (:gen-class)
  (:require [aleph.http :as http]
            [reitit.ring :as ring]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [hello-codebase.weather :as weather])
  (:import [java.time OffsetDateTime]))

;; Handlers

(defn home-handler [{:keys [params]}]
  (let [friend (get params "friend")]
    {:status 200
     :headers {"Content-Type" "text/plain; charset=utf-8"}
     :body (str "Hello Aurajoki Overflow"
                (when friend
                  (str " and " friend))
                "!\n\n"
                (OffsetDateTime/now))}))


(defn- weather-handler [{:keys [path-params]}]
  (let [city (:city path-params)
        location (weather/get-location city)]
    (if location
      {:status 200
       :body (assoc (weather/get-temperature location)
                    :city city)}
      {:status 404
       :body {:error (str "City " city " not found :-(")}})))

;; Routes and middleware

(def routes
  [["/" {:get {:handler home-handler}}]
   ["/api/weather/:city" {:get {:handler weather-handler}}]])

(def ring-opts
  {:data
   {:muuntaja m/instance
    :middleware [parameters/parameters-middleware
                 muuntaja/format-middleware]}})

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
