(ns producer.postback-handler-component
  (:require [clojure.core.async :as asycn]
            [producer.producer-component]
            [metrics.meters :refer [meter mark!]]
            [taoensso.timbre :refer [error info]]
            [producer.metrics-component :as metrics]
            [com.stuartsierra.component :as component]))

(def ok-status
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "ok"})


(defn postback-handling-fn
  [edge_to_producer_chan]
  (fn [req ]
    (mark! metrics/messages-received)
    (let [body (ring.util.request/body-string req)]
      (asycn/>!! edge_to_producer_chan body)
      ok-status)))

(defrecord PostbackHandler [edge_to_producer_chan metrics]
  component/Lifecycle
  (start [this]
    (assoc this :postback-engine (postback-handling-fn  edge_to_producer_chan)))
  (stop [this]
    (when-let [s (:postback-engine this)]
      (s :timeout 100)
      (dissoc this :postback-engine))
    this))


(defn postback-handler []
  (map->PostbackHandler {}))