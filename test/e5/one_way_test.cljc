(ns e5.one-way-test
  (:require [clojure.test :refer [deftest is testing]]
            [statecharts.core :as fsm]
            [e5.one-way :refer [one-way]]
            [e5.traffic-light-core :as core]))

(defn step-events [machine events]
  (reduce (fn [s ev] (fsm/transition machine s ev {:strict false}))
          (fsm/initialize machine)
          events))

(deftest one-way-requirements-test
  (testing "R7 & R9: Starts at all-red (:r) and does not cycle until :run is received"
    (let [s0 (fsm/initialize one-way)
          s1 (fsm/transition one-way s0 :next {:strict false})]
      (is (core/state-matches? s0 :r))
      (is (core/state-matches? s1 :r))))

  (testing "R9 & R4: :run enables the standard UK cycle :r -> :ra -> :g -> :a -> :r"
    (let [s-run (step-events one-way [:run])
          s-ra  (fsm/transition one-way s-run :next {:strict false})
          s-g   (fsm/transition one-way s-ra :next {:strict false})
          s-a   (fsm/transition one-way s-g :next {:strict false})
          s-r   (fsm/transition one-way s-a :next {:strict false})]
      (is (core/state-matches? s-run :r))
      (is (core/state-matches? s-ra :ra))
      (is (core/state-matches? s-g :g))
      (is (core/state-matches? s-a :a))
      (is (core/state-matches? s-r :r))))

  (testing "R10 & R4: :halt while :g gracefully finishes :g -> :a -> :r on :next and stops"
    (let [s-green   (step-events one-way [:run :next :next])
          s-halting (fsm/transition one-way s-green :halt {:strict false})
          s-amber   (fsm/transition one-way s-halting :next {:strict false})
          s-red     (fsm/transition one-way s-amber :next {:strict false})
          s-after   (fsm/transition one-way s-red :next {:strict false})]
      (is (core/state-matches? s-halting :g))
      (is (core/state-matches? s-amber :a))
      (is (core/state-matches? s-red :r))
      (is (core/state-matches? s-after :r)))))