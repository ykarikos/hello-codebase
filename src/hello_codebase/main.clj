(ns hello-codebase.main
  (:gen-class)
  (:require [aleph.http :as http]
            [reitit.ring :as ring]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [byte-streams :as bs]
            [jsonista.core :as j])
  (:import [java.time OffsetDateTime]))

;; Handlers

(defn home-handler [{:keys [params]}]
  (let [friend (get params "friend")]
    {:status 200
     :headers {"Content-Type" "text/plain; charset=utf-8"}
     :body (str "Hello codebase"
                (when friend
                  (str " and " friend))
                "!\n"
                (OffsetDateTime/now))}))

; https://www.metaweather.com/api/
(def weather-url-prefix "https://www.metaweather.com/api/location/")

(defn- weather-url [location]
  (let [now (OffsetDateTime/now)]
    (str weather-url-prefix location
         "/" (.getYear now)
         "/" (.getMonthValue now)
         "/" (.getDayOfMonth now))))

(defn- get-temperature []
  (let [response (-> @(http/get (weather-url "565346")) ; Helsinki weoeid = 565346
                     :body
                     bs/to-string
                     (j/read-value (j/object-mapper {:decode-key-fn keyword})))
        temperature-sum (->> response
                             (map :the_temp)
                             (reduce +))]
    (/ temperature-sum (count response))))

(defn- weather-handler [_]
  {:status 200
   :body {:average-temperature (get-temperature)}})

;; Routes and middleware

(def routes
  [["/" {:get {:handler home-handler}}]
   ["/api/weather" {:get {:handler weather-handler}}]])

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
