package de.jstacs.fx;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import de.jstacs.utils.Pair;


public class Messages {

	public enum Level{
		SUCCESS,
		INFO,
		WARNING
	}
	
	private Label message;
	private HBox box;
	private ObservableList<Pair<String,Level>> messages;
	
	public Messages(){
		box = new HBox();
		box.setAlignment( Pos.CENTER );
		message = new Label();
		message.getStyleClass().add( "overlay" );
		box.getChildren().add( message );
		box.setOpacity( 0.0 );
		box.setMouseTransparent( true );
		messages = FXCollections.observableArrayList();
		
		messages.addListener( new ListChangeListener<Pair<String,Level>>(){

			@Override
			public void onChanged( ListChangeListener.Change<? extends Pair<String, Level>> arg0 ) {
				
				//System.out.println("changes");
				while( arg0.next() ){
					if(messages.size() == 1 || (arg0.getRemovedSize()>0 && messages.size() > 0 ) ){
						//System.out.println("elements");
						Pair<String,Level> first = messages.get( 0 );
						String text = first.getFirstElement();
						Level level = first.getSecondElement();
						//System.out.println("text: "+text);
						message.setText( text );
						if(level == Level.WARNING){
							message.setId( "errover" );
						}else if(level == Level.SUCCESS){
							message.setId( "succover" );
						}else{
							message.setId( "" );
						}
						box.setOpacity( 0.5 );

						FadeTransition fade = new FadeTransition( Duration.millis( 1500 ), box );
						fade.setDelay( Duration.millis( 1500 ) );
						fade.setAutoReverse( false );
						fade.setFromValue( 0.5 );
						fade.setToValue( 0.0 );
						fade.play();
						fade.setOnFinished( new EventHandler<ActionEvent>() {

							@Override
							public void handle( ActionEvent arg0 ) {
								box.setOpacity( 0.0 );
								//message.setText( "" );
								messages.remove( first );
								//System.out.println("removed "+text);
							}

						} );
					}

				}
			}
		} );
		
	}
	
	public Pane getMessagePane(){
		return box;
	}
	
	
	public void displayMessage(String text, Level level){
		
		messages.add( new Pair<String, Messages.Level>( text, level ) );
		
		
	}
	
	
	
	
}
