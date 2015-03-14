(ns buzz.core
  (:require [buzz.events        :as events]
            [clojure.core.typed :as t]
            [clojure.core.async :as async :refer [go go-loop chan <! >! alts! timeout]]))

(def stop (atom false))
(def timeout-value 500)
(def event-batch-chan (chan 1))
(def event-chan (chan 100))

(defn start-event-consumer
  []
  (go
    (while true
      (if-let [evt (<! event-chan)]
        (when-let [impl (-> evt :evt-name events/lookup-handler)]
          (impl (:data evt)))
        (println "waiting...")))))

(defn publish-event!
  [evt-name data]
  (go
    (let [evt {:evt-name evt-name
               :data     data}]
      (println (format "publishing event %s" evt))
      (>! event-chan evt))))

(defn fanout-event-batch
  [[evt-batch _]]
  (if evt-batch
    (go
      (do
        (println (format "publishing event batch of size %s" (count evt-batch)))
        (doseq [evt evt-batch
                :let [{:keys [evt-name data]} evt]]
          (publish-event! evt-name data))))))

(defn main
  []
  (start-event-consumer)
  (go-loop [times 0]
    (println (format "Tick: %s" times))
    (fanout-event-batch (alts!
                         [event-batch-chan (timeout timeout-value)]))
    (when-not @stop
      (recur (inc times))))
  :main-running)

(comment
  (reset! stop true)
  (reset! stop false)
  (main)
  (dotimes [_ 50]
    (go
      (>! event-batch-chan
          [{:evt-name :kick :data {}}
           {:evt-name :hat :data {}}])))
  )

;; always type check on compile
;;(t/check-ns)
