(ns e5.three-way-test
  (:require [clojure.test :refer [deftest is testing]]
            [statecharts.core :as fsm]
            [e5.three-way :refer [three-way]]))

(defn lights [state]
  (select-keys state [:w :n :s]))

(defn trace-states [machine events]
  (reductions (fn [s ev] (fsm/transition machine s ev {:strict false}))
              (fsm/initialize machine)
              events))

(deftest three-way-requirements-test
  (testing "R7 & R9: Starts at all-red {:w :r :n :r :s :r} and stays red until :run"
    (let [s0 (fsm/initialize three-way)
          s1 (fsm/transition three-way s0 :next {:strict false})]
      (is (= {:w :r :n :r :s :r} (lights s0)))
      (is (= {:w :r :n :r :s :r} (lights s1)))))

  (testing "R2 & R5: Never allows two signals to display :g or :a simultaneously"
    (let [states (trace-states three-way (cons :run (repeat 24 :next)))]
      (doseq [s states]
        (let [active-go-caution (filter #{:g :a :ra} (vals (lights s)))]
          (is (<= (count active-go-caution) 1)
              (str "Safety violation (R5) in state: " (lights s)))))))

  (testing "R3, R4 & R8: Fair round-robin interleaving (:w -> :n -> :s) in 12 :next events"
    (let [states (trace-states three-way (cons :run (repeat 12 :next)))
          seq-lights (map lights states)]
      ;; Start of :run (:w turn begins at :r)
      (is (= {:w :r  :n :r  :s :r}  (nth seq-lights 1)))
      ;; West cycle (events 1..4)
      (is (= {:w :ra :n :r  :s :r}  (nth seq-lights 2)))
      (is (= {:w :g  :n :r  :s :r}  (nth seq-lights 3)))
      (is (= {:w :a  :n :r  :s :r}  (nth seq-lights 4)))
      ;; Event 4 completes West -> :r and enters North turn at :r
      (is (= {:w :r  :n :r  :s :r}  (nth seq-lights 5)))
      ;; North cycle (events 5..8)
      (is (= {:w :r  :n :ra :s :r}  (nth seq-lights 6)))
      (is (= {:w :r  :n :g  :s :r}  (nth seq-lights 7)))
      (is (= {:w :r  :n :a  :s :r}  (nth seq-lights 8)))
      ;; Event 8 completes North -> :r and enters South turn at :r
      (is (= {:w :r  :n :r  :s :r}  (nth seq-lights 9)))
      ;; South cycle (events 9..12)
      (is (= {:w :r  :n :r  :s :ra} (nth seq-lights 10)))
      (is (= {:w :r  :n :r  :s :g}  (nth seq-lights 11)))
      (is (= {:w :r  :n :r  :s :a}  (nth seq-lights 12)))
      ;; Event 12 completes South -> :r and returns to West turn
      (is (= {:w :r  :n :r  :s :r}  (nth seq-lights 13)))))

  (testing "R10: :halt while :n is :g gracefully finishes :n -> :a -> :r and does NOT start :s"
    (let [states (trace-states three-way [:run
                                          :next :next :next :next ;; finish :w, enter :n at :r
                                          :next :next             ;; :n steps :r -> :ra -> :g
                                          :halt                   ;; trigger graceful halt while :n is :g
                                          :next                   ;; :n steps to :a
                                          :next                   ;; :n steps to :r (all-red)
                                          :next])                 ;; stays all-red, :s does not start
          last-five (map lights (take-last 5 states))]
      (is (= [{:w :r :n :g :s :r}
              {:w :r :n :g :s :r}
              {:w :r :n :a :s :r}
              {:w :r :n :r :s :r}
              {:w :r :n :r :s :r}]
             last-five)))))