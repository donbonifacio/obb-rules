(ns obb-rules.actions.move-test
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
(def nova (get-unit-by-name "nova"))
(def pretorian (get-unit-by-name "pretorian"))
(def move-down (build-action [:move [2 2] [2 3] 10]))
(def rain-element (create-element :p1 rain 10 :south))
(def crusader-element (create-element :p1 crusader 10 :south))
(def nova-element (create-element :p1 nova 10 :south))
(def pretorian-element (create-element :p1 pretorian 10 :south))
(def board (place-element (create-board) [2 2] rain-element))

(defn- test-complete-move
  [board old-coord new-coord expected-success?]
  (let [move (build-action [:move old-coord new-coord 10])
        result (move board :p1)]
    (if expected-success?
      (do
        (is (succeeded? result))
        (if (succeeded? result)
          (let [new-board (result-board result)
                new-element (get-element new-board new-coord)
                old-element (get-element new-board old-coord)]
            (is (not old-element))
            (is new-element))))
      (do
        (is (failed? result))))))

(deftest movement-restrictions

  (testing "front movement"
    (let [board (place-element (create-board) [2 2] crusader-element)]
      (test-complete-move board [2 2] [1 1] false)
      (test-complete-move board [2 2] [1 2] false)
      (test-complete-move board [2 2] [1 3] false)
      (test-complete-move board [2 2] [2 1] false)
      (test-complete-move board [2 2] [2 3] true)
      (test-complete-move board [2 2] [3 1] false)
      (test-complete-move board [2 2] [3 2] false)
      (test-complete-move board [2 2] [3 3] false)))

  (testing "normal movement"
    (let [board (place-element (create-board) [2 2] nova-element)]
      (test-complete-move board [2 2] [1 1] false)
      (test-complete-move board [2 2] [1 2] true)
      (test-complete-move board [2 2] [1 3] false)
      (test-complete-move board [2 2] [2 1] true)
      (test-complete-move board [2 2] [2 3] true)
      (test-complete-move board [2 2] [3 1] false)
      (test-complete-move board [2 2] [3 2] true)
      (test-complete-move board [2 2] [3 3] false)))

  (testing "diagonal movement"
    (let [board (place-element (create-board) [2 2] pretorian-element)]
      (test-complete-move board [2 2] [1 1] true)
      (test-complete-move board [2 2] [1 2] false)
      (test-complete-move board [2 2] [1 3] true)
      (test-complete-move board [2 2] [2 1] false)
      (test-complete-move board [2 2] [2 3] false)
      (test-complete-move board [2 2] [3 1] true)
      (test-complete-move board [2 2] [3 2] false)
      (test-complete-move board [2 2] [3 3] true)))

  (testing "all-movement"
    (test-complete-move board [2 2] [1 1] true)
    (test-complete-move board [2 2] [1 2] true)
    (test-complete-move board [2 2] [1 3] true)
    (test-complete-move board [2 2] [2 1] true)
    (test-complete-move board [2 2] [2 3] true)
    (test-complete-move board [2 2] [3 1] true)
    (test-complete-move board [2 2] [3 2] true)
    (test-complete-move board [2 2] [3 3] true)))

(deftest partial-movement

  (testing "partial-movement"
    (let [old-coord [2 2]
          new-coord [1 2]
          move (build-action [:move old-coord new-coord 4])
          result (move board :p1)
          new-board (result-board result)
          new-element (get-element new-board new-coord)
          old-element (get-element new-board old-coord)]
      (is (succeeded? result))
      (is old-element)
      (is new-element)
      (is (= 2 (result-cost result)))
      (is (= 4 (element-quantity new-element)))
      (is (= 6 (element-quantity old-element))))))

(deftest cost
  (testing "full move cost"
    (is (= 2 (movement-cost rain true)))
    (is (= 1 (movement-cost rain false)))))

(deftest generic-movement

  (testing "failures"

    (testing "fails if quantity is not the minimum percentage"
      (let [old-coord [2 2]
            new-coord [1 2]
            move (build-action [:move old-coord new-coord 1])
            result (move board :p1)]
        (is (failed? result))
        (is (= "InvalidQuantityPercentage" (result-message result)))))

    (testing "fails if quantity is not the maximum percentage"
      (let [old-coord [2 2]
            new-coord [1 2]
            move (build-action [:move old-coord new-coord 9])
            result (move board :p1)]
        (is (failed? result))
        (is (= "InvalidQuantityPercentage" (result-message result)))))

    (testing "not adjacent"
      (let [move-far (build-action [:move [2 2] [4 4] 10])
            result (move-far board :p1)]
        (is (failed? result))
        (is (= "NotAdjacent" (result-message result)))))

    (testing "out of bounds"
      (let [move-far (build-action [:move [8 8] [9 9] 10])
            result (move-far board :p1)]
        (is (failed? result))
        (is (= "OutOfBounds" (result-message result)))))

    (testing "coordinate is from another player"
      (let [action (build-action [:move [1 1] [2 2] 10])
            element (create-element :p2 rain 10 :south)
            board (place-element (create-board) [1 1] element)
            result (action board :p1)]
        (is (failed? result))
        (is (= "NotOwnedElement" (result-message result)))))

    (testing "target coordinate is from another player"
      (let [action (build-action [:move [2 2] [3 3] 10])
            element (create-element :p2 rain 10 :south)
            new-board (place-element board [3 3] element)
            result (action new-board :p1)]
        (is (failed? result))
        (is (= "NotOwnedElement" (result-message result)))))

    (testing "target coordinate is another unit"
      (let [action (build-action [:move [2 2] [3 3] 10])
            element (create-element :p1 crusader 10 :south)
            new-board (place-element board [3 3] element)
            result (action new-board :p1)]
        (is (failed? result))
        (is (= "UnitMismatch" (result-message result)))))

    (testing "coordinate empty"
      (let [action (build-action [:move [1 1] [1 2] :10])
            board (create-board)
            result (move-down board :p1)]
        (is (failed? result))
        (is (= "EmptyCoordinate" (result-message result)))))))

(run-tests)
