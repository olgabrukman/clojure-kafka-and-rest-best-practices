(ns producer.producer-component
  (:require
    [com.stuartsierra.component :as component]
    [custom-kafka10.producer :refer [producer byte-array-serializer record send]]
    [custom-kafka10.consumer :refer [consumer string-deserializer subscribe-to-topics messages]]
    [cheshire.core]
    [taoensso.timbre :refer [debug info warn error spy]]
    [clojure.core.async :refer [>!! <!! go buffer close! thread alts! alts!! timeout]]
    [metrics.meters :refer [meter mark!]]
    [custom-metrics-ng.core :refer :all]
    [producer.metrics-component :as metrics]))



(defn on-deliver [metadata error]
  (if error
    (do
      (mark! metrics/message-errors)
      (debug "On deliver error: " error))
    (mark! metrics/messages-produced)))

(defn- init-kafka-producer
  [settings edge_to_producer_chan]
  (let [producer-config {"bootstrap.servers"                     (str (:kafka-server settings) ":" (:kafka-port settings))
                         "metric.reporters"                      (:metric-reporters settings)
                         "acks"                                  (:acks settings)
                         "buffer.memory"                         (:buffer-memory settings)
                         "retries"                               (:retries settings)
                         "max.in.flight.requests.per.connection" (:max-in-flight-requests-per-connection settings)
                         "batch.size"                            (:batch-size settings)
                         "max.block.ms"                          (:max-block-ms settings)
                         "max.request.size"                      (:max-request-size settings)
                         "linger.ms"                             (:linger-ms settings)
                         "request.timeout.ms"                    (:request-timeout-ms settings)
                         "compression.type"                      (:compression-type settings)
                         "send.buffer.bytes"                     (:send-buffer-bytes settings)

                         }
        producer (producer producer-config (byte-array-serializer) (byte-array-serializer))]
    (dotimes [n (:kafka-producer-threads settings)]
      (future
        (let [thread-name (str "kafka-producer-thread-" n)]
          (.setName (Thread/currentThread) thread-name)
          (while true
            (try
              (let [msg (<!! edge_to_producer_chan)
                    kafka-msg (record (:kafka-topic settings) (.getBytes (cheshire.core/generate-string msg)))]
                (send producer kafka-msg (fn [metadata error] (on-deliver metadata error))))
              (catch Throwable t
                (do (mark! metrics/message-errors)
                    (error t (str thread-name " died!!!!")))))))))))

(defrecord Producer [settings edge_to_producer_chan]
  component/Lifecycle
  (start [this]
    (init-kafka-producer settings edge_to_producer_chan)
    this))


(defn producer-component [settings]
  (map->Producer {:settings settings}))

