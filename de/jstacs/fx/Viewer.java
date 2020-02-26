package de.jstacs.fx;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import de.jstacs.tools.JstacsTool;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public abstract class Viewer extends Stage {

	/**
	 * Creates a new {@link HelpViewer} for a specific {@link JstacsTool}.
	 * @param tool the tool
	 */
	public Viewer(JstacsTool tool){
		super();
		this.setTitle( getTitle(tool) );
		
		String content = tool.getHelpText();
		
		content = parse(tool);
		
		WebView view = new WebView();
		view.getEngine().loadContent( content );
		
		view.getEngine().getLoadWorker().stateProperty().addListener( new ChangeListener<State>(){

			@Override
			public void changed( ObservableValue<? extends State> arg0, State arg1, State arg2 ) {
				if(arg2 == State.SUCCEEDED){
					Document doc = view.getEngine().getDocument();
					NodeList list = doc.getElementsByTagName( "a" );
					for(int i=0;i<list.getLength();i++){
						EventTarget target = (EventTarget)list.item( i );
						target.addEventListener( "click", new EventListener() {
							
							@Override
							public void handleEvent( Event evt ) {
								EventTarget target = evt.getCurrentTarget();
		                        HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
		                        String href = anchorElement.getHref();
		                        
		                        Desktop d = Desktop.getDesktop();
		                        
		                        try {
									d.browse( new URI(href) );
								} catch ( IOException e ) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch ( URISyntaxException e ) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		                        
		                        evt.preventDefault();
							}
						}, false );
					}
				}
			}
			
		} );
		
		BorderPane pane = new BorderPane();
		pane.setCenter( view );
		
		Scene scene = new Scene( pane, 640, 400 );
		
		this.setScene( scene );
		
		KeyCombination kc = new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN);
		
		Runnable task = () -> { if(this.isShowing()){ this.getScene().getWindow().hide(); } };
		
		scene.getAccelerators().put(kc, task);
		
		
	}
	
	
	protected abstract String parse(JstacsTool tool);


	protected abstract String getTitle(JstacsTool tool);

	
}
