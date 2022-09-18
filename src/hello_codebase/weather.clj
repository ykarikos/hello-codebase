(ns hello-codebase.weather
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [jsonista.core :as j]))

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

(defn get-temperature [location]
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

