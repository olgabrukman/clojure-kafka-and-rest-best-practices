(ns consumer.message-handler
  (:require
    [mount.core :refer [defstate start]]
    [custom-kafka10.consumer :refer [consumer string-deserializer subscribe-to-topics messages]]
    [consumer.config :refer [config]]
    [consumer.edge :refer [edge]]
    [cheshire.core]
    [metrics.counters :refer [counter inc!]]
    [metrics.meters :refer [meter mark!]]
    [custom-metrics-ng.core :refer :all]
    [clojure.core.async :as async]
    [taoensso.timbre :refer [info debug error]]
    [consumer.metrics :as metrics]
    [consumer.media-source-store]
    [taoensso.timbre :refer [info debug error]]
    [consumer.config :refer [config]]))



(defn handle
  ([msg]
  (try
    (consumer.media-source-store/count-media-source msg)
    (catch Throwable e;Exception e
      (metrics.meters/mark! consumer.metrics/media-source-errors)
      (error e "Exception counting media-source")))
  (mark! consumer.metrics/messages-consumed)
  (try
    (consumer.edge/send-postback-request config msg)
    (inc! consumer.metrics/inflight-postbacks)
    (catch Throwable e;Exception e
      (metrics.meters/mark! consumer.metrics/postback-exceptions)
      (error e "Exception sending postback"))))

  ([msg semaphore]
  (try
    (consumer.media-source-store/count-media-source msg)
    (catch Exception e
      (metrics.meters/mark! consumer.metrics/media-source-errors)
      (error e "Exception counting media-source")))
  (mark! consumer.metrics/messages-consumed)
  (try
    (consumer.edge/send-postback-request config msg semaphore)
    (inc! consumer.metrics/inflight-postbacks)
    (catch Exception e
      (metrics.meters/mark! consumer.metrics/postback-exceptions)
      (error e "Exception sending postback")))))


;We start a thread pool of workers that read from a channel and send postbacks to the producer.
;As we do not want to bombard the producer with REST calls, causing DoS, we use semaphore to limit
;the number of concurrent REST calls.
(defn start-message-handling-background-thread-pool
  ([thread-num c]
  (dotimes [i thread-num]
    (future
      (loop [msg (async/<!! c)]
        (when msg
          (handle msg)
          (recur (async/<!! c)))))))
  ([thread-num c semaphore]
  (dotimes [i thread-num]
    (future
      (loop [msg (async/<!! c)]
        (when msg
          (handle msg semaphore)
          (recur (async/<!! c))))))))

;message handling is only writing the message into a channle
(defn message-handling-fn
  [c]
  (fn [msg]
    (async/>!! c msg)))

;The  reason those 2 methods are in the same methods is to share same variables,
; thus I couldn't separate those 2 methods
;This method is invoked exactly once, returning a function.
(defn start-message-handler [config]
  (let [thread-num (:match-handler-workers config 10)
        c (async/chan (async/buffer (:match-handler-buffer-size config)))
        semaphore (java.util.concurrent.Semaphore. (:max-inflight-postbacks config))]
    (start-message-handling-background-thread-pool thread-num c semaphore)
    ;(start-message-handling-background-thread-pool thread-num c)
    (debug "Message handler has started")
    (message-handling-fn c)))


;Every time the state is accessed, the returned value is the message-handling-fn
(defstate message-handler :start (start-message-handler config))
