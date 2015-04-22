(ns buzz.core
  (:require [buzz.loop          :as loop]
            [clojure.core.async :as async :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]]
            [overtone.midi      :as midi]
            [overtone.live      :as o]))

(def main-chan (chan))
(def midi-chan (chan 1))
(def timeout-value 400)

(defn find-kbd-controller
  []
  (first (filter #(= (:name %) "USB Oxygen 8 v2")
                 (midi/midi-devices))))

(o/definst steel-drum
  [note 60 amp 0.8]
  (let [freq (o/midicps note)]
    (* amp
       (o/env-gen (o/perc 0.01 0.05) 1 1 0 1 :action o/FREE)
       (+ (o/sin-osc (/ freq 2))
          (o/rlpf (o/saw freq) (* 1.1 freq) 0.4)))))

(defn listen-for-midi-events
  []
  (o/on-event
   [:midi :note-on]
   (fn [e]
     (println "publishing event...")
     (go
       (>! midi-chan e)))
   ::keyboard-handler))

(defn handler
  [{:keys [note velocity] :as e}]
  (go
    (dotimes [_ 5]
      (steel-drum note velocity)
      (<! (timeout (+ 65 (rand-int 45)))))))

(defn main
  []
  (listen-for-midi-events)
  (go
    (while true
      (>! main-chan (<! midi-chan))))
  (loop/timed-async-loop timeout-value main-chan handler))

(comment
  (do
    (loop/stop)
    (loop/start))
  (do
    (main)
    (go
      (dotimes [_ 10]
        (>! main-chan (rand-nth [:foof :...meow])))))
  )
