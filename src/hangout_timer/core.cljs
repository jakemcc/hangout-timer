(ns hangout-timer.core
  (:require [om.core :as om :include-macros true]
            [cljs.reader :as reader]
            [sablono.core :as html :refer-macros [html]]
            [goog.Timer :as gtimer]
            [goog.events :as events]
            [cljs-uuid.core :as uuid]))

(enable-console-print!)

(def me (uuid/make-random))

(def app-state (atom {}))

(defn update-counter [expiry]
  (let [seconds-remaining (max 0 (quot (- expiry (goog.now)) 1000))]
    (if (zero? seconds-remaining)
      (swap! app-state update-in [:timers] dissoc expiry)
      (swap! app-state assoc-in [:timers expiry] seconds-remaining))))

(defn update-counters []
  (doseq [expiry (keys @app-state)]
    (update-counter expiry)))

(defn start-timer []
  (let [timer (goog.Timer. 333)]
    (.start timer)
    (events/listen timer goog.Timer/TICK (partial update-counters))))

(defn now-plus-n-minutes [n]
  (+ (* n 60 1000) (goog.now)))

(defn button [label action]
  [:button
   {:on-click action}]
  label)

(defn n-minute-button [n]
  (button (str n "minutes")
          (fn [e]
            (println "Starting" n "minute timer")
            (update-counter (now-plus-n-minutes n)))))

(defn take-control [& _]
  (swap! app-state assoc :time-master me))

(defn relinquish-control [& _]
  (swap! app-state dissoc :time-master))

(defn widget [data]
  (om/component
   (html [:div
          [:p "Simple Timers!"]
          (when (nil? (:time-master data))
            (button "Take Control" take-control))
          (if (= me (:time-master data))
            [:div
             (n-minute-button 0.1)
             (n-minute-button 1)
             (n-minute-button 3)
             (button "Relinquish control" relinquish-control)])
          (for [t (vals (:timers data))]
            [:p (pr-str t)])])))

(defn submit-data [m]
  (.setValue gapi.hangout.data "cljs" (pr-str m)))

(defn read-data []
  (reader/read-string (.getValue gapi.hangout.data "cljs")))

(defn publish-timer []
  (let [timer (goog.Timer. 500)]
    (.start timer)
    (events/listen timer goog.Timer/TICK (fn [& _]
                                           (when (= me (:time-master @app-state))
                                             (submit-data @app-state))))))

(defn ^:export main []
  (start-timer)
  (publish-timer)
  (.add gapi.hangout.data.onStateChanged (fn [data] (reset! app-state (read-data))))
  (om/root widget app-state {:target js/document.body}))
