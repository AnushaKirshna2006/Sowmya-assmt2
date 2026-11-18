(ns e5.traffic-light-core)

(defn uk-signal-states
  "Returns the standard UK traffic light cycle states (R4, R6, R7):
   :r -> :ra -> :g -> :a -> :r on :next events.
   Uses internal targetless transitions {:actions []} to ignore :run and :halt."
  ([] (uk-signal-states :r))
  ([on-red-target]
   {:r  {:on {:next :ra, :run {:actions []}, :halt {:actions []}}}
    :ra {:on {:next :g,  :run {:actions []}, :halt {:actions []}}}
    :g  {:on {:next :a,  :run {:actions []}, :halt {:actions []}}}
    :a  {:on {:next on-red-target, :run {:actions []}, :halt {:actions []}}}}))

(defn state-matches?
  "Checks if a statechart state map or keyword contains `target-state`."
  [current-state target-state]
  (let [s (if (map? current-state) (:_state current-state) current-state)]
    (cond
      (= s target-state) true
      (set? s) (contains? s target-state)
      (sequential? s) (some #{target-state} s)
      (map? s) (boolean (some #(= % target-state) (tree-seq coll? seq s)))
      :else false)))