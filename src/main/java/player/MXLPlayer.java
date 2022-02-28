package player;

import converter.Score;
import custom_exceptions.TXMLException;
import models.Part;
import models.ScorePartwise;
import models.measure.Measure;
import models.measure.attributes.Attributes;
import models.measure.note.Dot;
import models.measure.note.Note;
import models.part_list.PartList;
import models.part_list.ScorePart;
import org.jfugue.player.Player;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class MXLPlayer{
	private ScorePartwise score;
	private Player player = new Player();
	private HashMap<String,ScorePart> scorePartMap = new HashMap<>();
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
		System.out.println(musicString.toString());
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
			double noteDuration = ((note.getDuration()/measure.getAttributes().getDivisions()) * 0.25);
			
			if(durationCount < duration) {
				durationCount += noteDuration;
			}
			else {
				if(note.getChord() == null && musicString.length() > 0 && musicString.charAt(musicString.length()-1) == '+') {
					musicString.deleteCharAt(musicString.length()-1);
					musicString.append(" ");
				}
				if(measure.getAttributes().getClef() != null) {
					if(note.getChord() != null && musicString.length() > 0 && musicString.charAt(musicString.length()-1) == '+') {
						
					}else {
						musicString.append(getNoteDetails(note));
					}
					String clef = measure.getAttributes().getClef().getSign();

					if(clef.equals("percussion")) {
						musicString.append(note.getUnpitched().getDisplayStep());
						musicString.append(note.getUnpitched().getDisplayOctave());
					}
					else if(clef.equals("TAB")){
						if(note.getRest() != null) {
							musicString.append("R");
						}
						else {
							//musicString.append(getNote(measure.getAttributes()));
							musicString.append(note.getPitch().getStep());
							musicString.append(note.getPitch().getOctave());
						}
					}

					if(note.getGrace() != null) {
						musicString.append("i");
					}
					else {
						DecimalFormat df = new DecimalFormat("#.###");
						noteDuration = Double.valueOf(df.format(noteDuration));
						musicString.append("/" + noteDuration);
					}
					musicString.append(" ");

					if(note.getChord() != null) { 
						musicString.deleteCharAt(musicString.length()-1);
						musicString.append("+");
					}
				}
			}
		}
		return musicString.toString();
	}
	public String getNote(Attributes attributes){
		StringBuilder musicString = new StringBuilder();
		
		String key = "K";
		for(int i = 0; i < Math.abs(attributes.getKey().getFifths()); i++) {
			if(attributes.getKey().getFifths() > 0) {
				key += "#";
			}
			else if (attributes.getKey().getFifths() < 0) {
				key += "b";
			}else {
				key = "";
			}
		}
		musicString.append(key);
		
		return musicString.toString();
	}
	
	public String getNoteDetails(Note note) {
		StringBuilder musicString = new StringBuilder();
		String voice = "V" + note.getVoice();
		String instrument; 
		if(note.getInstrument() == null || note.getInstrument().getId().equals("")) {
			instrument = "I0";
		}
		else { instrument = "I[" + note.getInstrument().getId() + "]";
		}
		
		musicString.append(voice + " " + instrument + " ");
		
		return musicString.toString();
	}
}
