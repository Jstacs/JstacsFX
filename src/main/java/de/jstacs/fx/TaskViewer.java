package de.jstacs.fx;

import java.util.Date;
import java.util.HashMap;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import de.jstacs.results.ResultSetResult;
import de.jstacs.tools.JstacsTool;
import de.jstacs.utils.Pair;

/**
 * Class for rendering a list of currently running or scheduled tasks in an {@link Application}.
 * Tasks that are scheduled may be removed from the list by pressing a "cancel" button. Tasks currently running
 * may also be canceled but (depending on the {@link JstacsTool} implementation) may nonetheless run all computations
 * until the end of the corresponding {@link JstacsTool#run(de.jstacs.parameters.ParameterSet, de.jstacs.tools.Protocol, de.jstacs.tools.ProgressUpdater, int)}
 * method.
 * 
 * Tasks are shown as rows of a {@link TableView} with the corresponding {@link JstacsTool} name, the date (and time) of starting the task, and
 * a cancel button.
 * 
 * The task viewer is shown in a separate window.
 * 
 * @author Jan Grau
 *
 */
public class TaskViewer extends Stage {

	//private ObservableList<Task<ResultSetResult>> enqueued;
	//private HashMap<Task<ResultSetResult>,Pair<String, Date>> nameMap;
	private TableView<Task<ResultSetResult>> table;
	
	/**
	 * Creates a new tasks viewer for the list of enqueued tasks and the map from task objects
	 * to corresponding tool names and start dates.
	 * @param enqueued the list of enqueued {@link Task}s
	 * @param nameMap the map from tasks to names and start dates
	 */
	public TaskViewer(ObservableList<Task<ResultSetResult>> enqueued, HashMap<Task<ResultSetResult>,Pair<String, Date>> nameMap) {
		//this.enqueued = enqueued;
		//this.nameMap = nameMap;
				
		TableColumn<Task<ResultSetResult>, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Task<ResultSetResult>,String>, ObservableValue<String>>() {
			
			@Override
			public ObservableValue<String> call( CellDataFeatures<Task<ResultSetResult>, String> arg0 ) {
				return new ReadOnlyStringWrapper(nameMap.get( arg0.getValue() ).getFirstElement());
			}
		} );
		
		TableColumn<Task<ResultSetResult>, Date> dateCol = new TableColumn("Date");
		dateCol.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Task<ResultSetResult>,Date>, ObservableValue<Date>>() {
			
			@Override
			public ObservableValue<Date> call( CellDataFeatures<Task<ResultSetResult>, Date> arg0 ) {
				return new ReadOnlyObjectWrapper<Date>( nameMap.get( arg0.getValue() ).getSecondElement() );
			}
		} );
		
		
		TableColumn<Task<ResultSetResult>, State> stateCol = new TableColumn<>("State");
		stateCol.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Task<ResultSetResult>,State>, ObservableValue<State>>() {
			
			@Override
			public ObservableValue<State> call( CellDataFeatures<Task<ResultSetResult>, State> arg0 ) {
				//return new ReadOnlyStringWrapper(arg0.getValue().getState().toString());
				return arg0.getValue().stateProperty();
			}
		} );
		
		
		
		TableColumn<Task<ResultSetResult>, Button> removeColumn = new TableColumn<>("Cancel");
		removeColumn.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<Task<ResultSetResult>, Button>, ObservableValue<Button>>() {

			@Override
			public ObservableValue<Button> call( CellDataFeatures<Task<ResultSetResult>, Button> arg0 ) {
				Button btn = new Button( "Cancel" );
				btn.setOnAction( new EventHandler<ActionEvent>() {

					@Override
					public void handle( ActionEvent arg1 ) {
						//System.out.println("cancelling "+arg0.getValue().getValue());

						if(arg0.getValue().isRunning()){
							arg0.getValue().cancel();	
						}
						enqueued.remove( arg0.getValue() );
						nameMap.remove( arg0.getValue() );

					}
				} );

				btn.disableProperty().bind(arg0.getValue().runningProperty());
				btn.visibleProperty().bind(arg0.getValue().runningProperty().not());
				
				return new ReadOnlyObjectWrapper<Button>( btn );

			}
			
			
			
		} );
		
		BorderPane border = new BorderPane();
		
		table = new TableView<>();
		table.getColumns().addAll( nameCol, dateCol, stateCol, removeColumn );
		//table.setColumnResizePolicy( TableView.UNCONSTRAINED_RESIZE_POLICY );
		table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
		table.setItems( enqueued );
		
		border.setCenter( table );
		
		Scene scene = new Scene( border, 700, 250 );
		
		this.setScene( scene );
		this.hide();
		
		final Stage st = this;
		
		this.setOnCloseRequest( new EventHandler<WindowEvent>() {

			@Override
			public void handle( WindowEvent arg0 ) {
				st.hide();
			}
			
		} );
		
		this.setAlwaysOnTop( true );
		
	}
	
	
	/*private double getTableWidth(){
		ObservableList<TableColumn<Task<ResultSetResult>, ?>> cols = table.getColumns();
		
		table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
		table.setColumnResizePolicy( TableView.UNCONSTRAINED_RESIZE_POLICY );
		double w = 0;
		for(int i=0;i<cols.size();i++){
			double temp = cols.get( i ).getPrefWidth();
			if(temp > 0){
				w += temp;
			}
		}
		return w;
	}*/
	
	/*public void adjustAndShow(){
		this.setWidth( Math.max(getTableWidth(), 100) );
		System.out.println(getTableWidth());
		this.setHeight( 250 );
		this.show();
	}*/
	

}
