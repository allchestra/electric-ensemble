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
  [path]
  (when (.exists (io/file path))
    (response/file-response path)))

(defn- handler [{:keys [uri]}]
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
  [_ring-req]
  (e/boot-server {} app/App))

(def app-handler
  (electric-ring/wrap-electric-websocket handler electric-entrypoint))

(defn- ensure-client-build!
  []
  (when-not @shadow-started?
    (shadow-server/start!)
    (shadow/watch :app)
    (reset! shadow-started? true)))

(defn stop!
  []
  (when-let [running @server]
    (.stop running)
    (reset! server nil)))

(defn start!
  []
  (stop!)
  (ensure-client-build!)
  (reset! server (jetty/run-jetty app-handler {:port 8080 :join? false}))
  (println "Electric app available at http://localhost:8080"))

(defn -main
  [& _args]
  (start!)
  @(promise))
