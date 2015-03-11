(ns buzz.scratch
  (:require [clojure.core.typed :as t]
            [clojure.core.async :as async]))

(def timeout-randomness-val 500)

(defn timeout-with-ack
  [impl ack-value & timeout-val]
  (let [ch (async/chan)]
    (async/go
      (impl)
      (async/<!! (async/timeout (or timeout-val
                                    (rand-int timeout-randomness-val))))
      (async/>! ch ack-value))
    ch))

(defmacro def-timeout-with-ack
  [^clojure.lang.Symbol fn-name & body]
  `(defn ~fn-name
     []
     (timeout-with-ack (fn []
                         ~@body)
                       ~(keyword fn-name))))

(def-timeout-with-ack hello (println "hello"))
(def-timeout-with-ack goodbye (println "goodbye"))
(def-timeout-with-ack meow (println "meow"))

(comment
  (let [ch (async/chan)]
    (async/go
      (while true
        (println (async/<! ch))))
    (async/go
      (doseq [thing (shuffle (range 10))]
        (async/>! ch (str "foo-" thing)))))
  (first (async/alts!! [(hello) (goodbye) (meow)]))
  )

;; always type check on compile
;;(t/check-ns)


(comment
  (let [ch (async/chan)]
    (doseq [n (shuffle (range 100))]
      (async/go
        (async/>! ch n)))
    (async/go
      (dotimes [_ 10]
        (println (async/<! ch))))
    :wat)
  )



(comment
  (let [make-random-event-batch (fn []
                                  (for [_ (range (rand-int 10))]
                                    (rand-nth [true false])))
        ch                      (async/timeout 400)]
    (doseq [evt (make-random-event-batch)]
      (when evt
        (async/go
          (let [val (rand-int 800)]
            (async/>! ch val)
            (Thread/sleep val))))
      (async/go
        (println (async/<! ch))))
    :done)
  )
