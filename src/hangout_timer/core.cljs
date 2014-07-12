(ns hangout-timer.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [goog.Timer :as gtimer]
            [goog.events :as events]
            [cljs-uuid.core :as uuid]))

(enable-console-print!)

(def me (uuid/make-random))

(defn submit-delta [m]
  (gapi.hangout.data/submitDelta m))

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

(defn increment-timer []
  (let [timer (goog.Timer. 2000)]
    (.start timer)
    (events/listen timer goog.Timer/TICK (fn [& _]
                                           (println (.getState gapi.hangout.data))
                                           (submit-delta (merge {:me me}
                                                                @app-state))))))

(defn ^:export main []
  (start-timer)
  ;; (gapi.hangout.data.onStateChanged.add (fn [data] (println data)))
  (om/root widget app-state {:target js/document.body}))
