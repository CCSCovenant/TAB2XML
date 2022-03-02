package player;

import converter.Score;
import custom_exceptions.TXMLException;
import models.Part;
import models.ScorePartwise;
import models.measure.Measure;
import models.measure.attributes.Attributes;
import models.measure.attributes.Clef;
import models.measure.note.Dot;
import models.measure.note.Note;
import models.measure.note.notations.Tied;
import models.part_list.PartList;
import models.part_list.ScorePart;
import org.jfugue.player.Player;

import java.util.HashMap;
import java.util.List;

public class MXLPlayer{
	private ScorePartwise score;
	private Player player = new Player();
	private HashMap<String,ScorePart> scorePartMap = new HashMap<>();
	private String clef;
	public MXLPlayer(Score score) throws TXMLException {
		this.score = score.getModel();
	}
	/**
	 * this method will play music from given duration.
	 *
	 * @param partID which part should player start
	 * @param measureID which measure should player start
	 * @param duration  when should player start in a measure.
	 * */
	public void play(int partID,int measureID, int duration){
		initPartList();
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
		player.play(musicString.toString());
	}
	public void initPartList(){
		List<ScorePart> list = score.getPartList().getScoreParts();
		for (ScorePart scorePart:list){
			scorePartMap.put(scorePart.getId(),scorePart);
		}
	}
	public String getPart(Part part,int measureID, int duration){
		StringBuilder musicString = new StringBuilder();
			int measureCount = 0;
			for (Measure measure:part.getMeasures()){
				if (measureCount>measureID){
					musicString.append(getMeasure(measure,part.getId(),-1));
				}else if (measureCount==measureID){
					musicString.append(getMeasure(measure,part.getId(),duration));
				}
				measureCount++;
			}

		return musicString.toString();
	}
	public String getMeasure(Measure measure,String partID,int duration){
		StringBuilder musicString = new StringBuilder();
		int durationCount = 0;
	
		for(Note note: measure.getNotesBeforeBackup()) {
			
			if(durationCount < duration) {
				durationCount += note.getDuration();
			}
			else {
				if(note.getChord() == null && musicString.length() > 0 && musicString.charAt(musicString.length()-1) == '+') {
					musicString.deleteCharAt(musicString.length()-1);
					musicString.append(" ");
				}
				if(this.clef != null || measure.getAttributes().getClef() != null ) {
					if(note.getChord() != null && musicString.length() > 0 && musicString.charAt(musicString.length()-1) == '+') {

					}else {
						musicString.append(getNoteDetails(note));
					}
					if(clef == null) {
						this.clef = measure.getAttributes().getClef().getSign();
					}
					if(clef.equals("percussion")) {
						if(note.getRest() != null) {
							musicString.append("R");
						}
					//	musicString.append(note.getUnpitched().getDisplayStep());
					//	musicString.append(note.getUnpitched().getDisplayOctave());
						musicString.deleteCharAt(musicString.length()-1);
						musicString.append(getNoteDuration(note));
						musicString.append(getDots(note));
					}
					else if(clef.equals("TAB")){
						if(note.getRest() != null) {
							musicString.append("R");
						}
						else {
							musicString.append(note.getPitch().getStep());
							musicString.append(note.getPitch().getOctave());
							
							if(note.getGrace() != null) {
								musicString.append("i");
							}else {
								musicString.append(getNoteDuration(note));
								musicString.append(getDots(note));
							}
							
						}
					}
					musicString.append(" ");

					if(note.getChord() != null && note != measure.getNotesBeforeBackup().get(measure.getNotesBeforeBackup().size()-1)) { 
						musicString.deleteCharAt(musicString.length()-1);
						musicString.append("+");
					}
				}
			}
		}
		return musicString.toString();
	}
	
	public String getNoteDetails(Note note) {
		StringBuilder musicString = new StringBuilder();
		String voice;
		String instrument;
		
		if(note.getUnpitched() != null) {
			voice = "V9";
		}
		else {
			voice = "V" + note.getVoice();
		}

		if(note.getInstrument() == null || note.getInstrument().getId().equals("")) {
			instrument = "I25";
		}
		else { instrument = "[" + getInstrument(note.getInstrument().getId()) + "]";
		}
		
		musicString.append(voice + " " + instrument + " ");
		
		return musicString.toString();
	}
	
	public String getNoteDuration(Note note) {
		if(note.getType().equals("whole")) { return "w"; }
		else if(note.getType().equals("half")) { return "h"; }
		else if(note.getType().equals("quarter")) { return "q"; }
		else if(note.getType().equals("eighth")) { return "i"; }
		else if(note.getType().equals("16th")) { return "s"; }
		else if(note.getType().equals("32nd")) { return "t"; }
		else if(note.getType().equals("64th")) { return "x"; }
		else if(note.getType().equals("128th")) { return "o"; }
		else { return ""; }
	}
	
	public String getDots(Note note) {
		StringBuilder musicString = new StringBuilder();
		if(note.getDots() != null) {
			for(Dot dot : note.getDots()) {
				if(dot != null) {
					musicString.append(".");
				}
			}
		}
//		if(musicString.length() == 0) { musicString.append(""); } 
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
	
//	public void addTies(StringBuilder input, Note note) {
//		int indextoCheck = input.length() - 1;
//		if(note.getNotations() != null && note.getNotations().getTieds() != null) {
//			for(int i = 1; i <= 10; i++) {
//				if(input.charAt(input.length()-i) != '.' || input.charAt(input.length()-i) == getNoteDuration(note)) {
//					input.deleteCharAt(input.length()-i);
//					break;
//				}
//			}
//			
//			for(Tied tie : note.getNotations().getTieds()) {
//				if(tie != null && (tie.getType().equals("stop") || tie.getType().equals("continue"))) {
//					input.append("-");
//				}
//			}
//			input.append(getNoteDuration(note));
//		}
//		else if(note.getNotations() != null && note.getNotations().getTieds() != null) {
//			for(int i = 1; i <= 10; i++) {
//				if(input.charAt(input.length()-i) != '.' || input.charAt(input.length()-i) == getNoteDuration(note)) {
//					input.deleteCharAt(input.length()-i);
//					break;
//				}
//			}
//			for(Tied tie : note.getNotations().getTieds()) {
//				if(tie != null && (tie.getType().equals("start") || tie.getType().equals("continue"))) {
//					input.append("-");
//				}
//			}
//			input.append(getNoteDuration(note));
//		}
//	}

}
