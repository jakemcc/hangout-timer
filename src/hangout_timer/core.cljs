(ns hangout-timer.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [goog.Timer :as gtimer]
            [goog.events :as events]))

(enable-console-print!)

(def app-state (atom {}))

(defn update-counter [expiry]
  (let [seconds-remaining (max 0 (quot (- expiry (goog.now)) 1000))]
    (if (zero? seconds-remaining)
      (swap! app-state dissoc expiry)
      (swap! app-state assoc expiry seconds-remaining))))

(defn update-counters []
  (doseq [expiry (keys @app-state)]
    (update-counter expiry)))

(defn start-timer []
  (let [timer (goog.Timer. 333)]
    (.start timer)
    (events/listen timer goog.Timer/TICK (partial update-counters))))

(defn now-plus-n-minutes [n]
  (+ (* n 60 1000) (goog.now)))

(defn n-minute-button [n]
  [:button
   {:on-click (fn [e]
                (println "Starting" n "minute timer")
                (update-counter (now-plus-n-minutes n)))}
   (str n " minutes")])

(defn widget [data]
  (om/component
   (html [:div
          [:p "Simple Timers!"]
          [:p (pr-str data)]
          (n-minute-button 0.1)
          (n-minute-button 1)
          (n-minute-button 3)
          ])))

(defn ^:export main []
  (start-timer)
  (om/root widget app-state {:target js/document.body}))

(main)
