(ns producer.edge-component
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :as server]
            [compojure.route :refer [files not-found]]
            [compojure.core :refer [routes GET POST DELETE ANY context]]
            [metrics.counters :refer [counter inc!]]
            [metrics.timers :refer [timer time!]]
            [metrics.meters :refer [meter mark!]]
            [custom-metrics-ng.core :refer :all]
            [cheshire.core]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.logger.timbre :refer [wrap-with-logger]]
            [compojure.handler :refer [site]]
            [clojure.core.async :as async]
            [taoensso.timbre :refer [error debug info]]
            [producer.metrics-component]
            [taoensso.timbre :as timbre]))


(def ok-status
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "ok"})

(def show-landing-page
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello World!"})



(defn create-routes [postback-handler]
  (routes
    (GET "/" [] show-landing-page)
    (GET "/healthcheck" [] ok-status)
    (POST "/message" [:as req] (postback-handler req))))



(defrecord HttpServer [settings postback-handler]
  component/Lifecycle
  (start [this]
    (assoc this :server (server/run-server
                          (create-routes  (:postback-engine postback-handler))
                          {:port       (:edge-port settings)
                           :thread     (:postback-threads settings)
                           :queue-size (:postback-buffer-size settings)})))
  (stop [this]
    (when-let [s (:server this)]
      (s :timeout 100)
      (dissoc this :server))))


(defn http-server [settings]
  (map->HttpServer {:settings settings}))

