(ns obb-rules.actions.attack
  (:require [obb-rules.actions.direction :as dir]
            [obb-rules.element :as element]
            [obb-rules.game :as game]
            [obb-rules.simplifier :as simplify]
            [obb-rules.actions.damage-calculator :as calculator])
  (:use obb-rules.result obb-rules.board obb-rules.element obb-rules.unit))

(defn- advance-and-check-target
  "Goes to the next coordinate and checks if the target is valid"
  [board attacker target current-coordinate distance bypassed-element?]
  (let [unit (element-unit attacker)
        direction (element-direction attacker)
        next-coordinate (dir/update direction current-coordinate)
        next-element (get-element board next-coordinate)
        may-try-next? (or (nil? next-element) (element/catapult-attack? attacker))
        bypassed? (or bypassed-element? (nil? next-element) (not= next-element target))]
    (cond
      (= next-element target) (if bypassed? :catapult :direct)
      (>= distance (unit-range unit)) :out-of-range
      may-try-next? (recur board attacker target next-coordinate (+ 1 distance) bypassed?)
      :else :out-of-range)))

(defn- attack-restrictions
  "Checks if the attack is possible"
  [board player attacker target]
  (cond
    (nil? attacker) "EmptyAttacker"
    (not (game/player-turn? board player)) "StateMismatch"
    (frozen? attacker) "FrozenElement"
    (nil? target) "EmptyTarget"
    (simplify/not-name= player (element-player attacker)) "NotOwnedElement"
    (= (element-player attacker) (element-player target)) "SamePlayer"))

(defn- resolve-attack
  "Checks if the target element is in range"
  [board player attacker target]
  (if-let [error-msg (attack-restrictions board player attacker target)]
    [false error-msg]
    (let [lock-target (advance-and-check-target board attacker target (element-coordinate attacker) 1 false)]
      (if (= :out-of-range lock-target)
        [false "OutOfRange"]
        [true lock-target]))))

(defn- process-attack
  "Processes the attack"
  [board attacker target attack-type]
  (let [destroyed (calculator/destroyed attacker target)
        attacker-coordinate (element-coordinate attacker)
        target-unit (element-unit target)
        coordinate (element-coordinate target)
        frozen-board (swap-element board attacker-coordinate (element/freeze attacker))
        final-board (remove-from-element frozen-board coordinate destroyed)]
    (action-success final-board 1 "OK" [{:attack-type attack-type
                                         :destroyed destroyed
                                         :unit (unit-name target-unit)}])))

(defn build-attack
  "Builds an attack action on a board"
  [[coord target-coord]]
  (fn attacker [board player]
    (let [attacker (get-element board coord)
          target (get-element board target-coord)
          [success? info] (resolve-attack board player attacker target)]
      (if-not success?
        (action-failed info)
        (process-attack board attacker target info)))))

