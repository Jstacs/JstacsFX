package de.jstacs.fx;

import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Class for displaying a splash screen while the main window of the {@link Application} is loading. 
 * This is especially useful if the workspace is automatically stored
 * to disk, as re-loading of the workspace may take substantial time for larger workspaces.
 * 
 * @author Jan Grau
 *
 */
public class SplashScreen extends Scene {

	/**
	 * Prepares the splash screen for display and returns the corresponding pane.
	 * @param title the text displayed in the splash screen
	 * @return the pane for displaying the splash screen
	 */
	public static Pane prepare(String title){
		BorderPane content = new BorderPane();
		System.out.println(title);
		Text message = new Text(title);
		
		message.setStyle( "-fx-font-size:16pt;	-fx-background-color:white;	-fx-padding: 5;	-fx-spacing:2;	-fx-alignment:center;" );
		
		content.setCenter( message );
		
		ProgressBar bar = new ProgressBar(-1);
		
		bar.prefWidthProperty().bind( content.widthProperty() );
		
		ProgressIndicator ind = new ProgressIndicator( -1 );
		
		content.setBottom( ind );
		content.setStyle( "-fx-padding:20;" );
		//content.setLeft( ind );
		
		return content;
	}
	
	/**
	 * Creates a new splash screen for the pane as provided by {@link #prepare(String)}.
	 * @param prepared the prepared pane
	 */
	public SplashScreen(Pane prepared) {
		super(prepared, 500, 200);
		
	}
	
	/**
	 * Shows the splash screen for the main window with given primary stage
	 * @param primaryStage the primary stage
	 */
	public void show(Stage primaryStage){
		
	}
	
	
	
}
