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
     :body (str "Hello Aurajoki Overflow"
                (when friend
                  (str " and " friend))
                "!\n\n"
                (OffsetDateTime/now))}))

; https://open-meteo.com/en/docs
(def weather-url-prefix "https://api.open-meteo.com/v1/forecast?hourly=temperature_2m&")
; https://open-meteo.com/en/docs/geocoding-api
(def geocoding-url-prefix "https://geocoding-api.open-meteo.com/v1/search?name=")

(defn- api-get [url]
  (-> @(http/get url)
      :body
      bs/to-string
      (j/read-value (j/object-mapper {:decode-key-fn keyword}))))

(defn- weather-url [{:keys [latitude longitude]}]
  (str weather-url-prefix
       "latitude=" latitude "&"
       "longitude=" longitude))

(defn- get-temperature [location]
  (let [response (api-get (weather-url location))
        timestamps (->> response
                        :hourly
                        :time
                        sort)
        temperatures (->> response
                          :hourly
                          :temperature_2m)]
    {:average-temperature (/ (reduce + temperatures) (count temperatures))
     :starting (first timestamps)
     :ending (last timestamps)}))

(defn get-location [city]
  (-> (str geocoding-url-prefix city)
      api-get
      :results
      first))

(defn- weather-handler [{:keys [path-params]}]
  (let [city (:city path-params)
        location (get-location city)]
    (if location
      {:status 200
       :body (assoc (get-temperature location)
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
