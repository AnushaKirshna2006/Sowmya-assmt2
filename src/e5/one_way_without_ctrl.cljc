(ns e5.one-way-without-ctrl
  (:require [statecharts.core :as fsm]
            [e5.traffic-light-core :as core]))

(def one-way-without-ctrl
  "One-way traffic light specification conforming to R1-R8.
   Starts at :r and cycles continuously on :next events without :run/:halt control."
  (fsm/machine
   {:id      :one-way-without-ctrl
    :strict  false
    :initial :r
    :states  (core/uk-signal-states :r)}))