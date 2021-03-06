(ns buzz.scratch
  (:require [clojure.core.typed :as t]
            [clojure.core.async :as async :refer [alts! go chan <! >! <!! >!! timeout]]))

(def stop (atom false))

(defn timed-async-loop
  [wait input-ch]
  (let [ch (chan)]
    (go
      (while true
        (<! (timeout wait))
        (println (<! ch))))
    (go
      (while true
        (<! (timeout wait))
        (>! ch (<! input-ch))))
    input-ch))
