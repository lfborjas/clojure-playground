(ns clojure-playground.crossword
  (:require [clojure.string :as s]
            [clojure.set :as set]))


;; Lifted from the NYT crossword for Saturday, 10/13
;; Trying to maximize for ease of computation
;; without needing to provide data that could be computed
;; (like the number, or capitalizing/removing spaces from the solution).
(def example-crossword
  [{:start [0 3]
    :solution "Snap"
    :direction :across
    :hint "Finger-clicking sound"}
   {:start [1 2]
    :solution "Viola"
    :direction :across
    :hint "Instrument in a string quartet"}
   {:start [2 1]
    :solution "Butter"
    :direction :across
    :hint "Stick in the fridge"}
   {:start [3 0]
    :solution "Malware"
    :direction :across
    :hint "It might give you a computer virus"}
   {:start [4 0]
    :solution "Urgent"
    :direction :across
    :hint "Requiring immediate atention"}
   {:start [5 0]
    :solution "Scaly"
    :direction :across
    :hint "Like snake and lizard skin"}
   {:start [6 0]
    :solution "Earl"
    :direction :across
    :hint "___ Grey tea"}

   {:start [0 3]
    :solution "Sit well"
    :direction :down
    :hint "Prove to be pleasing"}
   {:start [0 4]
    :solution "Not any"
    :direction :down
    :hint "Zero"}
   {:start [0 5]
    :solution "Alert"
    :direction :down
    :hint "A 'presidential' one was sent to every phone last week"}
   {:start [0 6]
    :solution "Pare"
    :direction :down
    :hint "Cut the skin from, as a fruit"}
   {:start [1 2]
    :solution "Vulgar"
    :direction :down
    :hint "Crude"}
   {:start [2 1]
    :solution "Barca"
    :direction :down
    :hint "Spanish soccer powerhouse, as they're known"}
   {:start [3 0]
    :solution "Muse"
    :direction :down
    :hint "Artist's inspiration"}])

;; to prove that the above data structure is useful, here's some REPL playing around to validate my entry:

;; clojure-playground.crossword> (map :solution (filter #(= (:direction %) :down) example-crossword))
;; ("Sit well" "Not any" "Alert" "Pare" "Vulgar" "Barca" "Muse")
;; clojure-playground.crossword> (map :solution (filter #(= (:direction %) :across) example-crossword))
;; ("Snap" "Viola" "Butter" "Malware" "Urgent" "Scaly" "Earl")

;; it's also very easy to group by any given attribute

;; clojure-playground.crossword> (clojure.pprint/pprint (group-by :start example-crossword))
;; {[0 6]
;;  [{:start [0 6],
;;    :solution "Pare",
;;    :direction :down,
;;    :hint "Cut the skin from, as a fruit"}],
;;  [0 5]
;;  [{:start [0 5],
;;    :solution "Alert",
;;    :direction :down,
;;    :hint "A 'presidential' one was sent to every phone last week"}],
;;  [3 0]
;;  [{:start [3 0],
;;    :solution "Malware",
;;    :direction :across,
;;    :hint "It might give you a computer virus"}
;;   {:start [3 0],
;;    :solution "Muse",
;;    :direction :down,
;;    :hint "Artist's inspiration"}],
;;  [0 3]
;;  [{:start [0 3],
;;    :solution "Snap",
;;    :direction :across,
;;    :hint "Finger-clicking sound"}
;;   {:start [0 3],
;;    :solution "Sit well",
;;    :direction :down,
;;    :hint "Prove to be pleasing"}],

;; and grouping by start proves helpful:

;; clojure-playground.crossword> (clojure.pprint/pprint (into (sorted-map) (group-by :start example-crossword)))
;; {[0 3]
;;  [{:start [0 3],
;;    :solution "Snap",
;;    :direction :across,
;;    :hint "Finger-clicking sound"}
;;   {:start [0 3],
;;    :solution "Sit well",
;;    :direction :down,
;;    :hint "Prove to be pleasing"}],
;;  [0 4]
;;  [{:start [0 4], :solution "Not any", :direction :down, :hint "Zero"}],
;;  [0 5]
;;  [{:start [0 5],
;;    :solution "Alert",
;;    :direction :down,
;;    :hint "A 'presidential' one was sent to every phone last week"}],
;;  [0 6]
;;  [{:start [0 6],
;;    :solution "Pare",
;;    :direction :down,
;;    :hint "Cut the skin from, as a fruit"}],
;;  [1 2]
;;  [{:start [1 2],
;;    :solution "Viola",
;;    :direction :across,
;;    :hint "Instrument in a string quartet"}
;;   {:start [1 2], :solution "Vulgar", :direction :down, :hint "Crude"}],
;;  [2 1]
;;  [{:start [2 1],
;;    :solution "Butter",
;;    :direction :across,
;;    :hint "Stick in the fridge"}
;;   {:start [2 1],
;;    :solution "Barca",
;;    :direction :down,
;;    :hint "Spanish soccer powerhouse, as they're known"}],
;;  [3 0]
;;  [{:start [3 0],
;;    :solution "Malware",
;;    :direction :across,
;;    :hint "It might give you a computer virus"}
;;   {:start [3 0],
;;    :solution "Muse",
;;    :direction :down,
;;    :hint "Artist's inspiration"}],
;;  [4 0]
;;  [{:start [4 0],
;;    :solution "Urgent",
;;    :direction :across,
;;    :hint "Requiring immediate atention"}],
;;  [5 0]
;;  [{:start [5 0],
;;    :solution "Scaly",
;;    :direction :across,
;;    :hint "Like snake and lizard skin"}],
;;  [6 0]
;;  [{:start [6 0],
;;    :solution "Earl",
;;    :direction :across,
;;    :hint "___ Grey tea"}]}

;; as you can see, the size of the above array is 10,
;; which means that it can assign the numbers automatically:

;; clojure-playground.crossword> (clojure.pprint/pprint (map-indexed #(vector (inc %1) %2) (keys (into (sorted-map) (group-by :start example-crossword)))))
;; ([1 [0 3]]
;;  [2 [0 4]]
;;  [3 [0 5]]
;;  [4 [0 6]]
;;  [5 [1 2]]
;;  [6 [2 1]]
;;  [7 [3 0]]
;;  [8 [4 0]]
;;  [9 [5 0]]
;;  [10 [6 0]])

;; some convenience functions for display, etc.

(defn prep-word
  "Prepares a word for display: all upper-case, no spaces"
  [w]
  (-> w
      (#(s/replace % #"[ ]+" ""))
      s/upper-case))

(defn by-starting-cell
  "Given a crossword, return a sorted map of start->element"
  [crossword]
  (into (sorted-map)
        (group-by :start crossword)))

;; some query functions

(defn all-numbers
  "Given a crossword, determine which cells get which numbers"
  [crossword]
  (into (hash-map)
        (map-indexed #(vector %2 (inc %1))
                     (keys (by-starting-cell crossword)))))

(defn assign-cell-letters
  "Given a crossword entry, determine the cells that belong to it; and map to its letters"
  [entry]
  (let [{[row col] :start
         word :solution
         dir :direction} entry
         pword (prep-word word)
         sz  (count pword)
         vfn #(range % (+ % sz))]
    (zipmap (if (= :across dir)
              ; if across, col varies, row stays the same; vice-versa
              (for [x (vfn col)] [row x])
              (for [x (vfn row)] [x col]))
            (seq pword))))

(defn get-word-cells
  "Given a crossword entry, determine the cells that belong to it; and map to its letters"
  [entry]
  (let [{[row col] :start
         word :solution
         dir :direction} entry
         pword (prep-word word)
         sz  (count pword)
         vfn #(range % (+ % sz))]
    (if (= :across dir)
              ; if across, col varies, row stays the same; vice-versa
              (for [x (vfn col)] [row x])
              (for [x (vfn row)] [x col]))))


(defn all-letters
  "Given a crossword, determine which cells get which letters"
  [crossword]
  (into (hash-map)
        (apply conj (map assign-cell-letters
                         crossword))))

(defn all-positions
  "Given a size, generates all possible positions in a square board"
  [size]
  (for [x (range 0 size) y (range 0 size)] [x y]))

(defn populate-cells
  "Given maps for numbers and letters, and positions, assign"
  [letter-map number-map all-cells]
  (map #(into {} {:letter (get letter-map %)
                  :number (get number-map %)
                  :pos %})
       all-cells))

(defn prep
  "Given a crossword, return a vector of vectors representing
  all rows, with maps representing each cell's letter and number"
  [crossword sz]
  (let [number-map (all-numbers crossword)
        letter-map (all-letters crossword)
        all-cells  (all-positions sz)]
    (partition sz (populate-cells letter-map number-map all-cells))))


(defn cell-str [cell]
  (let [letter (:letter cell)
        number (:number cell)
        number-str (if (some? number) (str "(" number ")") "")
        letter-str (if (some? letter) (str letter) "@")
        num-separator (if (> (count (str number)) 1) "" " ")]
    (cond
     (and letter number) (str number-str num-separator letter-str)
     (nil? number) (str "    " letter-str ""))))

;; TODO: make showing letters optional
(defn print-puzzle
  "ASCII-prints the given puzzle, with numbers and empty squares"
  [puzzle]
  (let [row-sep (apply str (repeat (* (count puzzle) 6) "-"))]
    (println row-sep)
    (dotimes [row (count puzzle)]
      (print "|")
      (doseq [cell (nth puzzle row)]
        (print (str  (cell-str cell) "|")))
      (println)
      (println row-sep))))


;; the above yields some useful stuff:
;; clojure-playground.crossword> (assign-cell-letters (last (filter #(= (:direction %) :down) example-crossword)))
;; {[3 0] \M, [4 0] \U, [5 0] \S, [6 0] \E}
;; clojure-playground.crossword> (assign-cell-letters (first (filter #(= (:direction %) :down) example-crossword)))
;; {[0 3] \S, [1 3] \I, [2 3] \T, [3 3] \W, [4 3] \E, [5 3] \L, [6 3] \L}
;; clojure-playground.crossword> (assign-cell-letters (first example-crossword))
;; {[0 3] \S, [0 4] \N, [0 5] \A, [0 6] \P}
;; clojure-playground.crossword> 


;; prep pretty much does what we want:

;; clojure-playground.crossword> (clojure.pprint/pprint (prep example-crossword 7))
;; (({:letter nil, :number nil}
;;   {:letter nil, :number nil}
;;   {:letter nil, :number nil}
;;   {:letter \S, :number 1}
;;   {:letter \N, :number 2}
;;   {:letter \A, :number 3}
;;   {:letter \P, :number 4})
;;  ({:letter nil, :number nil}
;;   {:letter nil, :number nil}
;;   {:letter \V, :number 5}
;;   {:letter \I, :number nil}
;;   {:letter \O, :number nil}
;;   {:letter \L, :number nil}
;;   {:letter \A, :number nil})
;;  ({:letter nil, :number nil}
;;   {:letter \B, :number 6}
;;   {:letter \U, :number nil}
;;   {:letter \T, :number nil}
;;   {:letter \T, :number nil}
;;   {:letter \E, :number nil}
;;   {:letter \R, :number nil})
;;  ({:letter \M, :number 7}

;; now it's a matter of printing:



;; and voilà, an almost perfect ascii rendering of
;; the puzzle (note that we must give the size of the puzzle
;; to prep because it can't quite infer it from the words:

;; clojure-playground.crossword> (-> example-crossword (#(prep % 7)) print-puzzle)
;; ------------------------------------------
;; |    @|    @|    @|(1) S|(2) N|(3) A|(4) P|
;; ------------------------------------------
;; |    @|    @|(5) V|    I|    O|    L|    A|
;; ------------------------------------------
;; |    @|(6) B|    U|    T|    T|    E|    R|
;; ------------------------------------------
;; |(7) M|    A|    L|    W|    A|    R|    E|
;; ------------------------------------------
;; |(8) U|    R|    G|    E|    N|    T|    @|
;; ------------------------------------------
;; |(9) S|    C|    A|    L|    Y|    @|    @|
;; ------------------------------------------
;; |(10)E|    A|    R|    L|    @|    @|    @|
;; ------------------------------------------
