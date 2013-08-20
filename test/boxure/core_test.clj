(ns boxure.core-test
  (:require [clojure.test :refer :all]
            [boxure.core :refer :all]))

(deftest a-test
  (testing "Leaking"
    (doseq [i (range 10)]
      (println "Starting and stopping box nr" (inc i))
      (clean-and-stop (boxure {:resolve-dependencies true}
                    (.getClassLoader clojure.lang.RT)
                    "dev-resources/modules/module.jar"))
      (System/gc)
      (Thread/sleep 1000))))
