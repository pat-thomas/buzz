(ns buzz.sounds
  (:require [overtone.live :as o]))

(def kick (o/freesound 148634))
(def hat (o/freesound 53525))

(o/definst steel-drum
  [note 60 amp 0.8]
  (let [freq (o/midicps note)]
    (* amp
       (o/env-gen (o/perc 0.01 0.05) 1 1 0 1 :action o/FREE)
       (+ (o/sin-osc (/ freq 2))
          (o/rlpf (o/saw freq) (* 1.1 freq) 0.4)))))


