(ns buzz.events
  (:require [clojure.core.typed :as t]
            [buzz.sounds :as sounds]))

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

(defhandler foo
  [a b]
  (+ a b))

(defhandler kick
  [_]
  (sounds/kick))

(defhandler hat
  [_]
  (sounds/hat))

(defhandler default
  [payload]
  (println "received payload:" payload))
