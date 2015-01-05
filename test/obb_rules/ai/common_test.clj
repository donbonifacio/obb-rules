(ns obb-rules.ai.common-test
  (:require [obb-rules.evaluator :as evaluator]
            [obb-rules.stash :as stash]
            [obb-rules.board :as board]
            [obb-rules.unit :as unit]
            [obb-rules.result :as result]
            [obb-rules.turn :as turn]
            [obb-rules.generators :as obb-gen]
            [obb-rules.game :as game]
            [obb-rules.ai.common :as common]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop])
  (:use clojure.test obb-rules.board obb-rules.element
        clojure.test.check
        clojure.test.check.clojure-test))

(def rain (unit/get-unit-by-name "rain"))

(deftest get-empty-attack-options
  (let [board (-> (board/create-board)
                  (place-element [2 5] (create-element :p1 rain 1 :north [2 5])))
        options (common/attack-options board (board/get-element board [2 5]))]
    (is (= 0 (count options)))))
