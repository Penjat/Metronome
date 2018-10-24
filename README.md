# Metronome
Versatile Metronome for Android

This is a metronome I wrote for Anddroid.  I wanted to create something that could do a wide range range of rhythms and speeds.

This metronome allows for complicated sub divisions and skipped beats.  

The android timing and play sound functions proved to be not accurate enough for fast tempos (anything above 200 bpm sounded choppy).  My solution was generating the binary data for a wav file and then loop that file.  This allowed for extremly accurate timing and no limit to how fast the tempo could go (I set the maximum arbitraraly at 600 bpm).  
