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

/**
 * Class for displaying overlay messages (Tool started, tool failed, tool finished) stacked on top of the 
 * main window of the JavaFX {@link Application}. These overlays do not capture mouse events and may, hence, be used
 * to display messages without interrupting user interaction.
 * 
 * @author Jan Grau
 *
 */
public class Messages {

	/**
	 * The severity level of a message
	 * @author Jan Grau
	 *
	 */
	public enum Level{
		/**
		 * Success, message displayed with green background
		 */
		SUCCESS,
		/**
		 * Neutral, message displayed with grey background
		 */
		INFO,
		/**
		 * Warning or error, message displayed with red background
		 */
		WARNING
	}
	
	private Label message;
	private HBox box;
	private ObservableList<Pair<String,Level>> messages;
	
	/**
	 * Creates a new renderer for overlay messages
	 */
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
	
	/**
	 * Returns the component pane for rendering the messages.
	 * 
	 * @return the pane
	 */
	public Pane getMessagePane(){
		return box;
	}
	
	/**
	 * Adds a message to the queue of messages. Messages will be displayed in the order
	 * of submission. Each message is displayed at least 1.5 seconds.
	 * @param text the text of the message
	 * @param level the severity level of the message
	 */
	public void displayMessage(String text, Level level){
		
		messages.add( new Pair<String, Messages.Level>( text, level ) );
		
		
	}
	
	
	
	
}
