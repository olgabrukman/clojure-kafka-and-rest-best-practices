(ns consumer.config
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [mount.core :refer [defstate start]]))

(defn keyword-transform []
  (fn [new-map [key val]]
    (assoc new-map (keyword key) val)))

(defn load-config
  "load a config file as defined in resources/config.json, transform key strings into keywords & return the resulting map "
  []
  (let [string-map (json/parse-string (slurp (io/resource "config.json")))
    new-map (reduce (keyword-transform) {} string-map)]
    new-map))

(defstate config :start (load-config))


