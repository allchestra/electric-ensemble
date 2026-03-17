(ns ensemble.electric-app
  (:require [hyperfiddle.electric3 :as e]
            [hyperfiddle.electric-dom3 :as dom])
  #?(:clj
     (:import [java.util.concurrent Executors ThreadFactory TimeUnit])))

(def chord-cycle
  [{:name "D major"
    :notes ["d/4" "f#/4" "a/4"]}
   {:name "C major"
    :notes ["c/4" "e/4" "g/4"]}])

#?(:clj
   (do
     (defonce !active-chord (atom 0))

     (defn- start-chord-clock []
       "Starts the server-side chord timer for the demo score.

       The clock advances the shared chord state every four seconds so connected clients stay in sync."
       (let [executor (Executors/newSingleThreadScheduledExecutor
                        (reify ThreadFactory
                          (newThread [_ runnable]
                            (doto (Thread. runnable "electric-ensemble-chord-clock")
                              (.setDaemon true)))))]
         (.scheduleAtFixedRate executor
           (reify Runnable
             (run [_]
               (swap! !active-chord #(mod (inc %) (count chord-cycle)))))
           4
           4
           TimeUnit/SECONDS)
         executor))

     (defonce chord-clock (start-chord-clock))))

#?(:cljs
   (def !active-chord nil))

#?(:cljs
   (defn- render-staff!
     "Draws the current chord on a treble stave with a G major key signature.

     Call this with a DOM element and a chord map whenever the displayed score should refresh."
     [element {:keys [notes]}]
     (let [flow (.-Flow js/Vex)
           Renderer (.-Renderer flow)
           Stave (.-Stave flow)
           StaveNote (.-StaveNote flow)
           Voice (.-Voice flow)
           Formatter (.-Formatter flow)]
       (set! (.-innerHTML element) "")
       (let [renderer (Renderer. element (.-SVG (.-Backends Renderer)))
             context (.getContext renderer)
             stave (Stave. 36 26 420)
             note (StaveNote. (clj->js {:keys notes
                                        :duration "w"}))
             voice (Voice. (clj->js {:num_beats 4
                                     :beat_value 4}))
             formatter (Formatter.)]
         (.resize renderer 500 180)
         (.addClef stave "treble")
         (.addKeySignature stave "G")
         (.setContext stave context)
         (.draw stave)
         (.addTickables voice #js [note])
         (.joinVoices formatter #js [voice])
         (.formatToStave formatter #js [voice] stave
           (clj->js {:align_rests true
                     :padding (- (/ (.getWidth stave) 2) 24)}))
         (.draw voice context stave)))))

(e/defn ScoreView
  "Renders the current chord label and stave in the browser.

  Pass a chord map with `:name` and `:notes` to show the current score state."
  [chord]
  (dom/div
    (dom/props {:class "score-view"
                :style {:background "#f6f1e8"
                        :border "1px solid #d7cab5"
                        :border-radius "18px"
                        :padding "1.5rem"
                        :box-shadow "0 18px 40px rgba(38, 26, 12, 0.08)"}})
    (dom/h2
      (dom/props {:style {:font-size "1rem"
                          :letter-spacing "0.08em"
                          :margin "0 0 0.75rem 0"
                          :text-transform "uppercase"}})
      (dom/text (:name chord)))
    (dom/p
      (dom/props {:style {:margin "0 0 1rem 0"
                          :color "#5f4c34"}})
      (dom/text "Alternates with the neighbouring triad every four seconds."))
    (dom/div
      (dom/props {:style {:min-height "180px"}})
      (e/client
        (e/drain
          (render-staff! dom/node chord))))))

(e/defn App
  "Mounts the Electric demo app into the browser root element.

  The app watches server-owned chord state and presents it as a client-rendered VexFlow score."
  []
  (e/$ dom/With
    (e/client (.getElementById js/document "app"))
    (e/fn []
      (let [chord (e/server (get chord-cycle (e/watch !active-chord)))]
        (dom/main
         (dom/props {:class "electric-shell"
                     :style {:font-family "\"Iowan Old Style\", \"Palatino Linotype\", serif"
                              :padding "3rem 1.5rem 4rem"
                              :margin "0 auto"
                              :max-width "640px"
                              :color "#24180c"}})
          (dom/h1
            (dom/props {:style {:font-size "2.6rem"
                                :margin "0 0 0.75rem 0"}})
            (dom/text "Electric Ensemble"))
          (dom/p
            (dom/props {:style {:font-size "1.1rem"
                                :line-height "1.6"
                                :margin "0 0 1.5rem 0"
                                :color "#5f4c34"}})
            (dom/text "Server state alternates between a D-major and C-major triad; VexFlow renders the stave in the browser."))
          (ScoreView chord))))))
