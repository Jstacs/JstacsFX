package de.jstacs.fx.renderers.results;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import de.jstacs.results.TextResult;


public class TextResultRenderer implements ResultRenderer<TextResult> {

	public static void register(){
		ResultRendererLibrary.register( TextResult.class, new TextResultRenderer() );
	}
	
	private TextResultRenderer() {
		
	}

	@Override
	public Node render( TextResult result, Pane parent ) {
		
		if(TextResult.equals( result.getMime(), "xml")){
			return null;
		}else{

			ListView<String> lv = new ListView<>();
			ObservableList<String> items =FXCollections.observableArrayList ( result.getValue().getContent().split( "\n" ) );
			lv.setItems( items );

			/*TextArea ta = new TextArea();
		ta.setEditable( false );
		ta.setText( result.getValue().getContent() );
		ta.setCache( true );*/

			/*Text text = new Text(result.getValue().getContent());
		TextFlow flow = new TextFlow(text);

		ScrollPane pane = new ScrollPane( flow );

		return pane;*/

			return lv;
		}
	}

}
