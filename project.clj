(defproject boxure "0.1.0-SNAPSHOT"
  :description "A Clojure runtime isolating classloader."
  :url "http://github.com/containium/boxure"
  :license {:name "Mozilla Public License 2.0"
            :url "http://mozilla.org/MPL/2.0/"}
  :dependencies [[boxure/clojure "1.5.1"]
                 [leiningen-core "2.2.0"]
                 [classlojure "0.6.6"]]
  :exclusions [org.clojure/clojure]
  :jvm-opts ["-XX:+UseConcMarkSweepGC"
             "-XX:+CMSClassUnloadingEnabled"
             "-XX:MaxPermSize=35m"
             ;; "-XX:+TraceClassLoading"
             ;; "-XX:+TraceClassUnloading"
             ;; "-XX:+HeapDumpOnOutOfMemoryError"
             ]
  :java-source-paths ["src-java"])

;;; Sync changes to this file with pom.xml.
;;; ---TODO: Add the :pom-plugins key when Leiningen 2.3.4 is released.
