package de.jstacs.fx.repository;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import de.jstacs.fx.Application;
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


public class ResultRepositoryRenderer implements ResultConsumer{

	private Control c;
	private TreeItem<Result> root;
	private HashMap<Result, TreeItem<Result>> itemMap;
	private TreeTableView<Result> ttv;
	private BorderPane viewerPane;
	private Application app;
	
	
	public ResultRepositoryRenderer(BorderPane viewerPane, Application app){
		itemMap = new HashMap<>();
		c = renderRepository();
		ResultRepository.getInstance().register( this );
		this.viewerPane = viewerPane;
		this.app = app;
	}
		
	public Control getControl(){
		return c;
	}
	
	public void addListener(ListChangeListener listener){
		root.getChildren().addListener( listener );
	}
	
	private Control renderRepository(){
	
		TreeTableColumn<Result, Label> nameColumn = new TreeTableColumn<Result, Label>("Name");
		nameColumn.setCellValueFactory( new Callback<TreeTableColumn.CellDataFeatures<Result,Label>, ObservableValue<Label>>() {
			
			@Override
			public ObservableValue<Label> call( CellDataFeatures<Result, Label> arg0 ) {
				//return new ReadOnlyStringWrapper( arg0.getValue().getValue().getComment() );
				Label lab = new Label(arg0.getValue().getValue().getName() );
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
				}
				
				return new ReadOnlyObjectWrapper<Label>(lab);
			}
		} );
		
		
		/*TreeTableColumn<Result, Label> commentColumn = new TreeTableColumn<Result, Label>("Comment");
		commentColumn.setCellValueFactory( new Callback<TreeTableColumn.CellDataFeatures<Result,Label>, ObservableValue<Label>>() {
			
			@Override
			public ObservableValue<Label> call( CellDataFeatures<Result, Label> arg0 ) {
				//return new ReadOnlyStringWrapper( arg0.getValue().getValue().getComment() );
				Label lab = new Label(arg0.getValue().getValue().getComment() );
				Tooltip tt = new Tooltip(arg0.getValue().getValue().getComment());
				tt.setPrefWidth(300);
				tt.setWrapText(true);
				lab.setOnMouseEntered(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						Point2D p = lab.localToScreen(lab.getLayoutBounds().getMaxX(), lab.getLayoutBounds().getMaxY());
				        tt.show(lab, p.getX(), p.getY());
					}
					
				});
				lab.setOnMouseExited(new EventHandler<MouseEvent>(){
					
					public void handle(MouseEvent event) {
						tt.hide();
					}
					
				});
				return new ReadOnlyObjectWrapper<Label>(lab);
			}
		} );
		*/
		
		
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
				final ResultSaver saver = ResultSaverLibrary.getSaver( res );
				
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

								for(int i=0;i<ft.length;i++){
									ft[i] = "*."+ft[i];
								}
								
								String name = Arrays.toString( ft ).replaceAll( "(\\[|\\])", "" ).toUpperCase();
								
								FileChooser fc = new FileChooser();

								fc.getExtensionFilters().add( new FileChooser.ExtensionFilter( name, ft ) );
								
								File f = fc.showSaveDialog( Application.mainWindow );
								if(f == null){
									return;
								}else{
									saver.writeOutput( res, f );
								}
							}else{
								DirectoryChooser dir = new DirectoryChooser();
								
								File directory = dir.showDialog( Application.mainWindow );
								
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
		
		
		
		TreeTableColumn<Result, Button> restartColumn = new TreeTableColumn<Result, Button>("Restart");
		restartColumn.setCellValueFactory( new Callback<TreeTableColumn.CellDataFeatures<Result,Button>, ObservableValue<Button>>() {

			@Override
			public ObservableValue<Button> call( CellDataFeatures<Result, Button> arg0 ) {

				if(arg0.getValue().getParent() == root && arg0.getValue().getValue() instanceof ToolResult){

					Button btn = new Button( "Restart" );
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
		
		//ttv.setColumnResizePolicy( TreeTableView.CONSTRAINED_RESIZE_POLICY );
		
		return ttv;
		
	}
	
	
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
	
	public boolean removeResult(Result res){
		return removeResult( res, root );
	}
	
	public boolean removeResult(Result res, TreeItem<Result> root){
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
	
	
}
