(ns e5.three-way
  (:require [statecharts.core :as fsm]))

(def initial-lights
  "All signals start at red (R7)."
  {:w :r :n :r :s :r})

(defn- set-light [dir colour]
  (fsm/assign (fn [ctx _] (assoc ctx dir colour))))

(defn- turn-states
  "Builds the UK cycle (:r -> :ra -> :g -> :a -> next-turn) for a single direction `dir`
   while the other two directions remain locked at :r (R4, R5, R6)."
  [dir next-turn-target]
  {:initial :r
   :states
   {:r  {:entry (set-light dir :r)
         :on    {:next :ra
                 :halt [:> :halted]
                 :run  {:actions []}}}
    :ra {:entry (set-light dir :ra)
         :on    {:next :g
                 :halt [:> :halting dir :ra]
                 :run  {:actions []}}}
    :g  {:entry (set-light dir :g)
         :on    {:next :a
                 :halt [:> :halting dir :g]
                 :run  {:actions []}}}
    :a  {:entry (set-light dir :a)
         :on    {:next {:target next-turn-target
                        :actions [(set-light dir :r)]}
                 :halt [:> :halting dir :a]
                 :run  {:actions []}}}}})

(defn- halting-turn-states
  "Completes the interrupted direction's cycle gracefully to :r and stops in :halted (R10)."
  [dir]
  {:initial :ra
   :states
   {:ra {:entry (set-light dir :ra)
         :on    {:next :g
                 :run  [:> :running dir :ra]
                 :halt {:actions []}}}
    :g  {:entry (set-light dir :g)
         :on    {:next :a
                 :run  [:> :running dir :g]
                 :halt {:actions []}}}
    :a  {:entry (set-light dir :a)
         :on    {:next {:target [:> :halted]
                        :actions [(set-light dir :r)]}
                 :run  [:> :running dir :a]
                 :halt {:actions []}}}}})

(def three-way
  "Three-way coordinated traffic light specification (:w, :n, :s) conforming to R1-R10."
  (fsm/machine
   {:id      :three-way
    :initial :halted
    :context initial-lights
    :states
    {:halted
     {:on {:run  [:> :running]
           :next {:actions []}
           :halt {:actions []}}}

     :running
     {:initial :w
      :states
      {:w (turn-states :w [:> :running :n])
       :n (turn-states :n [:> :running :s])
       :s (turn-states :s [:> :running :w])}}

     :halting
     {:initial :w
      :states
      {:w (halting-turn-states :w)
       :n (halting-turn-states :n)
       :s (halting-turn-states :s)}}}}))

(comment
  (def service (fsm/service three-way))
  (fsm/start service)
  (fsm/send service :run)
  (dotimes [_ 12]
    (println (select-keys (fsm/state service) [:_state :w :n :s]))
    (fsm/send service :next)))