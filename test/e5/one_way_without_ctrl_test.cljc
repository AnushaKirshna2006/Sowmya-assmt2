(ns e5.one-way-without-ctrl-test
  (:require [clojure.test :refer [deftest is testing]]
            [statecharts.core :as fsm]
            [e5.one-way-without-ctrl :refer [one-way-without-ctrl]]))

(defn step-events [machine events]
  (reduce (fn [s ev] (fsm/transition machine s ev {:strict false}))
          (fsm/initialize machine)
          events))

(deftest one-way-without-ctrl-requirements-test
  (testing "R7: The signal starts at all-red (:r)"
    (let [s0 (fsm/initialize one-way-without-ctrl)]
      (is (= :r (:_state s0)))))

  (testing "R4 & R6: Follows UK display cycle :r -> :ra -> :g -> :a -> :r strictly on :next"
    (let [s0 (fsm/initialize one-way-without-ctrl)
          s1 (fsm/transition one-way-without-ctrl s0 :next {:strict false})
          s2 (fsm/transition one-way-without-ctrl s1 :next {:strict false})
          s3 (fsm/transition one-way-without-ctrl s2 :next {:strict false})
          s4 (fsm/transition one-way-without-ctrl s3 :next {:strict false})]
      (is (= :ra (:_state s1)))
      (is (= :g  (:_state s2)))
      (is (= :a  (:_state s3)))
      (is (= :r  (:_state s4)))))

  (testing "R3 & R8: Minimises wait time (4 :next events per full cycle) and eventually displays :g"
    (let [states (reductions (fn [s ev] (fsm/transition one-way-without-ctrl s ev {:strict false}))
                             (fsm/initialize one-way-without-ctrl)
                             (repeat 8 :next))
          colours (map :_state states)]
      (is (some #{:g} colours))
      (is (= [:r :ra :g :a :r :ra :g :a :r] colours))))

  (testing "Prevents invalid transitions: ignores :run, :halt, and unknown events (R6)"
    (let [s-green (step-events one-way-without-ctrl [:next :next])]
      (is (= :g (:_state s-green)))
      (is (= :g (:_state (fsm/transition one-way-without-ctrl s-green :halt {:strict false}))))
      (is (= :g (:_state (fsm/transition one-way-without-ctrl s-green :run {:strict false})))))))