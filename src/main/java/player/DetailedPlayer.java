package player;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import org.jfugue.midi.MidiFileManager;
import org.jfugue.midi.MidiParserListener;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.ManagedPlayer;
import org.jfugue.player.Player;
import org.jfugue.player.SequencerManager;
import org.staccato.StaccatoParser;

import models.measure.note.Dot;
import models.measure.note.Note;
import models.measure.note.notations.Slur;
import models.measure.note.notations.Tied;
import models.Part;
import models.measure.Measure;
import models.measure.attributes.Attributes;
import models.measure.attributes.Clef;

public class DetailedPlayer {
	private List<Note> musicNotes;
	private String musicString;
	private ManagedPlayer player;
	private Sequence sequence;
	private List<List<Double>> time;
	private int BPM = 120;
	private List<Measure> mE;
	private HashMap<Integer,List<List<Note>>> sortedMeas = new HashMap<>();
	private List<List<Note>> current = new ArrayList<>();
	private List<List<Double>> t = new ArrayList<>();
	private List<String> noteString = new ArrayList<>();
	private int notecount = 1; int chordcount = 1;int noteid = 1;
	private List<Integer> noteId = new ArrayList<>();
	private HashMap<Integer,List<String>> stringID = new HashMap<>();
	private int slidermax; private int slidermin;
	
	private HashMap<Integer,List<Note>> meas = new HashMap<>(); 
	private HashMap<Integer,List<List<Double>>> separateTimes = new HashMap<>();
	private HashMap<Integer,List<Integer>> ElementID = new HashMap<>(); 
	
	public DetailedPlayer(String musicString,List<Note> notes, ManagedPlayer p,List<Measure> meas) {
		this.musicString = musicString;
		measuremap(meas);
		player = p;
		musicNotes = notes;
		setTempo();	
		initialize();
	}

	public void initialize() {
		sequence = getSequence(musicString);
		time = new ArrayList<>();
		try {
		player.start(sequence);
		player.pause();
		}catch(MidiUnavailableException e) {
			e.printStackTrace();
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		//player.seek(player.getTickPosition());float t = javax.sound.midi.Sequence.PPQ; System.out.println(t);
		long current = player.getTickPosition();
		long total = player.getTickLength();
		double ratio = current/total;
		
		int end = 0;		//end of slider(will be determined when slider is implemented)
		int beginning = 0;  //start of slider(will be determined when slider is implemented)
		
		long position = (long)(beginning + (end-beginning)*ratio);
		//track position of music when playing
		
		//BPM=120
		//PPQ=128
//		int totaltime = (int) ( ( ((double)60000) / ((double)(128 * 120)) ) * (double)total);
		int totaltime = (int) (sequence.getMicrosecondLength() / 1000);
		double elapsedtime = 0;
		
		int i = 0;int j = 0;int c = 0;
		ListIterator<Note> iterator = musicNotes.listIterator();

		while(iterator.hasNext() && i < musicNotes.size()) {
			Note note = musicNotes.get(i);
			double start = elapsedtime;
			elapsedtime += getNoteDuration(note,i,musicNotes);
			double stop = elapsedtime;

			time.add(new ArrayList<>());
			time.get(i).add(start);
			time.get(i).add(stop);
			i++;

			if(Math.abs(j-i) == mE.get(c).getNotesBeforeBackup().size()) {
				j+= mE.get(c).getNotesBeforeBackup().size();
				this.sortedMeas.put(mE.get(c).getNumber(), this.current);
				this.stringID.put(mE.get(c).getNumber(), noteString);
				this.ElementID.put(mE.get(c).getNumber(), noteId);
				this.current = new ArrayList<>();
				noteString = new ArrayList<>();
				noteId= new ArrayList<>();
				notecount=1;chordcount=1;noteid = 1;
				c++;
			}
		}
		
		int index = 0;
		for(Measure m : mE) {
			int k = 0;
			ListIterator<Note> iterators = m.getNotesBeforeBackup().listIterator();
		
			while(iterators.hasNext() && k < m.getNotesBeforeBackup().size()) {
				t.add(time.get(index));
				index++;
				k++;
			}
			this.separateTimes.put(m.getNumber(), t);
			this.t = new ArrayList<>();
		}
		
		setNoteList();
		//t=null;
	} 
	
	public void play() {
		player.resume();
	}
	public void pause() {
		player.pause();
	}
	public void seek(double sliderPOS) {
		if(player.isPlaying() || player.isPaused()) {
			double ratio  = sliderPOS / (slidermax - slidermin);
			long total = player.getTickLength();
			long time =(long) ((double)player.getTickLength() * ratio);
			player.seek(time);
		}
	}
	
	private List<Note> tiednotes = new ArrayList<Note>(); private int ch=0;private int tie=0;
	
	public double getNoteDuration(Note note, int position,List<Note> in) {
		double beat = 60000/BPM; // duration of quarter note in milliseconds
		double duration = 0;
		boolean hasChord = note.getChord() != null;
		boolean hasTie =note.getNotations() != null && note.getNotations().getTieds() != null;
		boolean hasSlur = note.getNotations() != null && note.getNotations().getSlurs() != null;
		List<Note> notes = new ArrayList<>();
		
		if(hasTie) {
			tiednotes.add(note);tie++;
			boolean isLastNote = position == in.size()-1;
			for(Tied tie : note.getNotations().getTieds()) {
				boolean stop = tie != null && tie.getType().equals("stop");
				if(stop && !isLastNote && (in.get(position+1).getNotations() != null && in.get(position+1).getNotations().getTieds() == null)) {
					this.tie--;this.noteId.add(noteid);noteid++;
					duration = timeOf(tiednotes);
					while(this.tie>0) {
						time.get(position-this.tie).set(1, time.get(position-this.tie).get(0)+duration);
					//	if(t != null) {	t.get(position-this.tie).set(1, t.get(position-this.tie).get(0)+duration); }
						this.tie--;
						this.noteId.add(noteid);noteid++;
					}
					current.add(tiednotes);
					this.noteString.add("N"+notecount); notecount++;
					tiednotes = new ArrayList<>();
					return duration;
				}
				else if(stop && !isLastNote && in.get(position+1).getNotations() == null) {
					this.tie--;this.noteId.add(noteid);noteid++;
					duration = timeOf(tiednotes);
					while(this.tie>0) {
						time.get(position-this.tie).set(1, time.get(position-this.tie).get(0)+duration);
				//	/*	if(t != null) {*/	t.get(position-this.tie).set(1, t.get(position-this.tie).get(0)+duration); 
						this.tie--;
						this.noteId.add(noteid);noteid++;
					}
					current.add(tiednotes);
					this.noteString.add("N"+notecount); notecount++;
					tiednotes = new ArrayList<>();
					return duration;
				}
				else if(stop && isLastNote) {  
					this.tie--;this.noteId.add(noteid);noteid++;
					duration = timeOf(tiednotes);
					while(this.tie>0) {
						time.get(position-this.tie).set(1, time.get(position-this.tie).get(0)+duration);
				//		if(t != null) {	t.get(position-this.tie).set(1, t.get(position-this.tie).get(0)+duration); }
						this.tie--;
						this.noteId.add(noteid);noteid++;
					}
					current.add(tiednotes);
					this.noteString.add("N"+notecount); notecount++;
					tiednotes = new ArrayList<>();
					return duration;
				}
			}
			return duration;
		}
		else if(hasChord) {duration = getChordDuration(note,position,in); return duration;}
		else if(position != in.size()-1 && in.get(position+1).getChord() != null) {ch++;this.noteId.add(noteid);return 0;}
		else if(hasSlur) {
			if(position != -1) {notes.add(note); current.add(notes); this.noteString.add("N"+notecount); notecount++;}
			boolean sameNote = false;
			for(Slur slur : note.getNotations().getSlurs()) {
				if(slur != null && (slur.getType().equals("start") && !sameNote)) {
					duration = beat * 0.03125;
				}
				if(slur != null && (slur.getType().equals("stop"))) {
					sameNote = true;
					if(note.getNotations().getSlurs().size() <= 1) {
						notes.add(note); 
						duration = timeOf(notes);
					}
				}
				if(slur != null && slur.getType().equals("start") && sameNote) {
					duration = beat * 0.03125;
				}
			}
			this.noteId.add(noteid);noteid++;
			return duration;
		}
		else {notes.add(note); 
			current.add(notes);
			this.noteId.add(noteid);noteid++;
			this.noteString.add("N"+notecount); notecount++;
			duration = timeOf(notes);
		return duration;
		}
	}

	private List<Note> chordnotes = new ArrayList<Note>();
	
	private double getChordDuration(Note note,int index,List<Note> in) {
		
		Note prev = in.get(index-1);
		if( prev.getChord() == null) { chordnotes.add(prev); }
		
		chordnotes.add(note);ch++;
		
		if(index == in.size()-1 || in.get(index+1).getChord() == null) {
			double dur;ch--;
			if(note.getNotations() != null && note.getNotations().getSlurs() != null
					&& note.getNotations().getSlurs().size() > 1 && note.getNotations().getSlurs().get(1).getType().equals("start")) {
				dur = (double)500 * 0.03125;for(int i = ch; i > 0; i--) {this.noteId.add(noteid); }
			}else {
				dur = timeOf(chordnotes);
				while(ch>0) {
					time.get(index-ch).set(1, time.get(index-ch).get(0)+dur);
			//	if(t != null) {	t.get(index-ch).set(1, t.get(index-ch).get(0)+dur); }
					ch--;
					this.noteId.add(noteid);
				}
			}
			noteid++;
			current.add(chordnotes);
			this.noteString.add("C"+chordcount); chordcount++;
			chordnotes = new ArrayList<Note>();
			return dur;
		}
		return 0;
	}
	private double timeOf(List<Note> notes) {
		Measure measure = new Measure();
		Attributes a = new Attributes();
		Clef clef;
		if(notes.get(0).getRest() == null && notes.get(0).getUnpitched() != null) {clef = new Clef("percussion",0); }
		else {clef = new Clef("TAB",0); }
		a.setClef(clef); measure.setAttributes(a);
		measure.setNotesBeforeBackup(notes);
		MXLPlayer compose = new MXLPlayer();
		String musicString = compose.getMeasure(measure, "-1", -1);
		Sequence Notes = getSequence(musicString);
		double time = ((double)Notes.getMicrosecondLength())/((double)1000);

		return time;
	}
	
	private Sequence getSequence(String musicString) {
		StaccatoParser staccatoParser = new StaccatoParser();
		MidiParserListener midiParserListener = new MidiParserListener();
		staccatoParser.addParserListener(midiParserListener);
		staccatoParser.parse(musicString);
		Sequence sequence = midiParserListener.getSequence();
		
		return sequence;
	}
	private void setTempo() {
		
//		if(/*BPM is set by user*/) {
//			
//		}else { BPM = 120;}
		
		try {
			Field field = ManagedPlayer.class.getDeclaredField("common");
			field.setAccessible(true);
			
			SequencerManager manager = (SequencerManager)field.get(player);
			Sequencer sequencer = manager.getSequencer();
			sequencer.setTempoInBPM(BPM);
			
		}catch(NoSuchFieldException e) {
			System.out.println("NOT FOUND");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void measuremap(List<Measure> meas) {
		List<Measure> measures = new ArrayList<>();
		for(Measure m: meas) {
			if(m.getNotesBeforeBackup() != null) {
				this.meas.put(m.getNumber(), m.getNotesBeforeBackup());
				measures.add(m);
			}
		}
		mE = measures;
	}
	
	private List<List<Integer>> noteIDList = new ArrayList<>();
	
	private void setNoteList() {
		for(Integer measureID : this.ElementID.keySet()) {
			for(Integer noteID : this.ElementID.get(measureID)) {
				List<Integer> elID = new ArrayList<>();
				elID.add(measureID);
				elID.add(noteID);
				noteIDList.add(elID);
			}
		}
	}
 	
	public List<Integer> getElementID(int index){
		return noteIDList.get(index);
	}
	public List<List<Double>> getTime(){
		return this.time;
	}
//	try {
//	File file = new File("C:\\Users\\kidim\\Desktop\\m\\musik.mid");
//	Pattern p = new Pattern(); p.add(musicString); 
//	MidiFileManager.savePatternToMidi(p, file);
//	}catch(IOException e) {
//		
//	}
	
}
