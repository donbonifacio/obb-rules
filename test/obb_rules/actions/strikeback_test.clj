(ns obb-rules.actions.strikeback-test
  (:require [obb-rules.element :as element]
            [obb-rules.result :as result])
  (:use clojure.test
        obb-rules.action
        obb-rules.actions.move
        obb-rules.board
        obb-rules.element
        obb-rules.result
        obb-rules.unit))

(def rain (get-unit-by-name "rain"))
(def crusader (get-unit-by-name "crusader"))
(def eagle (get-unit-by-name "eagle"))
(def krill (get-unit-by-name "krill"))
(def rain-element (create-element :p1 rain 10 :south))
(def eagle-element (create-element :p1 eagle 10 :south))
(def krill-element (create-element :p2 krill 10 :north))
(def crusader-element (create-element :p1 crusader 10 :south))

(def board-with-krill (place-element (create-board) [8 8] krill-element))

(deftest attack-strikeback-direct
  (let [board (-> board-with-krill
                  (place-element [8 7] rain-element))
        attack (build-action [:attack [8 7] [8 8]])
        result (attack board :p1)
        info (last (result/info result))]
    (println info)
    (is (succeeded? result))
    (is (= "OK" (result-message result)))
    (is (= :strikeback (info :attack-type)))
    (is (nil? (get-element (result-board result) [8 7])))))
