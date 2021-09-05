(ns core
  (:require [clojure.data.xml :as xml]
            [clojure.core.async :refer [go go-loop chan timeout close! <! <!! >!! >!]]))

(defn fill-chan-concurrently
  "creates and returns a channel then fills the channel with :a, :b, :c, :d concurrently, then closes it"
  []
  (let [c (chan)]
    (go (dotimes [_ 12] (<! (timeout (rand-int 250))) (>! c :a)))
    (go (dotimes [_ 4] (<! (timeout (rand-int 750))) (>! c :b)))
    (go (dotimes [_ 3] (<! (timeout (rand-int 1000))) (>! c :c)))
    (go (dotimes [_ 2] (<! (timeout (rand-int 1500))) (>! c :d)))
    (go (<! (timeout 4000)) (close! c))
    c))

(defn fill-chan-sequentially
  "creates and returns a channel then fills the channel with :a, :b, :c, :d sequentially, then closes it"
  []
  (let [c (chan)]
    (go (dotimes [_ 12] (<! (timeout (rand-int 250))) (>! c :a))
        (dotimes [_ 4] (<! (timeout (rand-int 750))) (>! c :b))
        (dotimes [_ 3] (<! (timeout (rand-int 1000))) (>! c :c))
        (dotimes [_ 2] (<! (timeout (rand-int 1500))) (>! c :d))
        (close! c))
    c))

(defn read-chan
  "reads from supplied channel asynchronously (in a go block) until it is closed"
  [c]
  (go-loop [q (<! c)]
    (println q)
    (if q
      (recur (<! c))
      (println "channel closed")))
  (println "read-chan exit"))

(defn read-chan-blocking
  "reads from supplied channel until it is closed, blocks if channel empty"
  [c]
  (loop [q (<!! c)]
    (println q)
    (if q
      (recur (<!! c))
      (println "channel closed")))
  (println "read-chan-blocking exit"))

(read-chan (fill-chan-concurrently))
(read-chan (fill-chan-sequentially))
(read-chan-blocking (fill-chan-concurrently))
(read-chan-blocking (fill-chan-sequentially))
