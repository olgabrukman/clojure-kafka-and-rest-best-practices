(ns consumer.util
  (:require
    [clojure.string]))



(defn get-media-source [source pid]
  (case (some-> source (clojure.string/lower-case))
    "twitter" "twitter"
    "facebook" "facebook"
    pid))



