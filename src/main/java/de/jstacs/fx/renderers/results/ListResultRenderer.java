package de.jstacs.fx.renderers.results;

import java.util.LinkedList;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import de.jstacs.results.ListResult;
import de.jstacs.results.Result;
import de.jstacs.results.ResultSet;

/**
 * Class for rendering a {@link ListResult} in the JavaFX GUI. The contents are rendered by a {@link TableView}
 * using the column names provided by {@link Result#getName()} of the {@link ListResult} entries.
 * An additional column with row numbers is added to the table as a first column.
 * 
 * @author Jan Grau
 *
 */
public class ListResultRenderer implements ResultRenderer<ListResult> {

	/**
	 * Registers this {@link ResultRenderer} for class {@link ListResult}.
	 */
	public static void register(){
		ResultRendererLibrary.register( ListResult.class, new ListResultRenderer() );
	}
	
	private ListResultRenderer() {
	}
	
	@Override
	public Node render( ListResult result, Pane parent ) {
		
		ResultSet[] ress = result.getValue();
		LinkedList<Object[]> list = new LinkedList<>();
		String[] colNames = null;
		for(int i=0;i<ress.length;i++){
			if(colNames == null){
				colNames = new String[ress[i].getNumberOfResults()];
				for(int j=0;j<ress[i].getNumberOfResults();j++){
					colNames[j] = ress[i].getResultAt( j ).getName();
				}
			}
			Object[] vals = new Object[ress[i].getNumberOfResults()];
			for(int j=0;j<ress[i].getNumberOfResults();j++){
				Object val = ress[i].getResultAt( j ).getValue();
				if(val instanceof Number){
					vals[j] = (Number)val;
				}else{
					vals[j] = val.toString();
				}
			}
			list.add( vals );
		}
		
		if(list.size() > 0){
			
			Object[] fico = list.getFirst();
			
			TableColumn<Object[],Number> countCol = new TableColumn<Object[],Number>("#");
			countCol.setSortable( false );

		//	TableView<Object[]> rowHeader = new TableView<>();
		//	rowHeader.getColumns().add( countCol );
		//	rowHeader.setItems( FXCollections.observableArrayList( list.toArray( new Object[0][0] ) ) );
		//	rowHeader.maxWidthProperty().bind( countCol.widthProperty() );
			
			
			TableColumn[] cols = new TableColumn[colNames.length];
			
			cols[0] = countCol;
			
			for(int i=0;i<colNames.length;i++){
				//cols[i+1] = new TableColumn<>(colNames[i]);
				final int col = i;
				
				
				if(fico[col] instanceof Number){
					TableColumn<Object[], Number> numCol = new TableColumn<Object[], Number>(colNames[i]);
					numCol.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Object[],Number>, ObservableValue<Number>>() {

						@Override
						public ObservableValue<Number> call( CellDataFeatures<Object[], Number> arg0 ) {
							return new ReadOnlyObjectWrapper<Number>((Number)arg0.getValue()[col]);
						}
						
					} );
					cols[i] = numCol;
				}else{
					TableColumn<Object[], String> strCol = new TableColumn<Object[], String>(colNames[i]);
					strCol.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Object[],String>, ObservableValue<String>>() {

						@Override
						public ObservableValue<String> call( CellDataFeatures<Object[], String> arg0 ) {
							return new ReadOnlyStringWrapper( arg0.getValue()[col].toString() );
						}
					} );
					cols[i] = strCol;
				}
			}
			
			TableView<Object[]> tv = new TableView<>();
			tv.getColumns().add( countCol );
			tv.getColumns().addAll( cols );
			tv.setItems( FXCollections.observableArrayList( list.toArray( new Object[0][0] ) ) );
			
			countCol.setCellValueFactory(column-> new ReadOnlyObjectWrapper<Number>((tv.getItems().indexOf(column.getValue())+1)));

			
			/*ScrollBar sb = new ScrollBar();
			sb.setOrientation( Orientation.VERTICAL );
			
			BorderPane bp = new BorderPane();
			bp.setLeft( rowHeader );
			bp.setCenter( tv );
			bp.setRight( sb );


				
			
			return bp;*/
			return tv;
		}
		return null;
	}

	/*public void postHook( Node prevRes ) {//TODO doesn't work
		
		if(prevRes == null){
			return;
		}
		
		BorderPane bp = (BorderPane)prevRes;
		TableView rowHeader = (TableView)bp.getLeft();
		TableView tv = (TableView)bp.getCenter();
		ScrollBar sb = (ScrollBar)bp.getRight();
		
		System.out.println(rowHeader.getSkin());
		
		VirtualFlow vf = (VirtualFlow)rowHeader.getChildrenUnmodifiable().get(1);
		ScrollBar scrollBar1 = null;
		for (final Node subNode: vf.getChildrenUnmodifiable()) {
			if (subNode instanceof ScrollBar && 
					((ScrollBar)subNode).getOrientation() == Orientation.VERTICAL) {
				scrollBar1 = (ScrollBar)subNode;
			}
		}		
		
		// Get the scrollbar of second table
		vf = (VirtualFlow)tv.getChildrenUnmodifiable().get(1);
		ScrollBar scrollBar2 = null;
		for (final Node subNode: vf.getChildrenUnmodifiable()) {
			if (subNode instanceof ScrollBar && 
					((ScrollBar)subNode).getOrientation() == Orientation.VERTICAL) {
				scrollBar2 = (ScrollBar)subNode;
			}
		}

		// Set min/max of visible scrollbar to min/max of a table scrollbar
		sb.setMin(scrollBar1.getMin());
		sb.setMax(scrollBar1.getMax());

		// bind the hidden scrollbar valueProterty the visible scrollbar
		sb.valueProperty().bindBidirectional(scrollBar1.valueProperty());
		sb.valueProperty().bindBidirectional(scrollBar2.valueProperty());
		
	}*/

}
