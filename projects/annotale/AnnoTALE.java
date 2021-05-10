package projects.annotale;
import de.jstacs.fx.Application;
import de.jstacs.tools.JstacsTool;
import javafx.stage.Stage;
import projects.tals.prediction.QuickTBSPredictionTool;
import projects.tals.rnaseq.DerTALE;
import projects.xanthogenomes.tools.ClassAssignmentTool;
import projects.xanthogenomes.tools.ClassBuilderTool;
import projects.xanthogenomes.tools.ClassPresenceTool;
import projects.xanthogenomes.tools.LoadAndViewClassesTool;
import projects.xanthogenomes.tools.PredictAndIntersectTargetsTool;
import projects.xanthogenomes.tools.RenameTool;
import projects.xanthogenomes.tools.TALEAnalysisTool;
import projects.xanthogenomes.tools.TALEComparisonTool;
import projects.xanthogenomes.tools.TALEPredictionTool;



public class AnnoTALE extends javafx.application.Application {

	private static String version = "1.5";
	
	
	public static void main(String[] args) {
		
		launch(args);
		
    }
	

	@Override
	public void start( Stage primaryStage ) throws Exception {
		
		JstacsTool[] tools = new JstacsTool[]{new TALEPredictionTool(),
		                                      new TALEAnalysisTool(), 
		                                      new ClassBuilderTool(), 
		                                      new LoadAndViewClassesTool(), 
		                                      new ClassAssignmentTool(), 
		                                      new RenameTool(), 
		                                      new PredictAndIntersectTargetsTool(),
		                                      new ClassPresenceTool(),
		                                      new TALEComparisonTool(),
		                                      new QuickTBSPredictionTool(),
		                                      new DerTALE()};
		
        Application a = new Application( this.getParameters(), "AnnoTALE "+version, tools );
		//primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("projects/xanthogenomes/tools/AnnoTALE_small.png")));
		
        a.startWithSplash( primaryStage, "Starting AnnoTALE "+version+"..." );
	}
    
    

}
