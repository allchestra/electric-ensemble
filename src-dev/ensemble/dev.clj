(ns ensemble.dev
  (:require [clojure.java.io :as io]
            [ensemble.electric-app :as app]
            [hyperfiddle.electric-ring-adapter3 :as electric-ring]
            [hyperfiddle.electric3 :as e]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.server :as shadow-server]))

(defonce server (atom nil))
(defonce shadow-started? (atom false))

(defn- file-response
  "Returns a Ring file response when the given path exists.

  Use this to serve static assets without raising on missing files."
  [path]
  (when (.exists (io/file path))
    (response/file-response path)))

(defn- handler [{:keys [uri]}]
  "Serves the HTML shell and compiled frontend assets for local development.

  Requests for `/` return the app shell; requests under `/js` return generated client files."
  (or (if (= uri "/")
        (some-> (file-response "index.html")
                (response/content-type "text/html; charset=utf-8"))
        (let [response (file-response (str "public" uri))]
          (if (= uri "/js/main.js")
            (some-> response
                    (response/content-type "text/javascript; charset=utf-8"))
            response)))
      (response/not-found "Not found")))

(defn- electric-entrypoint
  "Boots the Electric server program for a websocket connection.

  Ring websocket requests use this to attach the browser client to `app/App`."
  [_ring-req]
  (e/boot-server {} app/App))

(def app-handler
  (electric-ring/wrap-electric-websocket handler electric-entrypoint))

(defn- ensure-client-build!
  "Starts the embedded Shadow build watcher once for the dev process.

  Call this before starting Jetty so the browser bundle is kept up to date."
  []
  (when-not @shadow-started?
    (shadow-server/start!)
    (shadow/watch :app)
    (reset! shadow-started? true)))

(defn stop!
  "Stops the local Jetty server if it is running.

  Use this before restarting the dev server in the same JVM."
  []
  (when-let [running @server]
    (.stop running)
    (reset! server nil)))

(defn start!
  "Starts the Electric development environment on localhost.

  This launches the frontend watcher, boots Jetty, and serves the app at port 8080."
  []
  (stop!)
  (ensure-client-build!)
  (reset! server (jetty/run-jetty app-handler {:port 8080 :join? false}))
  (println "Electric app available at http://localhost:8080"))

(defn -main
  "Runs the long-lived Electric development process.

  Use this as the `:main` entrypoint for local app development."
  [& _args]
  (start!)
  @(promise))
