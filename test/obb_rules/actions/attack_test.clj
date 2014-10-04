(ns obb-rules.actions.attack-test
  (:use clojure.test
        midje.sweet
        obb-rules.action
        obb-rules.actions.move
        obb-rules.board
        obb-rules.element
        obb-rules.result
        obb-rules.unit))

(def rain (get-unit-by-name "rain"))
(def crusader (get-unit-by-name "crusader"))
(def rain-element (create-element :p2 rain 10 :south))
(def crusader-element (create-element :p1 crusader 10 :south))

(def board1 (place-element (create-board) [2 2] crusader-element))
(def board (place-element board1 [2 3] rain-element))

(deftest test-attack

  (testing "hitpoints"
    (let [crusader-element (create-element :p1 crusader 1 :south)
          rain-element (create-element :p2 rain 1 :north)
          board (create-board)
          cboard (place-element board [1 1] crusader-element)
          rboard (place-element cboard [1 2] rain-element)
          attack (build-action [:attack [1 2] [1 1]])]
      (loop [board rboard]
        (let [result (attack board :p2)
              after-attack (result-board result)
              crusader-element (get-element after-attack [1 1])]
          (if crusader-element
            (recur after-attack)
            (is (nil? crusader-element)))))))

  (testing "emtpy target"
    (let [attack (build-action [:attack [2 2] [2 3]])
          result (attack board1 :p1)]
      (is (= "EmptyTarget" (result-message result)))))

  (testing "in range target"
    (let [board (place-element board1 [2 6] (create-element :p2 crusader 10 :east))
          attack (build-action [:attack [2 2] [2 6]])
          result (attack board :p1)]
      (is (succeeded? result))))

  (testing "out of range target"
    (let [board (place-element board1 [3 3] (create-element :p2 crusader 10 :east))
          attack (build-action [:attack [2 2] [3 3]])
          result (attack board :p1)]
      (is (failed? result))
      (is (= "OutOfRange" (result-message result)))))

  (testing "obstacle"
    (let [board (place-element board1 [2 3] (create-element :p2 crusader 10 :east))
          board2 (place-element board [2 4] (create-element :p2 crusader 10 :east))
          attack (build-action [:attack [2 2] [2 4]])
          result (attack board2 :p1)]
      (is (failed? result))
      (is (= "OutOfRange" (result-message result)))))

  (testing "same player target"
    (let [board (place-element board1 [2 3] (create-element :p1 crusader 10 :east))
          attack (build-action [:attack [2 2] [2 3]])
          result (attack board :p1)]
      (is (failed? result))
      (is (= "SamePlayer" (result-message result)))))

  (testing "player not owned"
    (let [attack (build-action [:attack [2 2] [2 3]])
          result (attack board :p2)]
      (is (failed? result))
      (is (= "NotOwnedElement" (result-message result)))))

  (testing "simple success"
    (let [attack (build-action [:attack [2 2] [2 3]])
          result (attack board :p1)]
      (is (succeeded? result))
      (is (nil? (get-element (result-board result) [2 3]))))))

(run-tests)