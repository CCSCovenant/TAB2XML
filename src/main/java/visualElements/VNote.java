package visualElements;

import javafx.scene.Group;
import visualElements.Notations.VINotation;

import java.util.HashMap;
import java.util.List;

public class VNote implements VElement,VConfigAble {
	int number;
	List<VNoteHead> noteHeads;
	List<VINotation> notations;

	public VNote(){

	}


	@Override
	public void setHighLight(boolean states) {

	}

	@Override
	public Group getShapeGroups() {
		return null;
	}

	@Override
	public void updateConfigList(HashMap<String, Double> configs) {

	}

	@Override
	public HashMap<String, Double> getConfigAbleList() {
		return null;
	}

	@Override
	public void updateConfig(String id, double value) {

	}
}
