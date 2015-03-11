(ns buzz.core
  (:require [buzz.events        :as events]
            [clojure.core.typed :as t]
            [clojure.core.async :as async]))

(def stop (atom false))
(def timeout-value 500)
(def event-batch-chan (async/chan 1))
(def event-chan (async/chan 100))

(defn consume-events
  []
  (async/go
    (while true
      (if-let [evt (async/<! event-chan)]
        (let [evt-name (:evt-name evt)]
          (when-let [impl (events/lookup-handler evt-name)]
            (impl (:data evt))))
        (println "waiting...")))))

(defn publish-event!
  [evt-name data]
  (async/go
    (let [evt {:evt-name evt-name
               :data     data}]
      (println (format "publishing event %s" evt))
      (async/>! event-chan evt))))

(defn fanout-event-batch
  []
  (async/go
    (when-let [evt-batch (async/<! event-batch-chan)]
      (do (println "will fanout event batch"))
      (println (format "publishing event batch of size %s" (count evt-batch)))
      (doseq [evt evt-batch]
        (publish-event! (:evt-name evt) (:data evt)))))
  :batch-published)

;; kick off consumer
;; start main loop
;; every time through loop, attempt to pull one batch of events and fan them out
(defn main
  []
  (consume-events)
  (async/go-loop [times 0]
    (println (format "Tick: %s" times))
    (async/<! (async/timeout timeout-value))
    (fanout-event-batch)
    (recur (inc times)))
  :main-running)

(comment
  (reset! stop true)
  (reset! stop false)
  (main)
  (async/<!! (async/timeout 1000))

  (async/go-loop [seconds 1]
    (async/<! (async/timeout 300))
    (println "tick..." seconds)
    (recur (inc seconds)))
  
  (dotimes [_ 100]
    (async/>!! event-batch-chan
               [{:evt-name :default :data :meow}
                {:evt-name :default :data :meow}
                {:evt-name :default :data :meow}]))
  )

;; always type check on compile
;;(t/check-ns)
