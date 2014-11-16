(ns obb-rules.action
  (:require [obb-rules.actions.rotate :as rotate]
            [obb-rules.actions.attack :as attack]
            [obb-rules.actions.deploy :as deploy]
            [obb-rules.actions.move :as move]))

(def ^:private available-actions
  {:rotate rotate/build-rotate
   :attack attack/build-attack
   :deploy deploy/build-deploy
   :move move/build-move})

(defn build-action
  "Buidls an action given its code and args"
  [[action-type & action-args]]
  (let [builder (-> (keyword action-type)
                    (available-actions))]
    (assert builder (str "No action builder defined for " action-type))
    (builder action-args)))

(defn reset-action-specific-state
  "Removes action specific state from the board"
  [board]
  (move/reset-action-state board))
