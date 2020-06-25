package projects.methyl;

import de.jstacs.fx.Application;
import de.jstacs.tools.JstacsTool;
import javafx.stage.Stage;
import projects.quickscan.QuickBindingSitePredictionTool;

public class MotifMethylationGUI extends javafx.application.Application {

private static String version = "1.0";
	
	
	public static void main(String[] args) {
		
		launch(args);
		
    }
	

	@Override
	public void start( Stage primaryStage ) throws Exception {
		
		JstacsTool[] tools = new JstacsTool[]{
				new ExtractMethylatedSequencesTool(), 
				new MethylSlimDimontTool(),
				new MotifScanningTool(), 
				new EvaluateScoringTool(), 
				new QuickBindingSitePredictionTool(), 
				new MethylationSensitivity()};
		
        Application a = new Application( this.getParameters(), "MeDeMo "+version, tools );
		
        a.startWithSplash( primaryStage, "Starting MeDeMo "+version+"..." );
	}
	
}
