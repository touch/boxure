;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.

(ns boxure.core-test
  (:refer-clojure :exclude (eval))
  (:require [clojure.test :refer :all]
            [clojure.java.io :refer (file)]
            [boxure.core :refer :all])
  (:import [java.lang.ref WeakReference]))

(deftest a-test
  (testing "Leaking"
    ;; Ensure an Agent thread pool is (also) running in ROOT
    (send (agent "\nAgent has started\n") println)

    (doseq [i (range 10)]
      (println "Starting, testing and stopping box nr" (inc i))
      (let [box (boxure {:resolve-dependencies true
                         :debug? true}
                        (.getClassLoader clojure.lang.RT)
                        (file "dev-resources/modules/module.jar"))]
        ;; Simple test.
        (eval box '(prn *ns*))

        ;; Redefining types.
        (do (eval box '(defrecord Foo [bar]))
            (eval box '(defrecord Foo [bar baz]))
            (eval box '(prn (map->Foo {:baz "THE Alice?"}))))

        ;; Using functions directly.
        (prn ((eval box 'map->Foo) {:baz "Nope, just Bob."}))
        ;; Requiring namespaces in a Future and evalling immediately
        (prn (@(future (eval box '(do (require 'clojure.set) (clojure.set/union)))) {}))

        ;; Using a function that defines new things.
        (do (eval box '(defn foo [s] (def bar (fn [] s))))

            ;; Evaluate defining function within box thread.
            (eval box '(prn (do (foo "baz")
                                (bar))))
            (prn ((eval box 'bar)))

            ;; Evaluate defining function within this thread, but context classloader set to box's.
            (call-in-box box (eval box 'foo) "eve")
            (prn ((eval box 'bar)))

            ;; Start an agent threadpool
            (eval box '(def wout (agent 0)))
            (eval box '(send wout inc))

            ;; Evaluate defining function within this thread, not setting any classloader.
            ((eval box 'foo) "henk")
            (eval box '(prn bar))
            (eval box '(prn (bar))))

        (clean-and-stop box))
      (System/gc)
      (Thread/sleep 1000))))
