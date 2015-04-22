(ns buzz.loop
  (:require [clojure.core.async :as async :refer [alts! go chan <! >! <!! >!! timeout]]))

(def stop-val (atom false))

(defn stop
  []
  (reset! stop-val true))

(defn start
  []
  (reset! stop-val false))

(defn timed-async-loop
  [wait input-ch handler]
  (let [ch (chan)]
    (go
      (while (not @stop-val)
        (<! (timeout wait))
        (handler (<! ch))))
    (go
      (while (not @stop-val)
        (<! (timeout wait))
        (>! ch (<! input-ch))))
    input-ch))
