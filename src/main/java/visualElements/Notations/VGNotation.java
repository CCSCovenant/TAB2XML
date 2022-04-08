package visualElements.Notations;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import visualElements.VConfig;
import visualElements.VElement;
import visualElements.VUtility;

import java.util.ArrayList;
import java.util.List;

public class VGNotation extends VElement {
	List<Integer> notes = new ArrayList<>(); // how much note in current notation
	List<String> types = new ArrayList<>(); // the type of note in current notation
	List<Integer> dots = new ArrayList<>();
	List<Line> Vlines = new ArrayList<>();
	List<Line> Hlines = new ArrayList<>();
	List<Circle> circles = new ArrayList<>();
	public VGNotation(){
		initConfig();
	}
	public void initConfig(){
		configMap.put("GuitarNotationStartHeight",100d);
		configMap.put("GuitarNotationEndHeight",120d);

		configMap.put("DrumNotationHeight",-30d);

		configMap.put("notationGap",10d);
		configMap.put("thickness",5d);

		configMap.put("dotSize",2d);
		configMap.put("dotOffset",6d);

	}
	public int getSize(){
		return notes.size();
	}
	public void addNote(int noteID, String type, int dotC){
		notes.add(noteID);
		types.add(type);
		dots.add(dotC);
	}
	public List<Integer> getNotes(){
		return notes;
	}
	@Override
	public void setHighLight(boolean states) {
		Color color;
		if (states){
			color	= VConfig.getInstance().getHighLightColor();
		}else {
			color = VConfig.getInstance().getDefaultColor();
		}
		for (Line line:Vlines){
			line.setStroke(color);
		}
		for (Line line:Hlines){
			line.setStroke(color);
		}
	}

	@Override
	public Group getShapeGroups() {
		return group;
	}

	@Override
	public double getH() {
		return 0;
	}

	@Override
	public double getW() {
		return 0;
	}



	public void initElements(){
		int globalHLineNum = 0;
		for (int i =0;i<notes.size();i++){
			if (dots.get(i)>0){
				for (int k=0;k<dots.get(i);k++){
					circles.add(new Circle());
				}
			}
			Vlines.add(new Line());
			int localHline = (int)(Math.log(VUtility.NoteType2Integer(types.get(i)))/Math.log(2))-2;
			if (localHline>globalHLineNum){
				int diff =  localHline - globalHLineNum;
				globalHLineNum = localHline;
				if (notes.size()==1){
					//TODO add flag for only one note.
				}else {
					for (int j=0;j<diff;j++){
						Line line = new Line();
						line.setStrokeWidth(configMap.get("thickness"));
						Hlines.add(line);
					}
				}
			}else if (localHline<globalHLineNum){
				globalHLineNum = localHline;
			}
		}
		for (Circle circle:circles){
			group.getChildren().add(circle);
		}
		for (Line line:Vlines){
			group.getChildren().add(line);
		}
		for (Line line:Hlines){
			line.setStrokeLineCap(StrokeLineCap.BUTT);
			group.getChildren().add(line);
		}
	}

	public void alignment(List<Double> HPosition,List<Double> VPosition){

	}
}
