(ns hello-codebase.weather
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [jsonista.core :as j])
  (:import [java.time OffsetDateTime]))


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

(defn get-temperature [city-id]
  (let [response (http-get (weather-url city-id))
        timestamps (->> response
                        (map :created response)
                        sort)
        temperature-sum (->> response
                             (map :the_temp)
                             (reduce +))]
    {:average-temperature (/ temperature-sum (count response))
     :starting (first timestamps)
     :ending (last timestamps)}))

(defn get-city-id [city]
  (-> (str location-url-prefix city)
      http-get
      first
      :woeid))

;(def weather-id-helsinki (get-weather-id "helsinki"))
;(def response-helsinki (http-get (weather-url weather-id-helsinki)))
