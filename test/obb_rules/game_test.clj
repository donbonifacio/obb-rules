(ns obb-rules.game-test
  (:require [obb-rules.game :as game]
            [obb-rules.board :as board]
            [obb-rules.turn :as turn]
            [obb-rules.game-mode :as game-mode]
            [obb-rules.stash :as stash])
  (:use clojure.test))

(deftest create-game
  (let [game (game/random)]
    (is game)
    (is (= :deploy (game/state game)))
    (is (= 0 (board/board-elements-count game)))
    (is (not (stash/cleared? game)))))

(deftest complete-game-processing
  (let [stash (stash/create :kamikaze 1)
        game (game/create stash)
        game2 (turn/process game :p1 [:deploy 1 :kamikaze [1 7]])
        game3 (turn/process game2 :p2 [:deploy 1 :kamikaze [1 2]])
        game4 (game/start-battle game3)]
    (is (not= :deploy (game/state game4)))
    (is (stash/cleared? (game/get-stash game4 :p2)))
    (is (stash/cleared? (game/get-stash game4 :p1)))
    (is (= false (game-mode/final? game4)))

    (let [battle (turn/process game4 :p1 [:move [1 7] [1 6] 1]
                                         [:move [1 6] [1 5] 1]
                                         [:move [1 5] [1 4] 1]
                                         [:move [1 4] [1 3] 1]
                                         [:attack [1 3] [1 2]])]
      (is (= 1 (board/board-elements-count battle)))

      (let [mode (game/mode battle)]
        (is (= :final (game/state battle)))
        (is (= :default mode))
        (is (= true (game-mode/final? battle)))
        (is (= :p1 (game-mode/winner battle)))))))
