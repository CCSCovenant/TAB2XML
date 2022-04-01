package visualElements;

import javafx.scene.shape.Line;
import models.measure.Measure;
import models.measure.attributes.Time;
import models.measure.barline.BarLine;
import models.measure.note.Note;
import visualElements.Notations.VDrumGNotation;
import visualElements.Notations.VGNotation;
import visualElements.Notations.VGuitarGNotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VMeasure extends VElement implements VConfigAble {
	private int number;
	private List<VNote> Notes;
	private List<VGNotation> Notations = new ArrayList<>();
	private List<VSign> Signs = new ArrayList<>();
	private List<Line> staffLines = new ArrayList<>();
	private List<VBarline> barlines = new ArrayList<>();
	private HashMap<String,Double> config = VConfig.getInstance().getDefaultConfigMap("measure");


	String instrument = "";
	double gapCount = 0;
	boolean showClef = false;
	public VMeasure(Measure measure,String instrument,List<Integer> staffInfo){
		if (measure.getAttributes().getTime()!=null){
			Time time = measure.getAttributes().getTime();
			VTime vTime = new VTime(time.getBeats(),time.getBeatType());
			Signs.add(vTime);
			group.getChildren().add(vTime.getShapeGroups());
		}
		this.instrument = instrument;
		int i = 0;
		Notes = new ArrayList<>();
		initStaffLines(staffInfo);
		initBarlines(measure.getBarlines());
		if (measure.getNotesBeforeBackup()!=null){
			VNote vNote = null;
			for (Note note:measure.getNotesBeforeBackup()){
				if (note.getChord()==null){
					if (vNote!=null){
						Notes.add(vNote);
					}
					vNote = new VNote(i);
					i++;
					addNoteHead(note,vNote);
				}else {
					addNoteHead(note,vNote);
				}
			}
			if (vNote!=null){
				Notes.add(vNote);
			}
		}
		initNoteGroups();
		if (instrument.equals("TAB")){
			initGuitarNotations();
		}else {
			initDrumNotations();
		}
	}

	public void initDrumNotations(){
		double durationCounter = 0;
		VGNotation notation;
		notation = new VDrumGNotation();

		for (VNote note:Notes){
			String type = "quarter";
			if (note.type!=null){
				type = note.type;
			}
			if (VUtility.NoteType2Integer(type)<=2){ // do not create notation for whole and half notes.

			}else {
				notation.addNote(note.number, type);
			}
			durationCounter += (1d/VUtility.NoteType2Integer(type));

			if (durationCounter>=0.5){
				durationCounter = 0;
				Notations.add(notation);
				notation = new VDrumGNotation();
			}
		}

		if (notation.getSize()>0){
			Notations.add(notation);
		}
		for (VGNotation notationf:Notations){
			notationf.initElements();
			group.getChildren().add(notationf.getShapeGroups());
		}
	}

	public void initGuitarNotations(){
		double durationCounter = 0;
		VGNotation notation;
		notation = new VGuitarGNotation();

		for (VNote note:Notes){
			String type = "quarter";
			if (note.type!=null){
				type = note.type;
			}

			notation.addNote(note.number, type);

			durationCounter += (1d/VUtility.NoteType2Integer(type));

			if (durationCounter>=0.5){
				durationCounter = 0;
				Notations.add(notation);
				notation = new VGuitarGNotation();
			}
		}

		if (notation.getSize()>0){
			Notations.add(notation);
		}
		for (VGNotation notationf:Notations){
			notationf.initElements();
			group.getChildren().add(notationf.getShapeGroups());
		}
	}
	public void initNoteGroups(){
		for (VNote vNote:Notes){
			group.getChildren().add(vNote.getShapeGroups());
		}
	}
	public void addNoteHead(Note note,VNote vNote){
		int dots = 0;
		if (note.getDots()!=null){
			dots = note.getDots().size();
		}

		VNoteHead noteHead = null;
		String step;
		int octave;
		int relative = 3;
		//TODO set relative to default rest position. calculate based on the staffline.
		if (note.getRest()!=null){
			noteHead = new VNoteHead(VUtility.getDrumAssetName(note),0,relative);
		}else {
			if (instrument.equals("TAB")){
				if (note.getNotations()!=null&&note.getNotations().getTechnical()!=null){
					relative = note.getNotations().getTechnical().getString()*3; // since tab staff is double-space\
					noteHead = new VNoteHead(note.getNotations().getTechnical().getFret(),dots,relative);
				}
			}else {
				String result = VUtility.getDrumAssetName(note);
				step = note.getUnpitched().getDisplayStep();
				octave = note.getUnpitched().getDisplayOctave();
				relative = VUtility.getRelative(step,octave);
				noteHead = new VNoteHead(result,dots,relative);
			}
		}
		vNote.setNoteType(note.getType());
		vNote.addNoteHead(noteHead);
	}
	@Override
	public HashMap<String, Double> getConfigAbleList() {
		return config;
	}

	@Override
	public void updateConfig(String id, double value) {
		if (config.containsKey(id)){
			config.put(id,value);
		}
	}

	@Override
	public void setHighLight(boolean states) {

	}

	public void setNumber(int number) {
		this.number = number;
	}

	public double getGapCount(){
		return gapCount;
	}

	public void initStaffLines(List<Integer> staffInfo){
		//staffInfo contain offset of each staff
		for (Integer i:staffInfo){
			Line line = new Line(0,0,0,0);
			double gap = VConfig.getInstance().getGlobalConfig().get("Step");
			line.setLayoutY(i*gap);//
			staffLines.add(line);
			group.getChildren().add(line);
		}
	}
	public void updateStaffLine(double W){
		for (Line line:staffLines){
			line.setEndX(W);
		}
	}
	public void setShowClef(boolean states){
		this.showClef = states;
	}
	public void initBarlines(List<BarLine> barLine){
		barlines = new ArrayList<>();
		double length = staffLines.get(staffLines.size()-1).getLayoutY();
		barlines.add(new VBarline(length,"default",null,"right"));
		if (barLine!=null){
			for (BarLine barLine1:barLine){
				barlines.add(new VBarline(length,barLine1.barStyle,barLine1.repeat,barLine1.location));
			}
		}

		for (VBarline vBarline:barlines){
			group.getChildren().add(vBarline.getShapeGroups());
		}
	}
	public void alignmentBarlines(){
		for (VBarline vBarline:barlines){
			if (vBarline.location.equals("right")){
				vBarline.getShapeGroups().setLayoutX(W);
			}else {
				vBarline.getShapeGroups().setLayoutX(0);
			}
		}
	}
	public void alignmentNotations(){
		for (VGNotation notation:Notations){
			List<Double> HPosition = new ArrayList<>();
			for (Integer i:notation.getNotes()){
				double offset = VConfig.getInstance().getGlobalConfig().get("Step")*2;
				HPosition.add(Notes.get(i).getShapeGroups().getLayoutX()+offset);
			}
			//H position for each note
			List<Double> VPosition = new ArrayList<>();
			for (Integer i:notation.getNotes()){
				VPosition.add(Notes.get(i).maxVPos);
			}
			notation.alignment(HPosition,VPosition);
			//V position for the most bottom note
		}
	}
	public void addGapBetweenElements(double width){
		W += width;
		gapCount++;
	}
	public void alignment(){
		W = 0;
		W += config.get("gapBeforeMeasure");
		gapCount = 0;
		double gapBetweenElement = config.get("gapBetweenElement");
		for (VSign sign:Signs){
			sign.alignment();
			sign.getShapeGroups().setLayoutX(W);
			W += sign.getW();
			addGapBetweenElements(gapBetweenElement);
		}
		for (VNote note:Notes){
			note.alignment();
			note.getShapeGroups().setLayoutX(W);
			W += note.getW();
			addGapBetweenElements(gapBetweenElement);
		}
		addGapBetweenElements(gapBetweenElement);
		//update the staffline.
		updateStaffLine(W);
		alignmentBarlines();
		alignmentNotations();
	}
}