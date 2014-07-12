(ns hangout-timer.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [goog.Timer :as gtimer]
            [goog.events :as events]))

(enable-console-print!)

(def app-state (atom {}))

(defn update-counter [expiry]
  (let [seconds-remaining (max 0 (quot (- expiry (goog.now)) 1000))]
    (swap! app-state assoc expiry seconds-remaining)))

(defn start-timer [expiry]
  (let [timer (goog.Timer. 333)]
    (update-counter expiry)
    (events/listen timer goog.Timer/TICK (partial update-counter expiry))
    (.start timer)))

(defn widget [data]
  (om/component
   (html [:div
          [:p "Simple Timers!"]
          [:p (pr-str data)]
          [:button
           {:on-click (fn [e]
                        (println "Starting 3 minute timer")
                        (start-timer (+ (* 3 60 1000) (goog.now))))}
           "3 minutes"]])))

(om/root widget app-state {:target js/document.body})

