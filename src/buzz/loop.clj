(ns buzz.loop
  (:require [clojure.core.async :as async :refer [alts! go chan <! >! <!! >!! timeout]]))

(defn timed-async-loop
  [wait input-ch handler]
  (let [ch (chan)]
    (go
      (while true
        (<! (timeout wait))
        (handler (<! ch))))
    (go
      (while true
        (<! (timeout wait))
        (>! ch (<! input-ch))))
    input-ch))
