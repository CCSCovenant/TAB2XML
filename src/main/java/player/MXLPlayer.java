package player;

import converter.Score;
import custom_exceptions.TXMLException;
import models.Part;
import models.ScorePartwise;
import models.measure.Measure;
import models.measure.attributes.Attributes;
import models.measure.attributes.Clef;
import models.measure.barline.BarLine;
import models.measure.note.Dot;
import models.measure.note.Note;
import models.measure.note.notations.Slur;
import models.measure.note.notations.Tied;
import models.part_list.ScorePart;
import org.jfugue.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MXLPlayer{
	private ScorePartwise score; private List<Note> notes= new ArrayList<>(); List<Measure> scoreMeasure= new ArrayList<>();
	private String clef;
	private Player player = new Player();
	private HashMap<String,ScorePart> scorePartMap = new HashMap<>();
	public MXLPlayer(Score score) throws TXMLException {
		this.score = score.getModel();						
		initPartList();
	}
	public MXLPlayer(){ }
	public List<Note> getNotes(){ return notes; }
	public List<Measure> getMeasure() { return scoreMeasure; }
	/**
	 * this method will play music from given duration.
	 *
	 * @param partID which part should player start
	 * @param measureID which measure should player start
	 * @param duration  when should player start in a measure.
	 * */
	public void play(int partID,int measureID, int duration){
		StringBuilder musicString = new StringBuilder();
		int partCount = 0;
		for (Part part:score.getParts()){
			if (partCount>partID){
				musicString.append(getPart(part,-1,-1));
			}else if (partCount==partID){
				musicString.append(getPart(part,measureID,duration));
			}
			partCount++;
		}
		ThreadPlayer threadPlayer = new ThreadPlayer("player-1");
		threadPlayer.start(musicString.toString());
	}
	public String getString(int partID,int measureID, int duration){
		StringBuilder musicString = new StringBuilder();
		int partCount = 0;
		for (Part part:score.getParts()){
			if (partCount>partID){
				musicString.append(getPart(part,-1,-1));
			}else if (partCount==partID){
				musicString.append(getPart(part,measureID,duration));
			}
			partCount++;
		}
		return musicString.toString();
	}
	public void initPartList(){
		List<ScorePart> list = score.getPartList().getScoreParts();
		for (ScorePart scorePart:list){
			scorePartMap.put(scorePart.getId(),scorePart);
		}
	}private int measures=0;boolean partOfRepeat = false;
	public String getPart(Part part,int measureID, int duration){
		StringBuilder musicString = new StringBuilder(); 
		List<Measure> repeats = new ArrayList<>(); 				
		
			int measureCount = 0; 								measures=part.getMeasures().size();
			for (Measure measure:part.getMeasures()){	scoreMeasure.add(measure);
				if (measureCount>measureID){
					musicString.append(getMeasure(measure,part.getId(),-1));
					musicString.append(getRepeats(part, measure, -1,repeats));
				}else if (measureCount==measureID){
					musicString.append(getMeasure(measure,part.getId(),duration));
					musicString.append(getRepeats(part, measure, duration,repeats));
				}
				measureCount++;
			}

		return musicString.toString();
	}private int m_count = 0;
	public String getMeasure(Measure measure,String partID,int duration){
		StringBuilder musicString = new StringBuilder();
		int durationCount = 0;
		if (measure.getNotesBeforeBackup()!=null){m_count++;
			for(Note note: measure.getNotesBeforeBackup()) {if(m_count<=measures) {notes.add(note);}
				if (durationCount < duration) {
					durationCount += note.getDuration();
				} else {
					if (note.getChord() == null && musicString.length() > 0 && musicString.charAt(musicString.length() - 1) == '+') {
						musicString.deleteCharAt(musicString.length() - 1);
						musicString.append(" ");
					}
					if (this.clef != null || measure.getAttributes().getClef() != null) {
						if (note.getChord() != null && musicString.length() > 0 && musicString.charAt(musicString.length() - 1) == '+') {
							if(clef.equals("percussion")) {
								musicString.append("[" + getInstrument(note.getInstrument().getId()) + "] ");
							}
						} else {
							musicString.append(getNoteDetails(note));
						}
						if (clef == null) {
							this.clef = measure.getAttributes().getClef().getSign();
						}
						if (clef.equals("percussion")) {
							if (note.getRest() != null) {
								musicString.append("R");
								musicString.append(getNoteDuration(note));
							}else {
								musicString.deleteCharAt(musicString.length() - 1);
								musicString.append(getNoteDuration(note));
							}
							musicString.append(getDots(note)); 
							addTies(musicString, note); 
						} 
						else if (clef.equals("TAB")) {
							if (note.getRest() != null) {
								musicString.append("R");
								musicString.append(getNoteDuration(note));
							} else {
								musicString.append(note.getPitch().getStep());
								
								if(note.getPitch().getAlter() != null) {
									if(note.getPitch().getAlter() == 1) musicString.append("#");
									else if(note.getPitch().getAlter() == -1) musicString.append("b");
								}
								musicString.append(note.getPitch().getOctave());
								
								if(note.getNotations() != null && note.getNotations().getTechnical() != null
										&& note.getNotations().getTechnical().getBend() != null) {
								musicString.append(bendNote(note, false));
								}
								else if(note.getGrace() != null) {
									if(note.getNotations() != null && note.getNotations().getSlurs() != null) {
										musicString.append("o");
									}else {
										musicString.append("s");
									}
									addSlur(musicString,note);
								} else {
									musicString.append(getNoteDuration(note));
									addSlur(musicString,note);
									musicString.append(getDots(note));
									addTies(musicString, note);
								}

							}
						}
						musicString.append(" ");

						if (note != measure.getNotesBeforeBackup().get(measure.getNotesBeforeBackup().size() - 1)) {
							musicString.deleteCharAt(musicString.length() - 1);
							musicString.append("+");
						}
					}
				}			
			}

		}
		return musicString.toString();
	}
	
	public static String composeMusicSting(List<Note> notes) {
		Measure measure = new Measure();
		Attributes a = new Attributes();
		Clef clef;
		if(notes.get(0).getRest() == null && notes.get(0).getUnpitched() != null) {clef = new Clef("percussion",0); }
		else {clef = new Clef("TAB",0); }
		a.setClef(clef); measure.setAttributes(a);
		measure.setNotesBeforeBackup(notes);
		MXLPlayer compose = new MXLPlayer();
		String musicString = compose.getMeasure(measure, "-1", -1);
		
		return musicString;
	}
	
	public String getNoteDetails(Note note) {
		StringBuilder musicString = new StringBuilder();
		String voice;
		String instrument;
		
		//unpitched notes are generally used in music that contain a percussion clef
		//We need to use the appropriate voice for percussive notes (V9)
		if(note.getUnpitched() != null) {
			voice = "V9";
		}
		else {
			voice = "V" + note.getVoice();
		}
		
		//The MIDI instrument code for acoustic guitar is I25
		if(note.getInstrument() == null || note.getInstrument().getId().equals("")) {
			instrument = "I25";
		}//instruments for percussive notes are in the form '[name_of_instrument]' 
		else { instrument = "[" + getInstrument(note.getInstrument().getId()) + "]";
		}
		
		musicString.append(voice + " " + instrument + " ");
		
		return musicString.toString();
	}
	

	public char getNoteDuration(Note note) {
		if (note.getType()!=null){
			if(note.getType().equals("whole")) { return 'w'; }
			else if(note.getType().equals("half")) { return 'h'; }
			else if(note.getType().equals("quarter")) { return 'q'; }
			else if(note.getType().equals("eighth")) { return 'i'; }
			else if(note.getType().equals("16th")) { return 's'; }
			else if(note.getType().equals("32nd")) { return 't'; }
			else if(note.getType().equals("64th")) { return 'x'; }
			else if(note.getType().equals("128th")) { return 'o'; }
			else { return 'q'; }
		} else { return 'q'; }
	}
	
	public String getDots(Note note) {
		StringBuilder musicString = new StringBuilder();
		if(note.getDots() != null) {
			for(Dot dot : note.getDots()) {
				if(dot != null) {
					musicString.append('.');
				}
			}
		}
		return musicString.toString();
	}
	
	public String getInstrument(String InstrumentId) {
		if(InstrumentId.equals("P1-I47")) { return "OPEN_HI_HAT"; }
		else if(InstrumentId.equals("P1-I52")) { return "RIDE_CYMBAL_1"; }
		else if(InstrumentId.equals("P1-I53")) { return "CHINESE_CYMBAL"; }
		else if(InstrumentId.equals("P1-I43")) { return "CLOSED_HI_HAT"; }
		else if(InstrumentId.equals("P1-I46")) { return "LO_TOM"; }
		else if(InstrumentId.equals("P1-I44")) { return "HIGH_FLOOR_TOM"; }
		else if(InstrumentId.equals("P1-I54")) { return "RIDE_BELL"; }
		else if(InstrumentId.equals("P1-I36")) { return "BASS_DRUM"; }
		else if(InstrumentId.equals("P1-I50")) { return "CRASH_CYMBAL_1"; }
		else if(InstrumentId.equals("P1-I39")) { return "ACOUSTIC_SNARE"; }
		else if(InstrumentId.equals("P1-I42")) { return "LO_FLOOR_TOM"; }
		else if(InstrumentId.equals("P1-I48")) { return "LO_MID_TOM"; }
		else if(InstrumentId.equals("P1-I45")) { return "PEDAL_HI_HAT"; }
		/*More could be added later on*/
		else { return "GUNSHOT"; }//default for now
	}

	public void addTies(StringBuilder musicString, Note note) {
		int indextoCheck = musicString.length() - 1; 
		if(note.getNotations() != null && note.getNotations().getTieds() != null) {
			//This loop looks for the position of the last note in the music string
			for(int i = 1; i <= 10; i++) {
				if(musicString.charAt(musicString.length()-i) != '.' && musicString.charAt(musicString.length()-i) == getNoteDuration(note)) {
					indextoCheck = musicString.length() - i;
					break;
				}
			}
			//Once the last note is found, this loop checks if that note is tied to another 
			//note and if it is the end or middle of the tie.
			//If so, then a '-' is added before the note
			for(Tied tie : note.getNotations().getTieds()) {
				if(tie != null && (tie.getType().equals("stop") || tie.getType().equals("continue"))) {
					musicString.replace(indextoCheck, indextoCheck + 1, "-" + getNoteDuration(note));
					break;
				}
			}
		}
		//Checks if the last note is tied to another note and if it is the start or middle of the tie.
		//There is no need to look for the exact location of the note because the '-' can be added after the 
		//note's dots(if there are any)
		 if(note.getNotations() != null && note.getNotations().getTieds() != null) {
			for(Tied tie : note.getNotations().getTieds()) {
				if(tie != null && (tie.getType().equals("start") || tie.getType().equals("continue"))) {
					musicString.append("-");
					break;
				}
			}
			
		}
	}
	private String getRepeats(Part part,Measure measure, int duration, List<Measure> repeats) {
		/* Method checks whether the measure or group of measures is repeated then 
		 * appends the repeats to the musicString
		 */
		StringBuilder musicString = new StringBuilder();
		
		if(measure.getBarlines() != null) {
			boolean sameMeasure = false;
			for(BarLine barline : measure.getBarlines()) {
				//if-statement to avoid null-pointer exception
				if(barline != null && barline.getRepeat() != null) {
					//"forward" determines the beginning of a repeat
					if(barline.getRepeat().getDirection().equals("forward")) {
						repeats.add(measure); 
						partOfRepeat = true; 
						sameMeasure = true;
					}
					//"backward" determines the end of a repeat
					if(barline.getRepeat().getDirection().equals("backward") && !sameMeasure) {
						repeats.add(measure); 
						partOfRepeat = false;
					}
					//when the measure is part of repeat but not the beginning or end
					else if(partOfRepeat && !sameMeasure) {
						repeats.add(measure);
					}
					
					if(barline.getRepeat().getDirection().equals("backward")) {
						//'times' is the number of times the measure is played. It is a string so it needs to be converted to integer
						int times = Integer.parseInt(barline.getRepeat().getTimes());
						for(int i = 1; i < times; i++) {
							for(Measure currentMeasure: repeats) {measures++;
								musicString.append(getMeasure(currentMeasure,part.getId(),duration));
							}
						}
						
						break;
					}
				}
			}	
		}
		return musicString.toString();
	}
	
	private String bendNote(Note note, boolean isInChord) {
		StringBuilder musicString = new StringBuilder();
//	      musicString.append(" T100 V0 L0 I25  :CON(100,0) :CON(101,0) :CON(6,1) ");
//	      musicString.append(note.getPitch().getStep());
//			
//			if(note.getPitch().getAlter() != null) {
//				if(note.getPitch().getAlter() == 1) musicString.append("#");
//				else if(note.getPitch().getAlter() == -1) musicString.append("b");
//			}
//			musicString.append(note.getPitch().getOctave() +" R3 L1");
//	        int PW = 8192; // Default Pitch Bend
//	        int steps=250;  // Let us consider 250 steps in one semitone
//	        double duration = 0.25/steps;  // Duration of each step
//	        int PWValue=8192/steps;        // This is the value of pitch bend to be changed gradually
//	        for(int i=0;i<steps;i++) // S1 to R1
//	        {
//	             PW=PW+PWValue;
//	             musicString.append(" R/"+duration +" :PW("+PW+") ");
//	        }
	      
		double time;
		if(note.getType().equals("half")) time = 0.5;
		else time = 0.25;
		
		int PB = 8192; // Default Pitch Bend
		int steps=20; // Let us consider 20 steps in one semitone
		double duration = time/steps; // Duration of each step
		int PWValue=8192/steps; // This is the value of pitch bend to be changed gradually
		
		if(note.getNotations() != null && note.getNotations().getTechnical() != null
				&& note.getNotations().getTechnical().getBend() != null) {
			double bendalter = note.getNotations().getTechnical().getBend().getBendAlter();
			PWValue = 8192/(int)(steps/(2*bendalter));
		}
//		if(isInChord) musicString.append("");
//		else {
		
//			musicString.append("x "+note.getPitch().getStep());
//			
//			if(note.getPitch().getAlter() != null) {
//				if(note.getPitch().getAlter() == 1) musicString.append("#");
//				else if(note.getPitch().getAlter() == -1) musicString.append("b");
//			}
//			musicString.append(note.getPitch().getOctave());
			musicString.append("+");
			
	//	}
//			for(int i=0;i<(steps/4);i++) 
//			{
//				musicString.append("R/"+duration +" :PW("+PB+") ");
//			}
//		for(int i=0;i<(steps/2);i++) // S1 to R1
//		{
//		PB+=PWValue;
//		musicString.append("R/"+duration +" :PW("+PB+") ");
//		}
//		musicString.append(":PW(8192) ");
//		System.out.println(PWValue);
			
			for(int i=0;i<(steps*0.25);i++) 
			{
				musicString.append("R/"+duration +" :PW("+PB+") ");
			}
			if(PWValue == 1638) PB = 8190;
			else if(PWValue == 819) PB=4914;
		for(int i=0;i<(steps*0.8);i++) // S1 to R1
		{
		PB+=PWValue;
		musicString.append("R/"+duration +" :PW("+PB+") ");
		}
		//musicString.append(":PW(40952) ");
		musicString.append(":PW(8192) ");
		System.out.println(PWValue);
	//	musicString.append("V1 I25 Rh");
		return musicString.toString();
	}
	
	private int index=-1;
	private void addSlur(StringBuilder musicString, Note note) {
		int indextoCheck = musicString.length() - 1; 
		String current=""; boolean sameNote = false;
		if(note.getNotations() != null && note.getNotations().getSlurs() != null) {
			//This loop looks for the position of the last note in the music string
			for(int i = 1; i <= 10; i++) {
				current+=musicString.charAt(musicString.length()-i);
				if(note.getPitch().getStep().equals(current)) {
					indextoCheck = musicString.length() - i;
					break;
				}
				current="";
			}
			for(Slur slur : note.getNotations().getSlurs()) {
				if(slur != null && (slur.getType().equals("stop"))) {
					sameNote = true;
					
				}
				else if(slur.getType().equals("start") && sameNote) {
					musicString.replace(index-1, index, "o");
					for(int i=indextoCheck; i<musicString.length(); i++) {
						if(musicString.charAt(i) == getNoteDuration(note)) {
							musicString.replace(i, i + 1, "o");
							break;
						}
					}
					index=-1;
					sameNote=false;
				}
			}
		}
		
		 if(note.getNotations() != null && note.getNotations().getSlurs() != null) {
			for(Slur slur : note.getNotations().getSlurs()) {
				 if(slur != null && (slur.getType().equals("start"))) {
					index=musicString.length();
					break;
				}
			}
			
		}
	}
}
