(ns buzz.core
  (:require [buzz.loop          :as loop]
            [clojure.core.async :as async :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]]
            [overtone.midi      :as midi]
            [overtone.live      :as o]))

(do
  (def stop (atom false))
  (def main-chan (chan))
  (def midi-chan (chan 1))
  (def timeout-value 2000))

(defn listen-for-midi-events
  []
  (o/on-event
   [:midi :note-on]
   (fn [e]
     (>! midi-chan e))
   ::keyboard-handler))

(defn handler
  [e]
  (println "got event:" e))

(defn main
  []
  (listen-for-midi-events)
  (go
    (while true
      (>! main-chan (<! midi-chan))))
  (loop/timed-async-loop timeout-value main-chan handler))

(comment
  (main)
  (go
    (dotimes [_ 10]
      (>! main-chan (rand-nth [:foof :...meow]))))
  )
