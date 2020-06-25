package projects.dimont;
import de.jstacs.fx.Application;
import de.jstacs.tools.JstacsTool;
import javafx.stage.Stage;
import projects.quickscan.QuickBindingSitePredictionTool;
import projects.slim.SlimDimontTool;

public class DimontFX extends javafx.application.Application  {

	public static void main(String[] args) {
		
		
		launch(args);
    }
	
	@Override
	public void start( Stage primaryStage ) throws Exception {
		
		JstacsTool[] tools = new JstacsTool[] {
				new ExtractSequencesTool(),
				new DimontTool(),
				new SlimDimontTool(),
				new DimontPredictorTool(),
				new QuickBindingSitePredictionTool()
		};
		
        Application a = new Application(  this.getParameters(), "Dimont 1.0", tools );
        
        if(a.test()) {
        	a.startWithSplash( primaryStage, "Starting Dimont 1.0 ..." );
        }else {
        	System.exit(0);
        }
		

	}

}
