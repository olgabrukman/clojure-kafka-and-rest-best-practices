Producer Design Details
----

Producer accepts POST messages and puts the messages into inner kafka channel.
Then producer has a thread pool of predefined size (8) reading from the inner channel and then writing all messages into provided output *[kafka channel, topic]* with gzip compression.

Implementation Details 
===
Producer is implemented using components. 
1. [Some info about components](https://www.youtube.com/watch?v=13cmHf_kt-Q). 
2. [The components tutorial on slides](https://cb.codes/a-tutorial-of-stuart-sierras-component-for-clojure/). core.clj is the main method.

How to record metrics for discrete variables, e.g. ,queue sizes:

  (def active-count ["http" "worker-pool" "active-count"]) 
  (def pool-size ["http" "worker-pool" "pool-size"]) 
  (def task-count ["http" "worker-pool" "task-count"]) 
  (def completed-task-count ["http" "worker-pool" "completed-task-count"])

(defn- instrument-worker-pool [^ThreadPoolExecutor worker-pool] 
(at-at/every 1000 
   #(do (gauges/gauge-fn active-count (fn [] (.getActiveCount worker-pool))) 
        (gauges/gauge-fn pool-size (fn [] (.getPoolSize worker-pool))) 
        (gauges/gauge-fn task-count (fn [] (.getTaskCount worker-pool))) 
        (gauges/gauge-fn completed-task-count (fn [] (.getCompletedTaskCount worker-pool)))) (at-at/mk-pool)))`

Consumer Design Details
----
Thread pool of size min (10, # of partitions) reading from the channel. Handling a message includes updating atomic data structure and pushing the message to an inner channel for further processing. __Note:__ maximal number of thread for reading from a kafka channel equals to a number of partitions in the kafka cluster.

We start thread pool of predefined size (8 threads) to read from inner channel and send asynchronous REST calls to a producer side. Number of concurrent REST calls is limited by a predefined number to avoid denial-of-service behaviour of the producer side REST endpoint.

In order to see Kafka statistics in Graphana we add dependency to com.simple/kafka-dropwizard-reporter "1.1.1" (while excluding collisions section dependencies, see project.clj )

Implementation details: Consumer project is implemented using states as building blocks. core.clj contains the main method that starts all states in order that is defined by by a programmer (the order is not computed by clojure).

Useful links: http://danboykis.com/posts/things-i-wish-i-knew-about-core-async/
