(ns buzz.events
  (:require [clojure.core.typed :as t]
            [clojure.core.async :as async :refer [go go-loop chan <! >! alts! timeout]]
            [buzz.sounds        :as sounds]))

(def registry (atom {}))

(defn lookup-handler
  [^clojure.lang.Keyword evt-name]
  (get @registry evt-name))

(defmacro defhandler
  [fn-name & body]
  `(do
     (defn ~fn-name
       ~@body)
     (swap! registry assoc ~(keyword fn-name) ~fn-name)))

(defhandler kick
  [_]
  (sounds/kick))

(defhandler kick-splay
  [& _]
  (go
    (dotimes [_ 4]
      (sounds/kick)
      (Thread/sleep (rand-int 20)))))

(defhandler hat
  [_]
  (sounds/hat))

(defhandler default
  [payload]
  (println "received payload:" payload))
