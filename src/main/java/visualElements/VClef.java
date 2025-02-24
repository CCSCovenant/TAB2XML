package visualElements;

import javafx.geometry.Bounds;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import visualizer.ImageResourceHandler;

import java.util.ArrayList;
import java.util.List;

public class VClef extends VSign {
	ImageView imageView = new ImageView();
	ImageResourceHandler imageResourceHandler = ImageResourceHandler.getInstance();
	private List<Line> staffLines = new ArrayList<>();

	public VClef(String instrument){
		group.getChildren().add(imageView);
		initStaffLines(VConfig.getInstance().getStaffDetail());
		imageView.setImage(imageResourceHandler.getImage(instrument));
	}
	public void initStaffLines(List<Integer> staffInfo){
		//staffInfo contain offset of each staff
		for (Integer i:staffInfo){
			Line line = new Line(0,0,0,0);
			double gap = VConfig.getInstance().getGlobalConfig("Step");
			line.setLayoutY(i*gap);//
			staffLines.add(line);
			group.getChildren().add(line);
		}

	}
	public void setHighLight(HighLight states) {
		Color color = null;
		highLight = states;
		switch (states){
			case NULL -> color = VConfig.getInstance().getDefaultColor();
			case PLAY -> color = VConfig.getInstance().getPlayColor();
			case SELECTED -> color = VConfig.getInstance().getHighLightColor();
		}
		Blend blend = new Blend();
		Bounds bounds = group.getBoundsInLocal();
		ColorInput colorinput = new ColorInput(bounds.getMinX(),bounds.getMinY(),bounds.getWidth(),bounds.getHeight(),color);
		blend.setTopInput(colorinput);
		blend.setMode(BlendMode.SRC_ATOP);
		group.setEffect(blend);
	}
	public void alignment(){
		List<Integer> staffDetail = VConfig.getInstance().getStaffDetail();
		double step = VConfig.getInstance().getGlobalConfig("Step");
		double start = staffDetail.get(0)*step;
		double end = staffDetail.get(staffDetail.size()-1)*step;
		double min = VConfig.getInstance().getGlobalConfig("MinNoteDistance");

		imageView.setFitHeight(end-start);
		imageView.setFitWidth((end-start)/2);
		imageView.setLayoutX(min);
		W = imageView.getFitWidth()+min*2;
		updateStaffLine(W);
		setHighLight(highLight);
	}
	public void updateStaffLine(double W){
		for (Line line:staffLines){
			line.setEndX(W);
		}
	}
	public void setVisible(boolean states){
		imageView.setVisible(states);
	}
}
