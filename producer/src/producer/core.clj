(ns producer.core
  (:require
    [com.stuartsierra.component :as component]
    [producer.system :refer [system]]
    [taoensso.timbre :refer [info]]
    [clojure.java.io :as io]
    [cheshire.core :as json])
  (:gen-class))


(defrecord HealthCheckServerWrapper [server]
  component/Lifecycle
  (start [this] this)
  (stop [this]
    (component/stop server)))

(defn keyword-transform []
  (fn [new-map [key val]]
    (assoc new-map (keyword key) val)))

(defn load-config
  "load a config file as defined in resources/config.json, transform key strings into keywords & return the resulting map "
  []
  (let [string-map (json/parse-string (slurp (io/resource "config.json")))
        new-map (reduce (keyword-transform) {} string-map)]
    new-map))



(defn -main
  [& args]
  (info "Starting producer service! Hello World")
  (let [settings (load-config)
        system (component/start (system settings))]
    (info "producer service is up on port" (:edge-port settings))
    system))

