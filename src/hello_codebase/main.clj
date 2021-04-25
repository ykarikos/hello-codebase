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
(def location-url-prefix "https://www.metaweather.com/api/location/search/?query=")

(defn- http-get [url]
  (-> @(http/get url)
      :body
      bs/to-string
      (j/read-value (j/object-mapper {:decode-key-fn keyword}))))

(defn- weather-url [location]
  (let [now (OffsetDateTime/now)]
    (str weather-url-prefix location
         "/" (.getYear now)
         "/" (.getMonthValue now)
         "/" (.getDayOfMonth now))))

(defn- get-temperature [city-id]
  (let [response (http-get (weather-url city-id))
        temperature-sum (->> response
                             (map :the_temp)
                             (reduce +))]
    (/ temperature-sum (count response))))

(defn- get-city-id [city]
  (-> (str location-url-prefix city)
      http-get
      first
      :woeid))

(defn- weather-handler [{:keys [path-params]}]
  (let [city (:city path-params)
        city-id (get-city-id city)]
    {:status 200
     :body {:city city
            :average-temperature (get-temperature city-id)}}))

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
