package visualElements.Notations;

import javafx.scene.paint.Color;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import visualElements.VConfig;
import visualElements.VElement;

public class VCurvedNotation extends VElement {
	boolean positive = false;
	String type;
	int startID = -2; // measure start
	int endID = -1; // measure end
	MoveTo moveTo = new MoveTo();
	QuadCurveTo quadCurveTop = new QuadCurveTo();
	QuadCurveTo quadCurveDown = new QuadCurveTo();
	Path path;

	public VCurvedNotation(String type){
		initConfig();
		this.type = type;
		path = new Path(moveTo,quadCurveTop,quadCurveDown);
		group.getChildren().add(path);
	}
	public void Alignment(double x1,double x2,double y){
		double adjustedY = y;
		double adjustedControlY = y;
		double adjustDownControlY = y;

		if (positive){
			adjustedControlY -= configMap.get("defaultControlPoint");
			adjustDownControlY -= configMap.get("defaultControlPoint")*2/3;
		}else {
			adjustedY += VConfig.getInstance().getGlobalConfig("Step")*2;
			adjustedControlY = adjustedY+configMap.get("defaultControlPoint");
			adjustDownControlY = adjustedY+configMap.get("defaultControlPoint")*2/3;

		}
		moveTo.setX(x1);
		moveTo.setY(adjustedY);
		setUpCurve(quadCurveTop,x2,((x1+x2)/2),adjustedY,adjustedControlY);
		setUpCurve(quadCurveDown,x1,(x1+x2)/2,adjustedY,adjustDownControlY);
		path.setFill(Color.BLACK);


	}

	@Override
	public void setHighLight(boolean states) {
		Color color;
		if (states) {
			color = VConfig.getInstance().getHighLightColor();
		} else {
			color = VConfig.getInstance().getDefaultColor();
		}
		path.setFill(color);
		path.setStroke(color);
	}

	public void setUpCurve(QuadCurveTo quadCurve, double x1, double x2, double y1, double y2){
		quadCurve.setX(x1);
		quadCurve.setY(y1);
		quadCurve.setControlX(x2);
		quadCurve.setControlY(y2);
	}
	public void setStartID(int i){
		startID = i;
	}
	public void setEndID(int i){
		endID = i;
	}

	public String getType() {
		return type;
	}

	public int getStartID() {
		return startID;
	}

	public int getEndID() {
		return endID;
	}

	public void initConfig(){
		configMap.put("defaultControlPoint", VConfig.getInstance().getGlobalConfig("Step")*2);
	}


	public void setPositive(boolean positive) {
		this.positive = positive;
	}
}
