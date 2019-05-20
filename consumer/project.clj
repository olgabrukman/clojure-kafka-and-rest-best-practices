(defproject consumer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[s3-wagon-private "1.3.1" :exclusions [org.apache.httpcomponents/httpclient]]]
  :repositories [["releases" {:url "https://artifactory.company.com/artifactory/maven/"}]]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/tools.namespace "0.3.0-alpha4"]
                 [custom-kafka10 "1.5.0" :exclusions [org.slf4j/slf4j-log4j12]]
                 [overtone/at-at "1.2.0"]
                 [com.company/custom-metrics-ng "1.0.9"]
                 [mount "0.1.16"]
                 [tolitius/mount-up "0.1.2"]
                 [danlentz/clj-uuid "0.1.7"]
                 [cheshire "5.8.1"]
                 [compojure "1.6.1"]
                 [http-kit "2.3.0"]
                 [aero "1.1.3"]
                 [com.simple/kafka-dropwizard-reporter "1.1.1"]
                 ; Logging
                 [com.taoensso/timbre "4.10.0"]
                 [timbre-ns-pattern-level "0.1.2"]
                 [com.fzakaria/slf4j-timbre "0.3.12"]
                 [org.slf4j/slf4j-api "1.7.26"]
                 [org.slf4j/log4j-over-slf4j "1.7.26"]
                 [org.slf4j/jul-to-slf4j "1.7.26"]
                 [org.slf4j/jcl-over-slf4j "1.7.26"]
                 ; Avoid collisions:
                 [org.clojure/tools.reader "1.0.0-beta4"]
                 [commons-codec "1.12"]
                 [com.fasterxml.jackson.core/jackson-core "2.9.7"]
                 [overtone/at-at "1.2.0"]
                 [io.dropwizard.metrics/metrics-core "3.2.2"]]
  :global-vars {*warn-on-reflection* false}
  :main consumer.core
  :profiles {:uberjar {:aot :all}})

