
# Electric Ensemble

**Play a live ensemble using midi.**

As a player - play a midi instrument, and the notes will be distributed to performers, creating a real-world ensemble that can be played over midi. 

As a performer - join a real world session, and subscribe to play or sing the notes from the player. 
## Distribution thoughts

1. Possible: Notes should be distributed to players within their range (e.g. Soprano, Bass, Violin, Custom Range). 

2. Silence: If there are fewer notes than players, some players will have silence. 

3. Continuity: If someone is performing a note, and an event occurs (new notes), and the note the perofmer on is still held, they should remain on the same note, unless there are fewer performers than notes (in which case, see point 5).

4. Order: If continuity rules do not apply, notes should be distributed in order of pitch. 

5. Tie-break: If there are more notes than players, the oldest note should be dropped. 

## Example inputs

If the input is 

```
1  2  3   4   56  7   8    9
C ----------------x
   E ---------x
      G --x
          A----------------x
		       F------x
			      D--------x
```			   
		  
## Plan 

### Solo
* [x] Receiving midi input events
* [x] Render notes

### With Electric Team
* [ ] Port to electric starter
* [ ] Show live notes in browser
* [ ] Split notes across different staves (single screen)
* [ ] Allow player to join the session and choose their range
* [ ] Split across machines


## Extension
Use pressure to send other messages e.g. volume, articulation. 

