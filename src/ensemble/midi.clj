(ns ensemble.midi
  (:import
   [javax.sound.midi MidiDevice Synthesizer Sequencer MidiSystem Receiver ShortMessage]
   [java.io File]
   [java.awt Desktop])
  (:require [clojure.datafy :as d]
            [clojure.string :as string]
            [objection.core :as obj]))

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

(obj/defsingleton ::lumi
  ;; open LUMI and register it
  (let [device (-> (search-inputs "LUMI")
                   (first))]
    (obj/register
     (doto device .open)
     {:name "LUMI midi in"
      :stopfn (fn [device]
                (.close device))})))

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

(defn lumi
  []
  (obj/singleton ::lumi))

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

(comment
  (require '[hiccup2.core :as hiccup2]))

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
const { Factory } = Vex.Flow;

const vf = new Factory({ renderer: { elementId: \"output\", width:1000, height:500} });
const score = vf.EasyScore();
const system = vf.System();

system.addStave({
  voices: [
    score.voice(score.notes('(B4 D4)/w'))
  ]
}).addClef(\"treble\")
vf.draw()")]]))

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
  (.setReceiver (transmitter (lumi))
                (reify Receiver
                  (send [this msg timestamp]
                    (println (decode-midi-message msg)))
                  (close [this]))))


(comment
  (clojure.repl.deps/sync-deps))
