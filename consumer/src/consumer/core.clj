(ns consumer.core
  (:require
    [mount.core :refer [defstate start stop]]
    [consumer.config :refer [config]]
    [consumer.edge :refer [edge]]
    [consumer.kafka-consumer :refer [kafka-consumer]]
    [consumer.metrics :refer [metrics]]
    [consumer.media-source-store :refer [store]]
    [consumer.message-handler :refer [message-handler]])
  (:gen-class))


(def desired-states
  (vector
    #'config
    #'store
    #'metrics
    #'edge
    #'message-handler
    #'kafka-consumer))


(defn -main
  "Web server entry point."
  [& args]
  (println "Consumer here! Hello, World!")
  (doseq [s desired-states] (start s)))



