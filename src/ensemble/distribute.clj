(ns ensemble.distribute)

;; Distribution thoughts

;; Need an algorithm for this (not electric dependent)

;; 1. Possible: Notes should be distributed to players within their range (e.g. Soprano, Bass, Violin, Custom Range). 

;; 2. Silence: If there are fewer notes than players, some players will have silence. 

;; 3. Continuity: If someone is performing a note, and an event occurs (new notes), and the note the perofmer on is still held, they should remain on the same note, unless there are fewer performers than notes (in which case, see point 5).

;; 4. Order: If continuity rules do not apply, notes should be 
;;    - by who has been silent the longest
;;    - by pitch

;; ## Example inputs

;; If the input is 

;; ```
;; 1  2  3   4   56  7   8    9
;; C ----------------x
;;    E ---------x
;;       G --x
;;           A----------------x
;; 		       F------x
;; 			      D--------x
;; ```			   

;; Then the output should be 

;; Voice 1 - C---
;; Voice 2 -   E   F  
;; Voice 3 -    G
;; Voice 4 -     A

