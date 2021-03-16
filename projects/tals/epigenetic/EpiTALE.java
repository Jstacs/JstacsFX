package projects.tals.epigenetic;

import de.jstacs.fx.Application;
import de.jstacs.tools.JstacsTool;
import javafx.stage.Stage;

public class EpiTALE extends javafx.application.Application {
	
	private static String version = "1.0";
	
	
	public static void main(String[] args) {
		
		launch(args);
		
    }
	

	@Override
	public void start( Stage primaryStage ) throws Exception {
		
		JstacsTool[] tools = new JstacsTool[]{
				new Bed2Bismark(),
				new BismarkConvertToPromotorSearch(),
				new BismarkMerge2Files(),
				new PileupConvertToPromotorSearch(),
				new NormalizePileupOutput(),
				new NarrowPeakConvertToPromotorSearch(),
				new QuickTBSPredictionToolEpigenetic()
		};
		
        Application a = new Application( this.getParameters(), "EpiTALE "+version, tools );
		
        a.startWithSplash( primaryStage, "Starting EpiTALE "+version+"..." );
	}
    
}
