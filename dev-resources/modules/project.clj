(defproject module "1.0"
  :dependencies [;; [org.clojure/clojure "1.5.1"]
                 [org.clojars.tcrawley/clojure "1.6.0-clearthreadlocals"]
                 ]
  :exclusions [org.clojure/clojure]
  :boxure {:start module.core/start
           :stop module.core/stop
           :ring-var module.core/app
           :config {:module/connect "localhost:123"}})
