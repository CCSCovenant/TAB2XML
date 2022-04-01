package visualElements.Notations;

import javafx.scene.shape.Line;
import visualElements.VUtility;

import java.util.List;
import java.util.Stack;

public class VGuitarGNotation extends VGNotation{


	@Override
	public void alignment(List<Double> HPosition, List<Double> VPosition){
		double start = configMap.get("GuitarNotationStartHeight");
		double end = configMap.get("GuitarNotationEndHeight");
		double gap = configMap.get("notationGap");
		Stack<Line> lineStack = new Stack<>();
		int globalHLineNum = 0;
		int HlinePointer = 0;
		for (int i =0;i<notes.size();i++){
			int localHline = Math.max(0,(int)(Math.log(VUtility.NoteType2Integer(types.get(i)))/Math.log(2))-2);

			Vlines.get(i).setLayoutX(HPosition.get(i));
			if (types.get(i).equals("half")||types.get(i).equals("whole")){
				Vlines.get(i).setStartY(start+(end-start)/2);
			}else {
				Vlines.get(i).setStartY(start);
			}
			Vlines.get(i).setEndY(end);

			if (localHline>globalHLineNum){
				int diff = localHline - globalHLineNum;
				globalHLineNum = localHline;
				for (int j=0;j<diff;j++){
					lineStack.push(Hlines.get(HlinePointer));
					double startX = 0;
					if (i>0){
						if (i<notes.size()-1){
							if (VUtility.NoteType2Integer(types.get(i))<=VUtility.NoteType2Integer(types.get(i+1))){
								startX = HPosition.get(i)+configMap.get("thickness")/2;
							}else {
								startX = (HPosition.get(i) + HPosition.get(i - 1)) / 2;
							}
						}else {
							startX = (HPosition.get(i) + HPosition.get(i - 1)) / 2;
						}
					}else {
						startX = HPosition.get(i)+configMap.get("thickness")/2;
					}
					Hlines.get(HlinePointer).setStartX(startX);

					Hlines.get(HlinePointer).setLayoutY(end-(lineStack.size()-1)*gap);
					HlinePointer++;
				}
			}else if (localHline<globalHLineNum){
				int diff = globalHLineNum - localHline;
				globalHLineNum = localHline;
				for (int j=0;j<diff;j++){
					Line line = lineStack.pop();
					if (i==1) {
						line.setEndX((HPosition.get(i) + HPosition.get(i - 1)) / 2);
					}else {
						line.setEndX(HPosition.get(i-1)-configMap.get("thickness")/2);
					}
				}
			}
		}
		while (!lineStack.isEmpty()){
			Line line = lineStack.pop();
			line.setEndX(HPosition.get(HPosition.size()-1)-configMap.get("thickness")/2);
		}
	}
}