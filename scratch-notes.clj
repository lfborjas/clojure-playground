;; This buffer is for notes you don't want to save, and for Lisp evaluation.
;; If you want to create a file, visit that file with C-x C-f,
;; then enter the text in that file's own buffer.

adasdfasdfasdf

;; study of a "progress status"
;; assumes that "dates" is a discrete collection of dates, with gaps filled
labyrinthos.web> (def dates [{:date :a :words 5}, {:date :b :words 10}])

(reductions
 (fn [accum d]
     (assoc d :acc
            (+ (:acc accum) (:words d))))
 {:acc 0}
 dates)
;; result is an almost perfect list of: words accumulated, and words per date
({:acc 0} {:date :a, :acc 5, :words 5} {:date :b, :acc 15, :words 10})

;; to generate a range of dates:
;; from: http://www.rkn.io/2014/02/13/clojure-cookbook-date-ranges/


labyrinthos.web> (defn time-range
  "Return a lazy sequence of DateTime's from start to end, incremented
  by 'step' units of time."
  [start end step]
  (let [inf-range (time-period/periodic-seq start step)
        below-end? (fn [t] (time/within? (time/interval start end)
                                         t))]
    (take-while below-end? inf-range)))

labyrinthos.web> (def project-days (time-range (time/date-time 2018 9 28) (time/date-time 2018 10 5) (time/days 1)))
#'labyrinthos.web/project-days
labyrinthos.web> (doall project-days)
(#<DateTime 2018-09-28T00:00:00.000Z> #<DateTime 2018-09-29T00:00:00.000Z> #<DateTime 2018-09-30T00:00:00.000Z> #<DateTime 2018-10-01T00:00:00.000Z> #<DateTime 2018-10-02T00:00:00.000Z> #<DateTime 2018-10-03T00:00:00.000Z> #<DateTime 2018-10-04T00:00:00.000Z>)


;; then it would be a matter of taking the above range and matching it with the query results, somehow?

;; actually, what we store is the date and the words _so far_.
;; the query would have to group by date and give us the _max_ of the date
;; and then we take that and:

labyrinthos.web> (def sessions [{:sofar 0 :date :x} {:sofar 5 :date :a}, {:sofar 15 :date :b}, {:sofar 40 :date :c}])

(map
 (fn [window]
     (assoc (last window) :words
            (- (:sofar (last window)) (:sofar (first window)))))
 (partition 2 1 sessions))

;; a zeroth date must be provided, however, and it'll be ignored
;; perhaps it can be the day before the first session, always? (i.e. the "X" date above)
;; ({:date :a, :words 5, :sofar 5} {:date :b, :words 10, :sofar 15} {:date :c, :words 25, :sofar 40})

;;;;;;; <2018-10-28 Sun> : crossword notes

;; components.cljs

(defn cell [cur-word cur-pos info puzzle]
  (let [val (r/atom "")
            words (words-at puzzle pos)
            move-on (partial next-cell puzzle pos)]
    (fn [cur-word cur-pos {:keys [letter number pos]} _]
        [:span.cw-cell
         {:class [(when (= pos @cur-pos) "selected-cell")
                   (when (contains? #{@cur-word} words) "selected-word")]
         :on-keyup #(reset! val (-> % -.target -.value)) ; and move-on?
         :on-click #(set-pos! puzzle pos)
         :on-focus-lost move-on
         ;; something to do with @revealed-word and @revealed-pos
         ;; same with @autocorrect-enabled
         }
         @val [:span.cw-number number]])))

(defn crossword [puzzle & maybe-initial-data?]
  [:iterate-a-lot])

(defn hint [word]
  [:div.hint [:span @word]])

;; core.cljs

(def state (r/atom {:cur-word nil, :cur-pos nil, :revealed-pos nil, :revealed-word nil, :autocorrect false}))


(def puzzle-size 7)
(def puzzle-data (models/read-puzzle "nyt.edn"))

(def puzzle (models/make-puzzle puzzle-size puzzle-data state))

(def cur-word (r/cursor [:cur-word] state))
(def cur-pos  (r/cursor [:cur-pos] state))
(reset! cur-word (current-word puzzle))
(reset! cur-pos  (current-pos puzzle))

(r/render-component [:div
                     [components/crossword puzzle]
                     [components/hint cur-word]])

;; models.cljs

(defprotocol CrosswordPuzzle
  (words-at  [this pos])
  (set-pos!  [this pos])
  (next      [this pos])
  
  (starting-word [this])
  (starting-pos  [this])
  (cell-rows     [this]))

(declare all-numbers :done)
(declare all-letters :done)
(declare all-words :TODO)
(declare find-next-cell :TODO)

(defn make-puzzle
  "A closure that implements the crossword puzzle protocol"
  [size definition state]
  (let [number-map    (all-numbers definition)
        letter-map    (all-letters definition)
        word-map      (all-words definition)
        all-positions (all-positions size)
        cells (populate-cells number-map letter-map all-positions)
        rows  (partition size cells)]
    (reify CrosswordPuzzle
      (cell-rows [_]
        rows) ;; called by the rendered function
      (words-at [_ pos]
        (get word-map pos))
      (set-pos! [_ pos]
        (reset! (:cur-pos state) pos))
      (set-word! [_ word]
        (reset! (:cur-word state) word))
      (next-cell [this pos words]
        (let [state-change (find-next-cell this pos words)]
          (swap! update state-change state))))))



