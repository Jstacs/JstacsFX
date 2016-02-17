package de.jstacs.fx.renderers.results;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import de.jstacs.parameters.FileParameter.FileRepresentation;
import de.jstacs.results.TextResult;

/**
 * Renders a {@link TextResult} in the JavaFX GUI. This renderer uses a {@link ListView} for displaying the lines
 * of the {@link TextResult} for efficiency.
 * @author Jan Grau
 *
 */
public class TextResultRenderer implements ResultRenderer<TextResult> {

	/**
	 * Registers this {@link ResultRenderer} for class {@link TextResult}.
	 */
	public static void register(){
		ResultRendererLibrary.register( TextResult.class, new TextResultRenderer() );
	}
	
	private TextResultRenderer() {
		
	}
	
	/**
	 * Returns the header if one exists ((the first
     * row fails to convert to a number in all columns) the file in the {@link FileRepresentation},
     * or <code>null</code> if no such header exists.
	 * @param rep the file
	 * @return the header
	 */
	public static String[] getHeader(FileRepresentation rep) {
		String head = rep.getContent();
		int end = head.indexOf("\n");
		if(end < 0){
			end = head.length();
		}
		head = head.substring(0, end );

		String[] parts = head.split("\t");

		boolean parsable = false;
		for(int i=0;i<parts.length;i++){
			if(parts[i].trim().length() > 0){
				try{
					Double.parseDouble(parts[i]);

					parsable = true;
					break;
				}catch(NumberFormatException e){ }
			}
		}

		String[] opts = null;

		if(!parsable){
			opts = parts;
		}else{
			opts = new String[parts.length];
		}
		
		return opts;
	}

	@Override
	public Node render( TextResult result, Pane parent ) {
		
		if(TextResult.equals( result.getMime(), "xml") || result.getValue().getFilesize() > 20E6){
			return null;
		}else if(TextResult.equals(result.getMime(), "bed,gff,gff3,tsv,csv")){
			
			String sep = "\t";
			
			if(result.getValue().getExtension().equalsIgnoreCase("csv")){
				sep = ",";
			}
			
			String[] head = getHeader(result.getValue());
			boolean hasHeader = true;
			if(head[0] == null){
				for(int i=0;i<head.length;i++){
					head[i] = "Column "+(i+1);
				}
				hasHeader = false;
			}
			
			String cont = result.getValue().getContent();
			
			String[] lines = cont.split("\n");
			
			int i=0;
			if(hasHeader){
				i=1;
			}
			
			Object[][] content = new Object[lines.length-i][];
			
			Class[] types = new Class[head.length];
			
			for(int j=0;j<content.length;j++){
				String[] parts = lines[j+i].split(sep);
				content[j] = new Object[parts.length];
				for(int k=0;k<parts.length;k++){
					try{
						Integer.parseInt(parts[k]);
						if(types[k] == null || types[k].equals(Integer.class)){
							types[k] = Integer.class;
						}
					}catch(NumberFormatException e){
						try{
							Double.parseDouble(parts[k]);
							if(types[k] == null || types[k].equals(Integer.class) || types[k].equals(Double.class)){
								types[k] = Double.class;
							}
						}catch(NumberFormatException ex){
							types[k] = String.class;
						}
					}
					content[j][k] = parts[k];
				}
			}
			
			for(int j=0;j<content.length;j++){
				for(int k=0;k<content[j].length;k++){
					if(types[k].equals(Integer.class)){
						content[j][k] = Integer.parseInt((String)content[j][k]);
					}else if(types[k].equals(Double.class)){
						content[j][k] = Double.parseDouble((String)content[j][k]);
					}
				}
			}
			
			TableColumn[] cols = new TableColumn[head.length];
			
			for(int j=0;j<head.length;j++){
				final int col = j;
				if( Number.class.isAssignableFrom(types[j]) ){
					TableColumn<Object[], Number> numCol = new TableColumn<Object[], Number>(head[j]);
					numCol.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Object[],Number>, ObservableValue<Number>>() {

						@Override
						public ObservableValue<Number> call( CellDataFeatures<Object[], Number> arg0 ) {
							return new ReadOnlyObjectWrapper<Number>((Number)arg0.getValue()[col]);
						}
						
					} );
					cols[j] = numCol;
				}else{
					TableColumn<Object[], String> strCol = new TableColumn<Object[], String>(head[j]);
					strCol.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Object[],String>, ObservableValue<String>>() {

						@Override
						public ObservableValue<String> call( CellDataFeatures<Object[], String> arg0 ) {
							return new ReadOnlyStringWrapper( arg0.getValue()[col].toString() );
						}
					} );
					cols[j] = strCol;
				}
			}
			
			TableView<Object[]> tv = new TableView<>();
			tv.getColumns().addAll( cols );
			tv.setItems( FXCollections.observableArrayList( content ) );
		
			return tv;
			
			
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
