(ns ^{:added "1.10" :author "Pedro Santos"}
  obb-rules.evaluator
  "Evaluates a game for the participant players"
  (:require [obb-rules.game :as game]
            [obb-rules.unit :as unit]))

(defn- get-units
  "Gets the units from the game that belong to the given player.
  Also returns the quantities"
  [game player]
  (let [stash (game/get-stash game player)]
    (map (fn [[k v]] [(unit/fetch k) v]) stash)))

(defn- sum-value
  "Sums the value/quantity of the given units"
  [acc [unit quantity]]
  (+ acc (* quantity (unit/unit-value unit))))

(defn- get-score
  "Calculates the score for the given units on the given game"
  [game units]
  (reduce sum-value 0 units))

(defn eval-game
  "Evaluates a game for the participant players"
  ([game]
   (eval-game game :simple))
  ([game mode]
   (let [p1-units (get-units game :p1)
         p2-units (get-units game :p2)]
      [(get-score game p1-units) (get-score game p2-units)])))
