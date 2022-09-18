(ns hello-codebase.weather
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [jsonista.core :as j])
  (:import [java.time OffsetDateTime]))

; https://open-meteo.com/en/docs
; https://open-meteo.com/en/docs/geocoding-api
(def weather-url-prefix "https://api.open-meteo.com/v1/forecast?hourly=temperature_2m&")
(def geocoding-url-prefix "https://geocoding-api.open-meteo.com/v1/search?name=")

(defn- http-get [url]
  (-> @(http/get url)
      :body
      bs/to-string
      (j/read-value (j/object-mapper {:decode-key-fn keyword}))))

(defn- weather-url [{:keys [latitude longitude]}]
  (str weather-url-prefix
       "latitude=" latitude "&"
       "longitude=" longitude))

(defn get-avg-temperature [location]
  (let [hourly-data (-> (weather-url location)
                        http-get
                        :hourly)
        timestamps (->> hourly-data
                        :time
                        sort)
        temperature-sum (->> hourly-data
                             :temperature_2m
                             (reduce +))]
    {:average-temperature (/ temperature-sum (count (:temperature_2m hourly-data)))
     :starting (first timestamps)
     :ending (last timestamps)
     :location (:name location)
     :country (:country_code location)
     :elevation (:elevation location)}))

(defn get-location [city]
  (-> (str geocoding-url-prefix city)
      http-get
      :results
      first))
