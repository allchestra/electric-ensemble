(ns ensemble.dev
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]))

(defonce server (atom nil))

(defn- handler [{:keys [uri]}]
  (case uri
    "/" (-> (response/file-response "index.html")
            (response/content-type "text/html; charset=utf-8"))
    "/js/main.js" (-> (response/file-response "js/main.js")
                      (response/content-type "text/javascript; charset=utf-8"))
    (response/not-found "Not found")))

(defn stop!
  []
  (when-let [running @server]
    (.stop running)
    (reset! server nil)))

(defn start!
  []
  (stop!)
  (reset! server (jetty/run-jetty handler {:port 8080 :join? false}))
  (println "Electric shell available at http://localhost:8080"))

(defn -main
  [& _args]
  (start!)
  @(promise))
