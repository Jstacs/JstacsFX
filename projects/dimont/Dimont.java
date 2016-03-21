package projects.dimont;
import de.jstacs.classifiers.differentiableSequenceScoreBased.AbstractMultiThreadedOptimizableFunction;
import de.jstacs.fx.Application;
import javafx.stage.Stage;
import projects.dimont.DimontPredictorTool;
import projects.dimont.DimontTool;
import projects.dimont.ExtractSequencesTool;

public class Dimont extends javafx.application.Application  {

	
	public static void main(String[] args) {
		
		launch(args);
		
    }
	
	@Override
	public void start( Stage primaryStage ) throws Exception {
		
		DimontTool tool = new DimontTool();
		
		DimontPredictorTool pred = new DimontPredictorTool();
		
		ExtractSequencesTool est = new ExtractSequencesTool();

        Application a = new Application( "Dimont "+tool.getToolVersion(), est, tool, pred );
		
        a.startWithSplash( primaryStage, "Starting Dimont "+tool.getToolVersion()+"..." );
		

	}

}
