package de.jstacs.fx.renderers.results;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import de.jstacs.data.DataSet;
import de.jstacs.data.sequences.annotation.SplitSequenceAnnotationParser;
import de.jstacs.results.DataSetResult;


public class DataSetResultRenderer implements ResultRenderer<DataSetResult> {

	public static void register(){
		ResultRendererLibrary.register( DataSetResult.class, new DataSetResultRenderer() );
	}
	
	private DataSetResultRenderer(){
		
	}
	
	@Override
	public Node render( DataSetResult result, Pane parent ) {

		try{
			DataSet data = result.getValue();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if(result.getParser() == null){
				data.save( baos,'>', new SplitSequenceAnnotationParser( ":", ";" ) );
			}else{
				data.save( baos,'>', result.getParser() );
			}
			String content = baos.toString();

			ListView<String> lv = new ListView<>();
			ObservableList<String> items =FXCollections.observableArrayList ( content.split( "\n" ) );
			lv.setItems( items );
			
			return lv;
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
	}

}
