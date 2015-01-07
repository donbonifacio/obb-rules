(ns obb-rules.ai.acts-as-bot-test
  (:require [obb-rules.game :as game]
            [obb-rules.turn :as turn]
            [obb-rules.board :as board]
            [obb-rules.unit :as unit]
            [obb-rules.result :as result])
  (:use clojure.test obb-rules.board obb-rules.element))

(def rain (unit/get-unit-by-name "rain"))
(def krill (unit/get-unit-by-name "krill"))
(def vect (unit/get-unit-by-name "vector"))

(defn validate-deploy
  "Applies a bot function to a deployable game"
  [botfn]
  (let [game (game/random)
        actions (botfn game :p1)
        result (turn/process-actions game :p1 actions)]
    (is (result/succeeded? result))))

(defn direct-attack
  "Applies a bot function to a deployable game"
  [botfn]
  (let [board (-> (board/create-board)
                  (place-element [2 5] (create-element :p1 rain 1 :south [2 5]))
                  (place-element [2 6] (create-element :p2 rain 1 :north [2 6])))
        actions (botfn board :p1)
        result (turn/process-actions board :p1 actions)
        final-game (result/result-board result)]
    (is (result/succeeded? result))
    (is (board/empty-board? final-game :p2))))
