(ns consumer.kafka-consumer
  (:require
    [mount.core :refer [defstate start]]
    [custom-kafka10.consumer :as afc :refer [consumer string-deserializer subscribe-to-topics messages]]
    [consumer.config :refer [config]]
    [consumer.edge :refer [edge]]
    [cheshire.core]
    [metrics.counters :refer [counter inc!]]
    [metrics.meters :refer [meter mark!]]
    [custom-metrics-ng.core :refer :all]
    [taoensso.timbre :refer [info debug error]]
    [consumer.metrics]
    [consumer.media-source-store]
    [taoensso.timbre :as timbre]
    [overtone.at-at :as at-at]
    [consumer.message-handler :refer [message-handler]]))

(defn consumer-config [id]
  (let [curr-config {"bootstrap.servers"                     (str (:kafka-server config) ":" (:kafka-port config))
                     "enable.auto.commit"                    "false"
                     "auto.offset.reset"                     "earliest"
                     "group.id"                              (:kafka-consumer-group-id config)
                     "metric.reporters"                      (:metric-reporters config)
                     "client.id"                             (str id)}
        ]
    curr-config))

(defn find-workers-number []
  (try
    (let [c (consumer (consumer-config 0) (string-deserializer) (string-deserializer))
          partitions_number (count (afc/list-all-partitions c "matches"))
          consumer-workers (:consumer-workers config)
          workers-num (min partitions_number consumer-workers)]
      (timbre/info "Number of  workers:" workers-num)
      workers-num)
    (catch Throwable e                                      ; Exception e
      (error e))))



(defn consume-message [worker-index]
  (try
    (let [c (consumer (consumer-config worker-index) (string-deserializer) (string-deserializer))]
      (try
        (afc/subscribe-to-topics c (:kafka-topic config))
        ;reading some to get the channel warm
        (messages c)
        (debug "Assignments after warmup:" (afc/subscribed-partitions c) worker-index)
        (afc/seek c [] :beginning)

        (debug "Consumer thread ", worker-index, " starting its loop")
        (while true
          (doseq [msg (messages c)]
            ;(debug "Consumer " worker-index "got message " msg)
            (message-handler msg)))
        (catch Throwable e                                  ;Exception e
          (timbre/error e))
        (finally
          (timbre/debug "Finished consumer future for worker " worker-index))))
    (catch Throwable e                                      ;Exception e
      (timbre/error e))))


(defn start-consumer []
  (future
    (let [workers-num (find-workers-number)]
      (dotimes [i workers-num]
        (future (consume-message i))))
    (timbre/debug "Created all consumer workers")))


(defstate kafka-consumer :start (start-consumer))
