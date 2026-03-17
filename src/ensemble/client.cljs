(ns ensemble.client
  (:require [ensemble.electric-app :as app]
            [hyperfiddle.electric3 :as e]))

(defonce !dispose (atom nil))

(defn- stop!
  "Stops the running Electric client, if one has been booted.

  Use this before restarting the browser app in the same cljs runtime."
  []
  (when-let [dispose @!dispose]
    (dispose)
    (reset! !dispose nil)))

(defn start!
  "Boots the Electric browser client for the main app.

  Call this once from the compiled browser entrypoint or after a hot reload."
  []
  (stop!)
  (reset! !dispose
    ((e/boot-client {::e/hot-swap true} app/App))))

(defn ^:dev/after-load init
  "Initializes or reloads the browser client in development.

  Shadow-cljs calls this after a code reload so the app remounts cleanly."
  []
  (start!))
