Producer design details:
1. Producer accepts POST messages and puts the messages into inner kafka channel.
2. Then producer has a thread pool of predefined size (8) reading from the inner channel and 
then writing all messages into provided output [kafka channel, topic] with gzip compression 

Implementation details:
Producer is implemented using components. core.clj is main. 
Some info about components: https://www.youtube.com/watch?v=13cmHf_kt-Q

This tutorial has the code covered in the above lecture: 
https://cb.codes/a-tutorial-of-stuart-sierras-component-for-clojure/

How to record metrics for discrete variables, e.g. ,queue sizes:

(def active-count ["http" "worker-pool" "active-count"])
(def pool-size ["http" "worker-pool" "pool-size"])
(def task-count ["http" "worker-pool" "task-count"])
(def completed-task-count ["http" "worker-pool" "completed-task-count"])

(defn- instrument-worker-pool [^ThreadPoolExecutor worker-pool]
  (at-at/every 1000 #(do
                       (gauges/gauge-fn active-count (fn [] (.getActiveCount worker-pool)))
                       (gauges/gauge-fn pool-size (fn [] (.getPoolSize worker-pool)))
                       (gauges/gauge-fn task-count (fn [] (.getTaskCount worker-pool)))
                       (gauges/gauge-fn completed-task-count (fn [] (.getCompletedTaskCount worker-pool))))
               (at-at/mk-pool)))


