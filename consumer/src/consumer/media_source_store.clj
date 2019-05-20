(ns consumer.media-source-store
  (:require
    [mount.core :as mount :refer[defstate start]]
    [cheshire.core]
    [taoensso.timbre :refer [info debug error]]))

(def store (atom {}))


(defn update! [media-source-name]
  (if (contains? @store media-source-name)
    (swap! store update-in [media-source-name] inc)
    (swap! store conj [media-source-name 1]))
  ;(debug "Store state "@store)
  )


(defn get-all-media-sources
  ([]
   @store)
  ([name]
   ((keyword name) @store)))


(defn extract-media-source [message]
  (let [msg (cheshire.core/parse-string (:value message))
        source (get msg "source")
        pid (get-in msg ["attribution" "pid"])
        media-source (consumer.util/get-media-source source pid)]
    media-source))




(defn count-media-source [msg-record]
  (let [media-source (extract-media-source msg-record)]
    (update! media-source)))


(mount/defstate media-source-store
                :start store)
