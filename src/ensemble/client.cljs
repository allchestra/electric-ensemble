(ns ensemble.client
  (:require [ensemble.electric-app :as app]
            [hyperfiddle.electric3 :as e]))

(defonce !dispose (atom nil))

(defn- stop!
  []
  (when-let [dispose @!dispose]
    (dispose)
    (reset! !dispose nil)))

(defn start!
  []
  (stop!)
  (reset! !dispose
    ((e/boot-client {::e/hot-swap true} app/App))))

(defn ^:dev/after-load init
  []
  (start!))
