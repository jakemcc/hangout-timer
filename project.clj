(defproject hangout-timer "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2268"]
                 [sablono "0.2.17"]
                 [om "0.6.4"]
                 [cljs-uuid "0.0.4"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src"]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {
              :builds {:dev {:source-paths ["src"]
                             :notify-command ["terminal-notifier" "-sound" "Submarine" "-title" "hangout-timer" "-message"]
                             :compiler {
                                        :output-to "main.js"
                                        :output-dir "out"
                                        :optimizations :none
                                        :source-map true
                                        :externs ["hangout.js"]
                                        :closure-warnings {:externs-validation :off
                                                           :non-standard-jsdoc :off}}}
                       :release {:source-paths ["src"]
                                 :compiler {
                                            :output-to "prod-main.js"
                                            :optimizations :simple
                                            :pretty-print true
                                            :preamble ["react/react.min.js"]
                                            :externs ["react/externs/react.js" "hangout.js"]
                                            :closure-warnings {:externs-validation :off
                                                               :non-standard-jsdoc :off}}}}})
