(defproject boxure "0.2.0-SNAPSHOT"
  :description "A Clojure runtime isolating classloader."
  :url "http://github.com/containium/boxure"
  :license {:name "Mozilla Public License 2.0"
            :url "http://mozilla.org/MPL/2.0/"}
  :dependencies [[boxure/clojure "1.9.0"]
                 ;; Until Leiningen fixes it's plexus and wagon deps, we add and upgrade it explicitly:
                 [leiningen-core "2.10.0" :exclusions [org.clojure/clojure]]
                 [classlojure "0.6.6"]
                 [org.apache.maven.wagon/wagon-provider-api "3.5.3"] 
                 [org.apache.maven.wagon/wagon-http "3.5.3"]
                 [commons-io/commons-io "2.11.0"]]
  
  :exclusions [org.clojure/clojure]
  :global-vars {*warn-on-reflection* true}
  :jvm-opts ["-XX:+UseConcMarkSweepGC"
             "-XX:+CMSClassUnloadingEnabled"
             "-XX:MaxPermSize=35m"
             ;; "-XX:+TraceClassLoading"
             ;; "-XX:+TraceClassUnloading"
             ;; "-XX:+HeapDumpOnOutOfMemoryError"
             ]
  :java-source-paths ["src-java"]
  :pom-addition [:properties
                 ["maven.compiler.source" "8"]
                 ["maven.compiler.target" "8"]]
  :pom-plugins [[com.theoryinpractise/clojure-maven-plugin "1.7.1"
                 {:extensions "true"
                  :configuration ([:sourceDirectories [:sourceDirectory "src"]]
                                  [:temporaryOutputDirectory "true"])
                  :executions [:execution
                               [:id "compile-clojure"]
                               [:phase "compile"]
                               [:goals [:goal "compile"]]]}]])
