
# Technology 

 - Clojure, with clojurescript via electric clojure
 - Use the latest available version of electric clojure
 - Open source. 
 
# Development
 - Work on features in branches
 - Keep each branch as one logical change. Fold fixes, follow-up edits, and adaptations into that same branch change instead of leaving fixup commits.
 - Keep `changelog.md` as a squashed summary of the branch outcome. Rewrite it to describe the final result only, not the sequence of intermediate fixes.
 - Keep `spec.md` as a squashed formal specification of the current intended behaviour. Update it in place so it reflects the latest agreed design rather than accumulating history.
 - Before finalizing a branch, make sure the branch commit, `changelog.md`, and `spec.md` all describe the same final state.
 
# Purpose
 - An application for distributing musical notes over an ensemble. 
 It will take midi input via a keyboard, then disperse it according to a set of rules to different devices. It will be used so that a single musician can improvise with an entire ensemble
 

# Iterations 
 - 0 Can receive midi notes 
 - 1 Can receive midi notes and display in a browser on a stave
 - 2 Will interpret midi notes according to the current key, rendering accidentals correctly
 - 3 TBD

