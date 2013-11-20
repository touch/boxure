(defproject boxure "0.1.0-SNAPSHOT"
  :description "A Clojure runtime isolating classloader."
  :url "http://github.com/touch/boxure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[nl.onlinetouch/clojure "1.5.1"]
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
  :java-source-paths ["src-java"]
  :pom-plugins [[com.theoryinpractise/clojure-maven-plugin "1.3.15"
                 {:extensions "true"
                  :configuration ([:sourceDirectories [:sourceDirectory "src"]]
                                  [:temporaryOutputDirectory "true"])
                  :executions [:execution
                               [:id "compile-clojure"]
                               [:phase "compile"]
                               [:goals [:goal "compile"]]]}]])
