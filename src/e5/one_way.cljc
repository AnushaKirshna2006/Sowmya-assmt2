(ns e5.one-way
  (:require [statecharts.core :as fsm]))

(def one-way
  "Controlled one-way traffic light specification conforming to R1-R10.
   Starts in :halted (:r). :run enables cycling; :halt gracefully returns to :r."
  (fsm/machine
   {:id      :one-way
    :initial :halted
    :states
    {:halted
     {:initial :r
      :on      {:run  [:> :running]
                :next {:actions []}
                :halt {:actions []}}
      :states  {:r {}}}

     :running
     {:initial :r
      :states
      {:r  {:on {:next :ra
                 :halt [:> :halted]
                 :run  {:actions []}}}
       :ra {:on {:next :g
                 :halt [:> :halting :ra]
                 :run  {:actions []}}}
       :g  {:on {:next :a
                 :halt [:> :halting :g]
                 :run  {:actions []}}}
       :a  {:on {:next :r
                 :halt [:> :halting :a]
                 :run  {:actions []}}}}}

     :halting
     {:initial :a
      :states
      {:ra {:on {:next :g
                 :run  [:> :running :ra]
                 :halt {:actions []}}}
       :g  {:on {:next :a
                 :run  [:> :running :g]
                 :halt {:actions []}}}
       :a  {:on {:next [:> :halted]
                 :run  [:> :running :a]
                 :halt {:actions []}}}}}}}))