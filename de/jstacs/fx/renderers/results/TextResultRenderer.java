package de.jstacs.fx.renderers.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jstacs.parameters.FileParameter.FileRepresentation;
import de.jstacs.results.TextResult;
import de.jstacs.utils.Pair;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

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
	
	private LinkedList<Pair<TextResult,Node>> nodeCache;
	
	private TextResultRenderer() {
		nodeCache = new LinkedList<Pair<TextResult,Node>>();
	}
	
	private Node getFromCache(TextResult tr) {
		Iterator<Pair<TextResult,Node>> it = nodeCache.iterator();
		while(it.hasNext()) {
			Pair<TextResult,Node> el = it.next();
			if(el.getFirstElement() == tr) {
				return el.getSecondElement();
			}
		}
		return null;
	}
	
	private void addToCache(TextResult tr, Node node) {
		if(nodeCache.size()>5) {
			nodeCache.removeFirst();
		}
		nodeCache.addLast(new Pair<TextResult, Node>(tr, node));
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
		
		if(TextResult.equals( result.getMime(), "xml") || "gz".equalsIgnoreCase(result.getValue().getExtension()) || result.getValue().getFilesize() > 20E6){
			return null;
		}else {
			Node cached = getFromCache(result);
			if(cached != null) {
				return cached;
			}else {

				if(TextResult.equals(result.getMime(), "bed,gff,gff3,tsv,csv,narrowPeak")){

					String sep = "\t";

					if("csv".equalsIgnoreCase(result.getValue().getExtension())){
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

					if(lines.length > 0){

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
							if( types[j] != null && Number.class.isAssignableFrom(types[j]) ){
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


						tv.getSelectionModel().selectionModeProperty().set(SelectionMode.MULTIPLE);

						tv.setOnKeyPressed(new EventHandler<KeyEvent>() {

							@Override
							public void handle(KeyEvent event) {
								KeyCodeCombination copyKeyCodeCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_ANY);
								if(copyKeyCodeCombination.match(event)){
									ObservableList<Integer> sel = tv.getSelectionModel().getSelectedIndices();
									Set<Integer> set = new HashSet<>(sel);
									List<Integer> sorted = new ArrayList<>(set); 
									Collections.sort(sorted);
									StringBuffer sb = new StringBuffer();
									for(int i=0;i<sorted.size();i++){
										Object[] items = tv.getItems().get(sorted.get(i));
										for(int j=0;j<items.length;j++){
											sb.append(items[j]);
											if(j<items.length-1){
												sb.append("\t");
											}
										}
										sb.append("\n");
									}


									final ClipboardContent clipboardContent = new ClipboardContent();
									clipboardContent.putString(sb.toString());

									Clipboard.getSystemClipboard().setContent(clipboardContent);

								}

							}

						});

						addToCache(result, tv);

						return tv;
					}
				}


				ListView<String> lv = new ListView<>();
				ObservableList<String> items =FXCollections.observableArrayList ( result.getValue().getContent().split( "\n" ) );
				lv.setItems( items );
				lv.getSelectionModel().selectionModeProperty().set(SelectionMode.MULTIPLE);

				lv.setOnKeyPressed(new EventHandler<KeyEvent>() {

					@Override
					public void handle(KeyEvent event) {
						KeyCodeCombination copyKeyCodeCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_ANY);
						if(copyKeyCodeCombination.match(event)){
							ObservableList<Integer> sel = lv.getSelectionModel().getSelectedIndices();
							Set<Integer> set = new HashSet<>(sel);
							List<Integer> sorted = new ArrayList<>(set); 
							Collections.sort(sorted);
							StringBuffer sb = new StringBuffer();
							for(int i=0;i<sorted.size();i++){
								sb.append(lv.getItems().get(sorted.get(i)));
								sb.append("\n");
							}


							final ClipboardContent clipboardContent = new ClipboardContent();
							clipboardContent.putString(sb.toString());

							Clipboard.getSystemClipboard().setContent(clipboardContent);

						}

					}

				});

				addToCache(result,lv);
				return lv;

			}
		}
	}

}
