(ns producer.system
  (:require [com.stuartsierra.component :as component]
            [producer.edge-component :as edge_component]
            [producer.producer-component :as producer-component]
            [clojure.core.async :refer [chan]]
            [producer.metrics-component :as metrics-component]
            [producer.postback-handler-component :as postback-handler-component]))


(defn system [settings]
  (component/system-map
    :input-chan (chan)
    :edge_to_producer_chan (chan)
    :metrics (component/using
               (metrics-component/metrics) [])
    :postback-handler (component/using
                        (postback-handler-component/postback-handler)
                        [:edge_to_producer_chan :metrics])
    :edge_component (component/using
                      (edge_component/http-server settings)
                      [:postback-handler :metrics])
    :producer_component (component/using
                          (producer-component/producer-component settings)
                          [:edge_to_producer_chan :metrics])))
