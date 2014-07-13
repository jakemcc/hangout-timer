(ns hangout-timer.core
  (:require [om.core :as om :include-macros true]
            [cljs.reader :as reader]
            [sablono.core :as html :refer-macros [html]]
            [goog.Timer :as gtimer]
            [goog.events :as events]
            [cljs-uuid.core :as uuid]))

(enable-console-print!)

(defn me []
  (let [participant (.getLocalParticipant gapi.hangout)]
    (str (.-id (.-person participant)) "_"
         (.-displayName (.-person participant)))))

(def app-state (atom {:timers []}))

(defn dbg [x]
  (println x)
  x)

(defn read-data []
  (dbg (when-let [raw (.getValue gapi.hangout.data "cljs")]
         (reader/read-string raw))))

(defn submit-data [m]
  (.setValue gapi.hangout.data "cljs" (pr-str m)))

(defn seconds-remaining [expiry]
  (max 0 (quot (- expiry (goog.now)) 1000)))

(defn update-counters []
  (let [shared-data (read-data)]
    (swap! app-state assoc
           :timers (map seconds-remaining (:expiries shared-data))
           :time-master (:time-master shared-data))))

(defn start-timer []
  (let [timer (goog.Timer. 333)]
    (.start timer)
    (events/listen timer goog.Timer/TICK (partial update-counters))))

(defn now-plus-n-minutes [n]
  (+ (* n 60 1000) (goog.now)))

(defn button [label action]
  [:button {:on-click action} label])

(defn n-minute-button [n]
  (button (str n "minutes")
          (fn [e]
            (println "Starting" n "minute timer")
            (submit-data (update-in (read-data) [:expiries] conj (now-plus-n-minutes n))))))

(defn take-control [& _] ; try not doing this arguments
  (submit-data (assoc (read-data) :time-master (me))))

(defn relinquish-control [& _]
  (when (= (me) (:time-master (read-data)))
    (submit-data (dissoc (read-data) :time-master))))

(defn clear-timers [& _]
  (submit-data (dissoc (read-data) :expiries)))

(defn widget [data]
  (om/component
   (html [:div
          [:p "Simple Timers!"]
          (when (nil? (:time-master data))
            (button "Take Control" take-control))
          (when (= (me) (:time-master data))
            [:div
             (n-minute-button 0.1)
             (n-minute-button 1)
             (n-minute-button 3)
             (button "Relinquish control" relinquish-control)
             (button "Clear timers" clear-timers)
             ])
          (for [t (:timers data)]
            [:p (pr-str t)])])))

(defn ^:export main []
  (println "Me:" (me))
  (start-timer)
  (.add gapi.hangout.data.onStateChanged (fn [data]
                                           (swap! app-state assoc :time-master (:time-master (read-data)))))
  (om/root widget app-state {:target js/document.body}))
