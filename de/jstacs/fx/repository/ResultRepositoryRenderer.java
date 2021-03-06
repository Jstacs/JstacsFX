package de.jstacs.fx.repository;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import de.jstacs.fx.Application;
import de.jstacs.fx.LoadSaveDialogs;
import de.jstacs.fx.renderers.results.ResultRenderer;
import de.jstacs.fx.renderers.results.ResultRendererLibrary;
import de.jstacs.fx.repository.ResultRepository.ResultConsumer;
import de.jstacs.results.CategoricalResult;
import de.jstacs.results.Result;
import de.jstacs.results.ResultSet;
import de.jstacs.results.ResultSetResult;
import de.jstacs.results.SimpleResult;
import de.jstacs.results.savers.ResultSaver;
import de.jstacs.results.savers.ResultSaverLibrary;
import de.jstacs.tools.ToolResult;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 * The class renders the contents of the current {@link ResultRepository} in the JavaFX GUI.
 * The results are rendered in a {@link TreeTableView} with top-level entries corresponding to top-level
 * {@link Result}s in the {@link ResultRepository}. If these top-level entries are aggregate types like {@link ResultSetResult}s,
 * they may be expanded to also list their content {@link Result}s.
 * {@link Result}s with appropriate {@link ResultRenderer}s will be rendered in another {@link BorderPane} of the JavaFX GUI.
 * {@link Result}s with appropriate {@link ResultSaver}s may be stored to disk using a "Save" (or "Save all" in case of aggregate results) button also displayed
 * in the {@link TreeTableView}.
 * Top-level {@link Result}s may removed from the view (and the {@link ResultRepository}) via a "Remove" button.
 * This class implements {@link ResultConsumer} is is automatically notified if new results are added to the {@link ResultRepository}.
 * 
 * @author Jan Grau
 *
 */
public class ResultRepositoryRenderer implements ResultConsumer{

	private Control c;
	private TreeItem<Result> root;
	private HashMap<Result, TreeItem<Result>> itemMap;
	private TreeTableView<Result> ttv;
	private BorderPane viewerPane;
	private Application app;
	
	/**
	 * Creates a new {@link ResultRepositoryRenderer} for the current {@link Application}, where
	 * {@link Result}s with appropriate {@link ResultRenderer}s are displayed in the supplied {@link BorderPane}
	 * upon selection.
	 * @param viewerPane the pane for displaying results
	 * @param app the surrounding {@link Application}
	 */
	public ResultRepositoryRenderer(BorderPane viewerPane, Application app){
		itemMap = new HashMap<>();
		c = renderRepository();
		ResultRepository.getInstance().register( this );
		this.viewerPane = viewerPane;
		this.app = app;
	}
		
	/**
	 * Returns the Control (i.e., the {@link TreeTableView} in the current implementation) that renders the {@link ResultRepository} contents.
	 * @return the control
	 */
	public Control getControl(){
		return c;
	}
	
	/**
	 * Adds a listener to the list of elements in the {@link TreeTableView}.
	 * @param listener the listener
	 */
	public void addListener(ListChangeListener listener){
		root.getChildren().addListener( listener );
	}
	
	private Control renderRepository(){
	
		TreeTableColumn<Result, String> nameColumn = new TreeTableColumn<Result, String>("Name");
		nameColumn.setCellValueFactory( new Callback<TreeTableColumn.CellDataFeatures<Result,String>, ObservableValue<String>>() {
			
			@Override
			public ObservableValue<String> call( CellDataFeatures<Result, String> arg0 ) {
				
				//return new ReadOnlyStringWrapper( arg0.getValue().getValue().getComment() );
				/*Label lab = new Label(arg0.getValue().getValue().getName());
				String comment = arg0.getValue().getValue().getComment();
				if(comment != null && comment.trim().length() > 0){
					Tooltip tt = new Tooltip(comment);
					tt.setPrefWidth(300);
					tt.setWrapText(true);

					BooleanProperty shallVisible = new SimpleBooleanProperty(false);

					lab.setOnMouseEntered(new EventHandler<MouseEvent>() {

						@Override
						public void handle(MouseEvent event) {
							shallVisible.set(true);
							Point2D p = lab.localToScreen(lab.getLayoutBounds().getMaxX(), lab.getLayoutBounds().getMaxY());
							PauseTransition pt = new PauseTransition(Duration.millis(1000.0));
							pt.setOnFinished(new EventHandler<ActionEvent>() {
							
								@Override
								public void handle(ActionEvent event) {
									if(shallVisible.get()){
										tt.show(lab, p.getX(), p.getY());
									}
								}
								
							});
							pt.play();
						}

					});
					lab.setOnMouseExited(new EventHandler<MouseEvent>(){

						public void handle(MouseEvent event) {
							shallVisible.set(false);
							tt.hide();
						}

					});
				}*/
				//return new ReadOnlyObjectWrapper<Label>(lab);
				//SimpleObjectProperty<Label> prop = new SimpleObjectProperty<Label>(lab);
				SimpleObjectProperty<String> prop = new SimpleObjectProperty<String>(arg0.getValue().getValue().getName());
				return prop;
			}
		} );
		
		
		
		Callback<TreeTableColumn<Result, String>, TreeTableCell<Result, String>> cellFactory = (TreeTableColumn<Result,String> p) -> new TreeTableCell<Result,String>(){
			 private TextField textField;
			 
			@Override
	        public void startEdit() {
	            if (!isEmpty()) {
	                super.startEdit();
	                createTextField();
	                setText(null);
	                setGraphic(textField);
	                textField.selectAll();
	            }
	        }
	 
	        @Override
	        public void cancelEdit() {
	            super.cancelEdit();
	 
	            setGraphic(null);
	            setText( getItem() );
	            
	        }
	 
	        
	        @Override
	        public void updateItem(String item, boolean empty) {
	            super.updateItem(item, empty);
	            if(this.getTreeTableRow().getItem() != null) {
	            	String comment = this.getTreeTableRow().getItem().getComment();
	            	if(comment != null && comment.length()>0) {

	            		Tooltip tt = new Tooltip(comment);
	            		tt.setPrefWidth(300);
	            		tt.setWrapText(true);

	            		Tooltip.install(this, tt);
	            	}
	            }
	            if (empty) {
	            	setGraphic(null);
	                setText( getItem() );
	            } else {
	                if (isEditing()) {
	                    if (textField != null) {
	                        textField.setText(getString());
	                    }
	                    setText(null);
	                    setGraphic(textField);
	                } else {
	                	setGraphic(null);
	                	setText( getItem() );
	                }
	            }
	        }
	 
	        private void createTextField() {
	            textField = new TextField(getString());
	            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
	            textField.setOnAction(new EventHandler<ActionEvent>() {
					
					@Override
					public void handle( ActionEvent event ) {
						commitEdit(textField.getText());			
					}
				});
	           
	        }
	 
	        private String getString() {
	            return getItem() == null ? "" : getItem();
	        }
			
        };
		
            
            
            
		nameColumn.setCellFactory( cellFactory  );
        //nameColumn.setCellFactory( TextFieldTreeTableCell.forTreeTableColumn());
		nameColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Result,String>>() {

			@Override
			public void handle(CellEditEvent<Result, String> event) {
				event.getRowValue().getValue().rename(event.getNewValue());
				ResultRepository.getInstance().notifyRefresh(event.getRowValue().getValue());
			}
			
		});
		nameColumn.setEditable(true);
		
		
		
		
		
		TreeTableColumn<Result, String> valueColumn = new TreeTableColumn<Result, String>("Value");
		valueColumn.setCellValueFactory( new Callback<TreeTableColumn.CellDataFeatures<Result,String>, ObservableValue<String>>() {
			
			@Override
			public ObservableValue<String> call( CellDataFeatures<Result, String> arg0 ) {
				if(arg0.getValue().getValue() instanceof SimpleResult){
					return new ReadOnlyStringWrapper( arg0.getValue().getValue().getValue().toString() );
				}else{
					return new ReadOnlyStringWrapper( arg0.getValue().getValue().getDatatype().toString());
				}
			}
		} );
		
		
		
		TreeTableColumn<Result, Date> dateCol = new TreeTableColumn<Result, Date>("Date");
		dateCol.setCellValueFactory( new Callback<TreeTableColumn.CellDataFeatures<Result, Date>, ObservableValue<Date>>() {
			
			@Override
			public ObservableValue<Date> call( CellDataFeatures<Result, Date> arg0 ) {
				if(arg0.getValue().getValue() instanceof ToolResult){
					return new ReadOnlyObjectWrapper<Date>( ((ToolResult)arg0.getValue().getValue()).getFinishedDate() );
				}else{
					return null;
				}
			}
		} );
		
		
		TreeTableColumn<Result, Button> saveColumn = new TreeTableColumn<Result, Button>("Save");
		saveColumn.setCellValueFactory( new Callback<TreeTableColumn.CellDataFeatures<Result,Button>, ObservableValue<Button>>() {

			@Override
			public ObservableValue<Button> call( CellDataFeatures<Result, Button> arg0 ) {
				
				final Result res = arg0.getValue().getValue();
				final ResultSaver saver = ResultSaverLibrary.getSaver( res.getClass() );
				
				if(saver != null){

					String label = "Save...";
					if(!saver.isAtomic()){
						label = "Save all...";
					}
					
					Button btn = new Button( label );
					btn.setOnAction( new EventHandler<ActionEvent>() {

						@Override
						public void handle( ActionEvent arg1 ) {

							if(saver.isAtomic()){

								String[] ft = saver.getFileExtensions( res );
								
								String filename = res.getName().replaceAll( "[\\s\\:\\/]", "_" ) + (ft != null && ft.length>0 ? "."+ft[0] : "");
								
								for(int i=0;i<ft.length;i++){
									ft[i] = "*."+ft[i];
								}
								String name = Arrays.toString( ft ).replaceAll( "(\\[|\\])", "" ).toUpperCase();

								File f = LoadSaveDialogs.showSaveDialog(Application.mainWindow, filename , name, ft);
								if(f == null){
									return;
								}else{
									saver.writeOutput( res, f );
								}
							}else{
								//DirectoryChooser dir = new DirectoryChooser();
								
								File directory = LoadSaveDialogs.showDirectoryDialog(Application.mainWindow);//dir.showDialog( Application.mainWindow );
								
								if(directory == null){
									return;
								}else{
									saver.writeOutput( res, directory );
								}
								
							}
						}
					} );

					return new ReadOnlyObjectWrapper<Button>( btn );

				}else{
					return null;
				}
				
			}
			
			
			
		} );
		
		
		TreeTableColumn<Result, Button> removeColumn = new TreeTableColumn<Result, Button>("Remove");
		removeColumn.setCellValueFactory( new Callback<TreeTableColumn.CellDataFeatures<Result,Button>, ObservableValue<Button>>() {

			@Override
			public ObservableValue<Button> call( CellDataFeatures<Result, Button> arg0 ) {

				if(arg0.getValue().getParent() == root){

					Button btn = new Button( "Remove" );
					btn.setOnAction( new EventHandler<ActionEvent>() {

						@Override
						public void handle( ActionEvent arg1 ) {
							//System.out.println("removing "+arg0.getValue().getValue());
							
							Alert alert = new Alert( AlertType.CONFIRMATION );
							alert.setWidth( 300 );
							alert.setTitle( "Remove \""+arg0.getValue().getValue().getName()+"\"" );
							alert.setHeaderText( "Do you really want to remove \n\""+arg0.getValue().getValue().getName()+"\"\n from your workspace?" );
							alert.setContentText( "This cannot be undone." );
							ButtonType cancel = new ButtonType( "Cancel", ButtonData.CANCEL_CLOSE );
							ButtonType ok = new ButtonType( "Remove", ButtonData.OK_DONE );
							alert.getButtonTypes().setAll( cancel, ok );
							
							Optional<ButtonType> result = alert.showAndWait();
							
							if(result.get() == ok){
								ResultRepository.getInstance().remove( arg0.getValue().getValue() );
							}

						}
					} );

					return new ReadOnlyObjectWrapper<Button>( btn );
				}else{
					return null;
				}

			}
			
			
			
		} );
		
		
		
		TreeTableColumn<Result, Button> restartColumn = new TreeTableColumn<Result, Button>("Parameters");
		restartColumn.setCellValueFactory( new Callback<TreeTableColumn.CellDataFeatures<Result,Button>, ObservableValue<Button>>() {

			@Override
			public ObservableValue<Button> call( CellDataFeatures<Result, Button> arg0 ) {

				if(arg0.getValue().getParent() == root && arg0.getValue().getValue() instanceof ToolResult){

					Button btn = new Button( "Restore" );
					btn.setOnAction( new EventHandler<ActionEvent>() {

						@Override
						public void handle( ActionEvent arg1 ) {
							//System.out.println("restarting "+arg0.getValue().getValue());
							
							app.setParametersFromCopy( (ToolResult)arg0.getValue().getValue() );

						}
					} );

					return new ReadOnlyObjectWrapper<Button>( btn );
				}else{
					return null;
				}

			}
			
			
			
		} );
		
		
		
		
		
		
		root = new TreeItem<>(new CategoricalResult( "Root", "", "" ));
		
		
		List<Result> list = ResultRepository.getInstance().getResults();
				
		
		ttv = new TreeTableView<>();
				
		ttv.setShowRoot( false );
		
		ttv.getColumns().setAll( nameColumn, /*commentColumn,*/ valueColumn, dateCol, saveColumn, removeColumn , restartColumn);
		
		nameColumn.setPrefWidth(200);
		saveColumn.setPrefWidth(100);
		removeColumn.setPrefWidth(100);
		restartColumn.setPrefWidth(100);
		dateCol.setPrefWidth(200);
		//commentColumn.setPrefWidth(200);
		
		addResults(root,list);
		
		
		ttv.setRoot( root );
		
		ttv.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<TreeItem<Result>>() {

			@Override
			public void changed( ObservableValue<? extends TreeItem<Result>> arg0, TreeItem<Result> arg1, TreeItem<Result> arg2 ) {
				if(arg2 == null){
					
				}else{
					Result res = arg2.getValue();
					ResultRenderer renderer = ResultRendererLibrary.getRenderer( res );
					if(renderer != null){
						Node node = renderer.render( res, viewerPane );
						
						viewerPane.setCenter( node );
						
					}else{
						viewerPane.setCenter( null );
					}
				}
			}
			
		} );
		
		ttv.setEditable(true);
		
		//ttv.setColumnResizePolicy( TreeTableView.CONSTRAINED_RESIZE_POLICY );
		
		return ttv;
		
	}
	
	/**
	 * Adds a result to the rendered view. Will be called via the {@link ResultConsumer} interface.
	 * @param res the new {@link Result}
	 */
	public void addResult(Result res){
		addResult( res, root );
	}
	
	private void addResult(Result res, TreeItem<Result> root){
		if(res instanceof ResultSetResult){
			
			TreeItem<Result> parent = new TreeItem<Result>( res );
			itemMap.put( res, parent );
			root.getChildren().add( parent );
			
			ResultSet rs = ((ResultSetResult)res).getRawResult()[0];
			for(int i=0;i<rs.getNumberOfResults();i++){
				addResult( rs.getResultAt( i ), parent );
			}
		}else{
			TreeItem<Result> item = new TreeItem<>(res);
			itemMap.put( res, item );
			root.getChildren().add( item );
		}
		ttv.getSelectionModel().clearSelection();
	}

	/**
	 * Removes a result from the rendered view. Will be called via the {@link ResultConsumer} interface.
	 * @param res the new {@link Result}
	 * @return if the result could be removed
	 */
	public boolean removeResult(Result res){
		return removeResult( res, root );
	}
	
	private boolean removeResult(Result res, TreeItem<Result> root){
		//System.out.println("removing ["+res+"] from "+root);
		boolean b = root.getChildren().remove( itemMap.get( res ) );
		if(b){
			itemMap.remove( res );
			ttv.getSelectionModel().clearSelection();
			return b;
		}else{
			Iterator<TreeItem<Result>> it = root.getChildren().iterator();
			while(it.hasNext()){
				TreeItem<Result> item = it.next();
				Result r = item.getValue();
				if(r instanceof ResultSetResult){
					b = removeResult( res, item );
				}
				if(b){
					return true;
				}
			}
		}
		return false;
		
	}
	
	private void addResults(TreeItem<Result> root, List<Result> list){
		for(int i=0;i<list.size();i++){
			Result res = list.get( i );
			addResult(res, root);
		}
	}

	@Override
	public void notifyAdded( Result added ) {
		addResult(added);		
	}
	
	@Override
	public void notifyRemoved( Result removed ) {
		removeResult(removed);		
	}

	@Override
	public void notifyRefresh(Result renamed) {
		//TODO FIXME if renaming possible somewhere else!!!
	}
	
	
	
}
