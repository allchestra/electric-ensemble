(ns ensemble.midi
  (:import
   [javax.sound.midi MidiDevice Synthesizer Sequencer MidiSystem Receiver ShortMessage]
   [java.io File]
   [java.awt Desktop])
  (:require [clojure.datafy :as d]
            [clojure.string :as string]
            [objection.core :as obj]
            [hiccup2.core :as hiccup2]))

(defn devices
  []
  (seq (MidiSystem/getMidiDeviceInfo)))

(defn search-inputs
  "Find the input with the given name"
  [s]
  (->> (for [device (devices)
             :let [{:keys [name description]} (bean device)
                   midi-device (MidiSystem/getMidiDevice device)]
             :when (and (or (string/includes? (string/lower-case name) (string/lower-case s))
                           (string/includes? (string/lower-case description) (string/lower-case s)))
                        (not (zero? (.getMaxTransmitters midi-device))))]
         midi-device)))

(def my-inputs
  [{:search "LUMI"
    :alias ::piano
    :name "Piano M"}
   {:search "Seaboard"
    :alias ::seaboard
    :name "Seaboard"}])

(defn register-instruments
  []
  (doseq [{:keys [search alias name]} my-inputs
          :let [device (-> (search-inputs search)
                           (first))]
          :when device]
    (obj/register
     (doto device .open)
     {:name name
      :aliases [alias]
      :stopfn (fn [device]
                (.close device))}))
  (obj/status))

(defn transmitter
  "Get the transmitter for the device, registering it"
  [device]
  (let [{:keys [id]} (obj/id device)]
    (or (obj/object [:transmitter id])
        (let [transmitter (.getTransmitter device)]
          (obj/register
           (.getTransmitter device)
           {:name (str "MIDI Transmitter for " id)
            :alias [:transmitter id]
            :stopfn (fn [transmitter]
                      (.close transmitter))})))))

(def midi-command
  {ShortMessage/CHANNEL_PRESSURE :channel-pressure
   ShortMessage/CONTROL_CHANGE :control-change
   ShortMessage/NOTE_OFF :note-off
   ShortMessage/NOTE_ON :note-on
   ShortMessage/PITCH_BEND :pitch-bend
   ShortMessage/POLY_PRESSURE :poly-pressure
   ShortMessage/PROGRAM_CHANGE :program-change})

(defn- calculate-14-bit-value
  "Calculates the the 14 bit value given two integers 
representing the high and low parts of a 14 bit value."
  [lower higher]
  (bit-or (bit-and lower 0x7f)
          (bit-shift-left (bit-and higher 0x7f) 
                          7)))


(defn interpret-pitch
  "Interprets a pitch as a note in a key"
  [key pitch]
  (let [modal-adjustments {1 :flat
                           3 :flat
                           6 :sharp
                           8 :flat
                           10 :flat}]))

(defn decode-midi-message
  "Takes a message, and returns a map"
  [message]
  (let [command (-> (.getCommand message) midi-command)
        channel (.getChannel message)
        data1 (.getData1 message)
        data2 (.getData2 message)]
    {:command command
     :channel channel
     :data1 data1
     :data2 data2}))

(def notation-example
  (hiccup2/html
   [:html
    [:head
     [:script {:src "https://cdn.jsdelivr.net/npm/vexflow@4.2.2/build/cjs/vexflow.js"}]]
    [:body
     [:div {:class "vexbox"}
      [:div {:id "output"}]]]
    [:script {:type "text/javascript"}
     (hiccup2/raw
        "
const { Renderer, Stave, StaveNote, Formatter, Voice } = Vex.Flow;

const div = document.getElementById(\"output\");
const renderer = new Renderer(div, Renderer.Backends.SVG);
renderer.resize(1000, 200);
const context = renderer.getContext();

// Create stave at a specified position and size
const stave = new Stave(100, 40, 800);
stave.addClef(\"treble\").addKeySignature(\"G\");
stave.setContext(context).draw();

// Create a whole note chord
const notes = [new StaveNote({ keys: [\"b/4\", \"d/4\"], duration: \"w\" })];

// Create a voice
const voice = new Voice({ num_beats: 4, beat_value: 4 });
voice.addTickables(notes);

// Use Formatter to center the note horizontally
new Formatter().joinVoices([voice]).formatToStave([voice], stave, { align_rests: true, padding: stave.getWidth() / 2 - 30 });

// Render voice
voice.draw(context, stave);
"
)]]))

(defn open-html
  "Writes the html to a file and opens it in the default browser"
  [html]
  (let [file (File/createTempFile "test" ".html")]
    (spit file html)
    (.browse (Desktop/getDesktop) (.toURI file))
    (.getPath file)))


(comment

  (open-html notation-example)
  (transmitter (lumi))
  (obj/status)
  (obj/stop-all!)
  (.setReceiver (transmitter (obj/object ::seaboard))
                (reify Receiver
                  (send [this msg timestamp]
                    (println "hey")
                    )
                  (close [this]))))

(defn tap-tempo
  []
  (let [state (atom '())]
    (fn tap []
      (let [[a b] (swap! state (fn [state]
                                 (take 2 (conj state (java.time.Instant/now)))))]
        (when (and a b)
          (let [gap (- (java.time.Instant/.toEpochMilli a)
                       (java.time.Instant/.toEpochMilli b))]
            (int (/ (* 60 1000)
                     gap))))))))

(defn track-tempo
  [device]
  (let [pulse (tap-tempo)]
    (.setReceiver (transmitter device)
                  (reify Receiver
                    (send [this msg timestamp]
                      (let [{:keys [command]} (decode-midi-message msg)]
                        (when (= command :note-on)
                          (println (pulse)))))
                    (close [this])))))


(comment
  (track-tempo (obj/object ::seaboard))
  (clojure.repl.deps/sync-deps)
  )
