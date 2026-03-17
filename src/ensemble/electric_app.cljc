(ns ensemble.electric-app
  (:require [hyperfiddle.electric3 :as e]
            [hyperfiddle.electric-dom3 :as dom]))

(e/defn App []
  (dom/main
    (dom/props {:class "electric-shell"})
    (dom/h1 (dom/text "Electric Ensemble"))
    (dom/p
      (dom/text
        "Electric Clojure is now wired into the project dependencies and app structure."))
    (dom/p
      (dom/text
        "Next, move MIDI note reception and stave rendering behind this Electric entrypoint."))))
