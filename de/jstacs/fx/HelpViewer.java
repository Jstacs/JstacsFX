package de.jstacs.fx;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import de.jstacs.tools.JstacsTool;


/**
 * Class for displaying the help text of a {@link JstacsTool} ({@link JstacsTool#getHelpText()}) in the JavaFX GUI.
 * The help text is displayed in a separate window and the help text is converted from re-structured text to HTML.
 *  
 * @author Jan Grau
 *
 */
public class HelpViewer extends Stage {

	/**
	 * Creates a new {@link HelpViewer} for a specific {@link JstacsTool}.
	 * @param tool the tool
	 */
	public HelpViewer(JstacsTool tool){
		super();
		this.setTitle( "Help for "+tool.getToolName() );
		
		String content = tool.getHelpText();
		
		content = parse(content);
		
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
		
		Scene scene = new Scene( pane, 500, 400 );
		
		this.setScene( scene );
		
		//this.setAlwaysOnTop( true );
		
		this.show();
		
	}
	
	
	private static String parse(String restruct){
		String[] lines = restruct.split( "\n" );
		
		Pattern bold = Pattern.compile( "\\*\\*(.+?)\\*\\*" );
		Pattern italics = Pattern.compile( "\\*(.+?)\\*" );
		Pattern tt = Pattern.compile( "\\`\\`(.+?)\\`\\`" );
		Pattern amp = Pattern.compile( "\"" );
		
		Pattern link = Pattern.compile( "^\\.\\.\\s+\\_(.*?)\\s*\\:\\s*(.*)$" );
		
		HashMap<Pattern, String> linkTargets = new HashMap<>();
		
		for(int i=0;i<lines.length;i++){
			
			Matcher m = bold.matcher( lines[i] );
			lines[i] = m.replaceAll( "<b>$1</b>" );
			
			m = italics.matcher( lines[i] );
			lines[i] = m.replaceAll( "<i>$1</i>" );
			
			m = tt.matcher( lines[i] );
			lines[i] = m.replaceAll( "<kbd>$1</kbd>" );
			
			m = amp.matcher( lines[i] );
			lines[i] = m.replaceAll( "&quot;" );
			
			
			
			m = link.matcher( lines[i] );
			
			if(m.matches()){
				String key = m.group( 1 );
				String target = m.group( 2 );
				linkTargets.put( Pattern.compile( "\\`?("+key+")\\`?\\_" ), target );
				lines[i] = "";
			}else{
				lines[i] = lines[i]+"<br>";
			}
			
		}
		
		
		
		for(int i=0;i<lines.length;i++){
			Set<Pattern> pats = linkTargets.keySet();
			Iterator<Pattern> it = pats.iterator();
			while( it.hasNext() ){
				Pattern pat = it.next();
				Matcher m = pat.matcher( lines[i] );
				lines[i] = m.replaceAll( "<a href=\""+linkTargets.get( pat )+"\">$1</a>" );
			}
		}
		
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<lines.length;i++){
			sb.append( lines[i] );
			sb.append( "\n" );
		}
		return sb.toString();
	}
	
	
	
}
