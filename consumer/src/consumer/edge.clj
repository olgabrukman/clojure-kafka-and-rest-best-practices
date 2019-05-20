(ns consumer.edge
  (:require
    [org.httpkit.client :as client]
    [org.httpkit.server :as server]
    [compojure.route :refer [files not-found]]
    [compojure.core :refer [defroutes GET POST DELETE ANY context]]
    [metrics.meters :refer [meter mark!]]
    [metrics.counters :refer [counter inc!]]
    [custom-metrics-ng.core :refer :all]
    [clojure.core.async :refer [go-loop <! >! <!! >!! chan]]
    [mount.core :refer [defstate start]]
    [cheshire.core]
    [consumer.config :refer [config]]
    [consumer.util]
    [taoensso.timbre :as timbre :refer [info debug]]
    [consumer.metrics]
    [consumer.util]
    [consumer.media-source-store :refer [media-source-store]]))



(def ok-status
  {:status  200
   :headers {"Content-Type" "text/html"}})

(defn show-landing-page [req]
  (do
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    "Hello World!"}))

(defn record-responses-stats
  [{:keys [status headers error body]}]
  (mark! consumer.metrics/postbacks-sent)
  (debug "Processing POST request results, status " status)
  (cond
    error (timbre/error error)
    (nil? status) (do (mark! consumer.metrics/postbacks-errors)
                      (timbre/error "Error postback, status is nil.\n"))
    (and (<= 200 status) (< status 300)) (mark! consumer.metrics/postbacks-2XX)
    (and (<= 400 status) (< status 500)) (mark! consumer.metrics/postbacks-4XX)
    (and (<= 500 status) (< status 600)) (mark! consumer.metrics/postbacks-5XX)
    ))


; Using semaphore implies that there can be only semaphore.counter concurrent post requests
(defn send-postback-request
  ([config msg]
   (metrics.counters/inc! consumer.metrics/inflight-postbacks)
   (metrics.meters/mark! consumer.metrics/postbacks-inited)
   (let [res (client/post (:postback-url config)
                          {:timeout (:postback-timeout-ms config)
                           :headers {"Content-Type" "application/json; charset=utf-8"}
                           :body    (:value msg)}
                          (fn [res]
                            (record-responses-stats res)))]))
  ([config msg semaphore]
    (.acquire semaphore)
    (metrics.counters/inc! consumer.metrics/inflight-postbacks)
    (metrics.meters/mark! consumer.metrics/postbacks-inited)
    (let [res (client/post (:postback-url config)
                           {:timeout (:postback-timeout-ms config)
                            :headers {"Content-Type" "application/json; charset=utf-8"}
                            :body    (:value msg)}
                           (fn [res]
                             (.release semaphore)
                             (record-responses-stats res)))])))


(defroutes all-routes
           (GET "/" [] show-landing-page)
           (GET "/healthcheck" [] ok-status)
           (GET "/media-source" [:as req] (consumer.media-source-store/get-all-media-sources))
           (GET "/media-source/:name" [name :as req] (consumer.media-source-store/get-all-media-sources name))
           (not-found "<p>Page not found.</p>"))            ;; all other, return 404

(defn create-server []
  (server/run-server all-routes {:port (:edge-port config) :thread 8})
  (debug "Edge has started"))

(defstate edge :start (create-server))


