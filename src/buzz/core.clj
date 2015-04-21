(ns buzz.core
  (:require [buzz.loop          :as loop]
            [clojure.core.async :as async :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]]
            [overtone.midi      :as midi]))

(do
  (def stop (atom false))
  (def main-chan (chan))
  (def timeout-value 2000))

(defn main
  []
  (loop/timed-async-loop timeout-value main-chan))

(comment
  (main)
  (go
    (dotimes [_ 10]
      (>! main-chan (rand-nth [:foof :...meow]))))
  )
