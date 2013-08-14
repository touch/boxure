(defproject boxure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [leiningen-core "2.2.0"]
                 [classlojure "0.6.6"]]
  :jvm-opts ["-XX:+UseConcMarkSweepGC"
             "-XX:+CMSClassUnloadingEnabled"
             "-XX:MaxPermSize=28m" ; 28
             ;; "-XX:+TraceClassLoading"
             "-XX:+HeapDumpOnOutOfMemoryError"
             "-XX:+TraceClassUnloading"]
  :main boxure.core)
