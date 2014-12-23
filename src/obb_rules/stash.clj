(ns obb-rules.stash
  (:require [obb-rules.unit :as unit]))

(defn create
  "Creates a new stash"
  [& units]
  (apply hash-map units))

(defn how-many?
  "States how many of a unit are present"
  [stash unit]
  (or (stash (name unit)) 0))

(defn cleared?
  "Returns true if this stash is empty"
  [stash]
  (= 0 (count stash)))

(defn available?
  "Checks if a given quantity is available"
  [stash unit quantity]
  (<= quantity (how-many? stash unit)))

(defn retrieve
  "Removes units from stash"
  [stash unit quantity]
  (let [current-quantity (how-many? stash unit)
        new-quantity (- current-quantity quantity)]
    (assert (>= current-quantity quantity) "InvalidStashQuantity")
    (if (= new-quantity 0)
      (dissoc stash unit)
      (assoc stash unit new-quantity))))

(defn- random-by-category
  "Returns random units for the given category"
  [expected-units category quantity]
  (let [units (unit/units-by-category category)
        with-quantity (map (fn [u] [(unit/unit-name u)  quantity]) units)
        units-count (count with-quantity)
        places (take expected-units (distinct (repeatedly #(rand-int units-count))))
        lucky (map (vec with-quantity) places)]
    (flatten lucky)))

(defn random
  "Creates a stash randomly populated"
  []
  (let [lights (random-by-category 2 :light 100)
        mediums (random-by-category 3 :medium 50)
        heavies (random-by-category 3 :heavy 25)
        all (concat lights mediums heavies)]
    (apply create all)))
