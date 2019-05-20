Consumer project design:

1. Thread pool of size min (10, # of partitions) reading from the channel. Handling a message 
includes updating atomic data structure, and pushing the message to an inner channel for further 
processing. Note:  maximal number of thread for reading from a kafka channel equals to a number 
of partitions in the kafka cluster. 
           
2. We start thread pool of predefined size (8) to read from inner channel and send asynchronous 
REST calls to a producer side. Number of concurrent REST calls is limited by a predefined number 
to avoid denial-of-service behaviour of the producer side REST endpoint.
  
3. In order to see Kafka statistics in Graphana we add dependency to 
com.simple/kafka-dropwizard-reporter "1.1.1" (while excluding collisions section dependencies, 
see project.clj ) 

Implementation details:
Consumer project is implemented using states as building blocks. core.clj contains the main method that
starts all states in order that is defined by by a programmer (the order is not computed by clojure).

Useful links:

http://danboykis.com/posts/things-i-wish-i-knew-about-core-async/

              