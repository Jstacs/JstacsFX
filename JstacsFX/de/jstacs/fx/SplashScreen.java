package de.jstacs.fx;

import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class SplashScreen extends Scene {

	
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
	
	public SplashScreen(Pane prepared) {
		super(prepared, 500, 200);
		
	}
	
	public void show(Stage primaryStage){
		
	}
	
	
	
}
