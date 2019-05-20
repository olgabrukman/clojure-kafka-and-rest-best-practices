(ns producer.metrics-component
  (:require [com.stuartsierra.component :as component]
            [metrics.counters :refer [counter inc!]]
            [metrics.timers :refer [timer time!]]
            [metrics.meters :refer [meter mark!]]
            [taoensso.timbre :refer [error info]]
            [custom-metrics-ng.core]))


(def messages-received (metrics.meters/meter "messages-received"))
(def messages-produced (metrics.meters/meter "messages-produced"))
(def message-errors (metrics.meters/meter "message-errors"))
(def message-exceptions (metrics.meters/meter "message-exceptions"))





(defrecord Metrics []
  component/Lifecycle
  (start [this]
    (do
      (assoc this :metrics (custom-metrics-ng.core/setup-metrics))
      this))
  (stop [this]
    (when-let [s (:metrics this)]
      (s :timeout 100)
      (dissoc this :metrics))
    this))


(defn metrics []
  (map->Metrics []))




