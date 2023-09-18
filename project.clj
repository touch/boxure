(defproject boxure "0.2.0-SNAPSHOT"
  :description "A Clojure runtime isolating classloader."
  :url "http://github.com/containium/boxure"
  :license {:name "Mozilla Public License 2.0"
            :url "http://mozilla.org/MPL/2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 ;; Until Leiningen fixes it's plexus and wagon deps, we add and upgrade it explicitly:
                 [leiningen-core "2.10.0" :exclusions [org.clojure/clojure org.codehaus.plexus/plexus-utils org.apache.maven.wagon/wagon-provider-api]]
                 [org.codehaus.plexus/plexus-utils "3.5.1"]
                 [org.apache.maven.wagon/wagon-provider-api "3.5.3"]
                 [org.projectodd.shimdandy/shimdandy-api "1.2.1"] 
                 [classlojure "0.6.6"]]
  :exclusions [org.clojure/clojure]
  :global-vars {*warn-on-reflection* true}
  :jvm-opts ["-XX:+UseConcMarkSweepGC"
             "-XX:+CMSClassUnloadingEnabled"
             "-XX:MaxPermSize=35m"
             ;; "-XX:+TraceClassLoading"
             ;; "-XX:+TraceClassUnloading"
             ;; "-XX:+HeapDumpOnOutOfMemoryError"
             "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000 OurApplication"

             ]
  :java-source-paths ["src-java"]
  :pom-addition [:properties
                 ["maven.compiler.source" "1.8"]
                 ["maven.compiler.target" "1.8"]]
  :pom-plugins [[com.theoryinpractise/clojure-maven-plugin "1.9.2"
                 {:extensions "true"
                  :configuration ([:sourceDirectories [:sourceDirectory "src"]]
                                  [:temporaryOutputDirectory "true"])
                  :executions [:execution
                               [:id "compile-clojure"]
                               [:phase "compile"]
                               [:goals [:goal "compile"]]]}]])
