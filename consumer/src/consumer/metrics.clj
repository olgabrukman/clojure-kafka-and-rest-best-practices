(ns consumer.metrics
  (:require [custom-metrics-ng.core]
            [mount.core :refer [defstate start stop]]
            [metrics.core]
            [metrics.counters]
            [metrics.meters]
            [metrics.gauges]
            [taoensso.timbre :as timbre]))


(def messages-consumed (metrics.meters/meter "messages-consumed"))
(def   (metrics.meters/meter "media-source-errors"))
(def inflight-postbacks (metrics.counters/counter "inflight-postbacks"))
(def postbacks-inited (metrics.meters/meter "postbacks-inited"))
(def postbacks-sent (metrics.meters/meter "postbacks-sent"))
(def postbacks-errors (metrics.meters/meter "postback-errors"))
(def postback-exceptions (metrics.meters/meter "postback-exceptions"))
(def postbacks-2XX (metrics.meters/meter "postbacks-2XX"))
(def postbacks-4XX (metrics.meters/meter "postbacks-4XX"))
(def postbacks-5XX (metrics.meters/meter "postbacks-5XX"))


(defn create-metrics []
  (custom-metrics-ng.core/setup-metrics))

(defstate metrics :start (create-metrics))
