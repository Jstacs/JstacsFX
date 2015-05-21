package de.jstacs.fx;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.prefs.Preferences;

import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import de.jstacs.fx.Messages.Level;
import de.jstacs.fx.renderers.parameters.ParameterSetRenderer;
import de.jstacs.fx.repository.ResultRepository;
import de.jstacs.fx.repository.ResultRepositoryRenderer;
import de.jstacs.io.FileManager;
import de.jstacs.parameters.Parameter;
import de.jstacs.parameters.ParameterSet;
import de.jstacs.results.ResultSetResult;
import de.jstacs.tools.JstacsTool;
import de.jstacs.tools.ProgressUpdater;
import de.jstacs.tools.Protocol;
import de.jstacs.tools.ToolResult;
import de.jstacs.utils.Pair;


public class Application {

	public class FXProtocol implements Protocol{

		private TextFlow flow;
		
		public FXProtocol() {
			flow = new TextFlow();
			flow.setId( "protocol" );
		}
		
		public TextFlow getTextFlow(){
			return flow;
		}
		
		@Override
		public synchronized void append( String str ) {
			
			Platform.runLater( new Runnable() {
				@Override
				public void run() {
					flow.getChildren().add( new Text(str) );
				}
			} );
			
		}

		@Override
		public synchronized void appendHeading( String heading ) {
			
			Platform.runLater( new Runnable() {
				@Override
				public void run() {
					Text text = new Text(heading);
					text.setId( "heading" );
					flow.getChildren().add( text );
				}
			} );
			
		}

		@Override
		public synchronized void appendWarning( String warning ) {
			
			Platform.runLater( new Runnable() {
				@Override
				public void run() {
					Text text = new Text(warning);
					text.setId( "warning" );
					flow.getChildren().add( text );
				}
			} );
			
		}

		@Override
		public void appendThrowable(Throwable th) {
			
			
			Platform.runLater( new Runnable() {
				@Override
				public void run() {
					StringWriter str = new StringWriter();
					
					th.printStackTrace(new PrintWriter(str));
					
					String strstr = str.toString();
					if(strstr.length() > 5000){
						strstr = strstr.substring(0, 5000)+"\n";
					}
					Text text = new Text(strstr);
					text.setId( "warning" );
					flow.getChildren().add( text );
				}
			} );
			
			
			
		}
		
	}
	
	public static class ToolReady{
		
		private Button runButton;
		private ParameterSet toolParameters;
		private LinkedList<Pair<Parameter,Label>> errorList;
		
		public ToolReady(Button runButton, ParameterSet toolParameters){
			this.runButton = runButton;
			this.toolParameters = toolParameters;
			this.errorList = new LinkedList<>();
		}
		
		public void addErrorLabel(Parameter par, Label error){
			errorList.add( new Pair<Parameter, Label>( par, error ) );
		}
		
		public void testReady(){
			runButton.setDisable( !toolParameters.hasDefaultOrIsSet() );
			for(int i=0;i<errorList.size();i++){
				Pair<Parameter,Label> pair = errorList.get( i );
				pair.getSecondElement().setText( pair.getFirstElement().getErrorMessage() );
			}
		}
		
	}
	
	public static Preferences prefs = Preferences.userNodeForPackage( Application.class );
	
	public static Window mainWindow;//TODO cleaner solution
	
	private ObservableList<Task<ResultSetResult>> enqueuedJobs;
	private HashMap<Task<ResultSetResult>,Pair<String,Date>> nameMap;
	
	private String title;
	private JstacsTool[] tools;
	private SplitPane parameterMain;
	private ProgressBar progressBar;
	private Label enqueued;
	private Messages messageOverlay;
	private FXProtocol protocol;
	private TaskViewer tasks;

	private HashMap<String,TitledPane> paneMap;
	
	public Application(String title, JstacsTool... tools){
		this.title = title;
		this.tools = tools;
		enqueuedJobs = FXCollections.observableArrayList();
		nameMap = new HashMap<>();
		this.tasks = new TaskViewer( enqueuedJobs, nameMap );
		this.paneMap = new HashMap<>();
	}
	
	
	private Pane getToolParameters(final JstacsTool tool, ParameterSet parameters ){
		
		ParameterSetRenderer renderer = new ParameterSetRenderer();
		
		//ParameterSet parameters = tool.getToolParameters();
		
		Label error = new Label();
		error.setWrapText( true );
		error.getStyleClass().add( "error" );
		
		Button b = new Button( "Run "+tool.getToolName()+"..." );
		b.setDisable( !parameters.hasDefaultOrIsSet() );
		
		Pane p = renderer.render( parameters, new ToolReady( b, parameters ) );
		
		
		
		p.getChildren().add( error );
		
		
		b.setOnAction( new EventHandler<ActionEvent>() {
			
			@Override
			public void handle( ActionEvent arg0 ) {
				
				boolean ready = parameters.hasDefaultOrIsSet();
				
				error.setText( parameters.getErrorMessage() );

				if(!ready){
					return;
				}
				
				/*ParameterSet tempPars2 = null;
				try {
					tempPars2 = parameters.clone();
				} catch ( CloneNotSupportedException e1 ) {
					e1.printStackTrace();
				}*///saving parameters
				
				//final ParameterSet tempPars = tempPars2;
				
				//parameterMain.setDisable( true );
				//System.out.println("setting progress bar "+progressBar);
				progressBar.setVisible( true );
				
				final Task<ResultSetResult> task = new Task<ResultSetResult>() {
					
					
					@Override
					protected ResultSetResult call() throws Exception {
						ProgressUpdater progress = new ProgressUpdater(){
							
							@Override
							public void setCurrent(double d){
								super.setCurrent( d );
								updateProgress( getPercentage(), 1.0 );
								
							}
							
						};
						
						protocol.appendHeading( "Starting "+tool.getToolName()+"...\n\n" );
						try{
							
							ToolResult res = tool.run( parameters, protocol, progress );//TODO

							
							protocol.append( "\n...finished.\n" );
							//System.out.println(res);
							
							//System.out.println("returning");
							protocol.append("\n######################################################\n\n\n");
							return res;
						}catch(Exception e){
							e.printStackTrace();
							StringWriter sw = new StringWriter();
							e.printStackTrace( new PrintWriter( sw ) );
							protocol.appendHeading( "Tool "+tool.getToolName()+" failed.\nError message: "+e.getMessage()+"\n\n" );
							protocol.appendWarning( sw.toString()+"\n" );
							protocol.append("\n######################################################\n\n\n");
							throw e;
							//return null;
						}
					}
				};
				
				task.setOnSucceeded( new EventHandler<WorkerStateEvent>() {

					@Override
					public void handle( WorkerStateEvent arg0 ) {
						System.out.println("onSucceeded");
						messageOverlay.displayMessage( tool.getToolName()+" finished successfully", Level.SUCCESS );
						enqueuedJobs.remove( arg0.getSource() );
						nameMap.remove( arg0.getSource() );
						//enqueued.setText( "("+Math.max( 0, enqueuedJobs.size()-1)+" Jobs pending)" );

						ResultSetResult toolResults = task.getValue();
						ResultRepository.getInstance().add( toolResults );
						System.out.println("succeeded");
						
					}
					
				} );
				
				EventHandler<WorkerStateEvent> eh = new EventHandler<WorkerStateEvent>() {

					@Override
					public void handle( WorkerStateEvent arg0 ) {
						enqueuedJobs.remove( arg0.getSource() );
						
						nameMap.remove( arg0.getSource() );
						//enqueued.setText( "("+Math.max( 0, enqueuedJobs.size()-1)+" Jobs pending)" );
						if( arg0.getEventType().equals( WorkerStateEvent.WORKER_STATE_FAILED ) ){
							messageOverlay.displayMessage( tool.getToolName()+" failed; see protocol for details", Level.WARNING );
						}else if( arg0.getEventType().equals( WorkerStateEvent.WORKER_STATE_CANCELLED ) ){
							messageOverlay.displayMessage( tool.getToolName()+" canceled; see protocol for details", Level.INFO );
						}else{
							messageOverlay.displayMessage( tool.getToolName()+" stopped", Level.INFO );
						}
						/*if(enqueuedJobs.size()>0){
							new Thread(enqueuedJobs.get(0)).start();
						}else{
							progressBar.setVisible( false );
						}*/
					}
					
				};
				

				task.setOnCancelled( eh );
				task.setOnFailed( eh );
				
				task.setOnRunning( new EventHandler<WorkerStateEvent>(){

					@Override
					public void handle( WorkerStateEvent arg0 ) {
						//enqueued.setText( "("+Math.max( 0, enqueuedJobs.size()-1)+" Jobs pending)" );
						progressBar.progressProperty().bind( arg0.getSource().progressProperty() );
					}
					
				} );
				
				
				
				
				
				nameMap.put( task, new Pair<String,Date>(tool.getToolName(), new Date( System.currentTimeMillis() )) );
				enqueuedJobs.add( enqueuedJobs.size(), task );
				//enqueued.setText( "("+Math.max( 0, enqueuedJobs.size()-1)+" Jobs pending)" );
				System.out.println("Job "+task+" enqueued");
				
				/*if(enqueuedJobs.size() == 1 ){
					Thread thread = new Thread(task);
					thread.setDaemon( true );
					thread.start();
				}*/
				
				//
				//parameterMain.setDisable( false );
				
			}
		} );
		
		p.getChildren().add( b );
		
		
		
		return p;
	}
	
	public void setParametersFromCopy(ToolResult res){
		String tn = res.getToolName();
		TitledPane target = paneMap.get( tn );
		
		for(int i=0;i<tools.length;i++){
			String name = tools[i].getToolName();
			if(name.equals( tn )){
				
				ParameterSet tps = tools[i].getToolParameters();
				System.out.println(tps);
				System.out.println(res.getToolParameters());
				res.setFromStoredParameters( tps );
				
				Pane content = getToolParameters( tools[i], tps );
				
				target.setContent( content );
				
				target.setExpanded( true );
				
				System.out.println("set ready");
				
				return;
			}
		}
		
	}
	
	private void addToolParameters(ScrollPane pane){
		
		//Accordion acc = new Accordion();
		
		VBox acc = new VBox();
		
		TitledPane[] titleds = new TitledPane[tools.length];
		
		for(int i=0;i<tools.length;i++){
			final int idx = i;
			String title = tools[i].getToolName();
			
			Pane content = getToolParameters( tools[i], tools[i].getToolParameters() );
			
			TitledPane titled = new TitledPane( );
			titled.setContent( content );
			//titled.setText( title );
			
			HBox head = new HBox();
			head.setAlignment( Pos.CENTER );
			head.getChildren().add( new Label(title) );
			Region reg = new Region();
			HBox.setHgrow( reg, Priority.ALWAYS );
			head.getChildren().add( reg );
			Button help = new Button( "?" );
			help.setId( "helpbutton" );
			help.setPrefSize( 20, 20 );
			help.setMinSize( 20, 20 );
			help.setOnAction( new EventHandler<ActionEvent>() {

				@Override
				public void handle( ActionEvent arg0 ) {
					new HelpViewer( tools[idx] );					
				}
				
			} );
			
			
			head.getChildren().add( help );
			
			pane.widthProperty().addListener( new ChangeListener<Number>(){

				@Override
				public void changed( ObservableValue<? extends Number> arg0, Number arg1, Number arg2 ) {
					head.setMinWidth( arg2.doubleValue()-50 );
					head.setMaxWidth( arg2.doubleValue()-50 );					
				}
				
			} );
			
			titled.setGraphic( head );
			
			
			
			paneMap.put(title,titled);
			
			titled.setExpanded( false );
			acc.getChildren().add( titled );
			//acc.getPanes().add( titled );
			titleds[i] = titled;
			
			titled.expandedProperty().addListener( new ChangeListener<Boolean>(){

				@Override
				public void changed( ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2 ) {
					if(arg2){
						for(int j=0;j<titleds.length;j++){
							if(titleds[j] != titled){
								titleds[j].setExpanded( false );
							}
						}
					}					
				}
				
			} );
			
		}
		
		
		pane.setContent( acc );
		
	}
	
	private HBox createStatusBar(){
		HBox statusBar = new HBox(20);
		statusBar.setPrefHeight( 30 );
		statusBar.setAlignment( Pos.CENTER );
		
		Region region = new Region();
	    region.setMinWidth( 10 );
	    
	    statusBar.getChildren().add( region );
		
		progressBar = new ProgressBar();
		progressBar.setVisible( false );
		progressBar.setPrefWidth( 200 );
		
		statusBar.getChildren().add( progressBar );
		
		enqueued = new Label("(0 Jobs pending)");
		
		statusBar.getChildren().add( enqueued );
		
		
		enqueuedJobs.addListener( new ListChangeListener<Task<ResultSetResult>>(){

			@Override
			public void onChanged( javafx.collections.ListChangeListener.Change<? extends Task<ResultSetResult>> arg0 ) {
				int num = Math.max( 0, arg0.getList().size()-1);
				String job = num == 1 ? "job" : "jobs";
				enqueued.setText( "("+num+" "+job+" pending)" );
				if(arg0.getList().size() > 0 && !arg0.getList().get( 0 ).isRunning()){
					Thread thread = new Thread(arg0.getList().get( 0 ));
					thread.setDaemon( true );
					thread.start();
					messageOverlay.displayMessage( nameMap.get( arg0.getList().get( 0 ) ).getFirstElement()+" started", Level.INFO );
				}else if(arg0.getList().size() == 0 ){
					progressBar.setVisible( false );
				}
			}
			
		} );
		
		
		
		Button b = new Button("Tasklist...");
		
		statusBar.getChildren().add( b );
		
		b.setOnAction( new EventHandler<ActionEvent>() {

			@Override
			public void handle( ActionEvent arg0 ) {
				System.out.println("showing tasks");
				tasks.show();				
			}
			
		} );
		
//		messages = new Label("");
//		statusBar.getChildren().add( messages );
		
		Region spacer = new Region();
	    HBox.setHgrow(spacer, Priority.ALWAYS);
	    
	    statusBar.getChildren().add( spacer );
	    
	    
	    CheckBox check = new CheckBox( "Autosave workspace" );
	    check.setAllowIndeterminate( false );
	    
	    check.selectedProperty().bindBidirectional( ResultRepository.autosave );
	    
	    check.setSelected( prefs.getBoolean( "autosave", false ) );
	    
	    check.selectedProperty().addListener( new ChangeListener<Boolean>(){

			@Override
			public void changed( ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2 ) {
				prefs.putBoolean( "autosave", arg2 );
				ResultRepository.getInstance().autostore();
			}
	    	
	    });
	    
	    
	    
	    Button save = new Button( "Save workspace..." );
	    
	    save.setOnAction( new EventHandler<ActionEvent>() {

			@Override
			public void handle( ActionEvent arg0 ) {
				FileChooser fc = new FileChooser();

				fc.getExtensionFilters().add( new FileChooser.ExtensionFilter( "JST", "*.jst" ) );
				
				File f = fc.showSaveDialog( mainWindow );
				if(f == null){
					return;
				}else{
					StringBuffer sb = ResultRepository.getInstance().storeResultsToXML();
					try {
						FileManager.writeFile( f, sb );
					} catch ( IOException e ) {
						e.printStackTrace();
						messageOverlay.displayMessage( "Storing failed", Level.WARNING );
					}
				}
			}
	    	
	    } );
	    
	    Button load = new Button( "Load workspace..." );
	    load.setOnAction( new EventHandler<ActionEvent>() {
			
			@Override
			public void handle( ActionEvent arg0 ) {
				FileChooser fc = new FileChooser();

				fc.getExtensionFilters().add( new FileChooser.ExtensionFilter( "JST", "*.jst" ) );
				
				File f = fc.showOpenDialog( mainWindow );
				if(f == null){
					return;
				}else{
					try {
						StringBuffer sb = FileManager.readFile( f );
						ResultRepository.getInstance().restoreResultsFromXML( sb );
					} catch ( Exception e ) {
						e.printStackTrace();
						messageOverlay.displayMessage( "Loading failed", Level.WARNING );
					}
				}
			}
		} );
	    
	    statusBar.getChildren().addAll( check,save,load );
	    
	    region = new Region();
	    region.setMinWidth( 10 );
	    
	    statusBar.getChildren().add( region );
		
		return statusBar;
	}
	
	
	public void startWithSplash( Stage primaryStage, String message ){
		
		Application app = this;
		
		Pane splashContent = SplashScreen.prepare( message );
		
		SplashScreen splash = new SplashScreen( splashContent );
		
		primaryStage.initStyle( StageStyle.UNDECORATED );
		primaryStage.setScene( splash );
		primaryStage.show();
		
		Stage mainStage = new Stage(StageStyle.DECORATED);
		
		Task<Pane> task = new Task<Pane>() {

			@Override
			protected Pane call() throws Exception {
				//System.out.println("starting...");
				try{
					Pane pane = app.prepare();
					//System.out.println("finished");
					return pane;
				}catch(Exception e){
					e.printStackTrace();
					throw e;
				}
				
			}
			
		};
		
		task.setOnSucceeded( new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle( WorkerStateEvent arg0 ) {
				primaryStage.close();
				Pane pane = task.getValue();
				app.showApplication( pane, mainStage );
			}
			
		} );
		
		new Thread(task).start();
		
	}
	
	private class MyTransition extends Transition{

		private Label lab2;
		private Tab tab;

		public MyTransition(Label lab2, Tab tab) {
			this.lab2 = lab2;
			this.tab = tab;
			this.tab.setOnSelectionChanged( new EventHandler<Event>() {

				@Override
				public void handle( Event arg0 ) {
					if(tab.isSelected()){
						lab2.setStyle( "-fx-text-fill: black" );
					}
				}
				
			} );
			this.setCycleDuration( Duration.millis( 1000 ) );
		}
		
		
		
		@Override
		protected void interpolate( double arg0 ) {
			
			if(!tab.isSelected()){
				if(lab2.getOpacity() < arg0){
					lab2.setStyle( "-fx-text-fill: darkblue" );
				}
			}
			lab2.setOpacity( 1.0-arg0*0.7 );
		}
		
	}
	
	private Transition getTransition(Tab tab){
		Label node = (Label)tab.getGraphic();
		/*FadeTransition ft = new FadeTransition( Duration.millis( 1000 ), node );*/
		MyTransition ft = new MyTransition( node, tab );
		ft.setCycleCount( 2 );
		ft.setAutoReverse( true );
		return ft;
	}
	
	private Node getTabLabel(String title){
		Label lab2 = new Label(title);
		lab2.setFont( Font.font( lab2.getFont().getName(), FontWeight.EXTRA_BOLD, lab2.getFont().getSize() ) );
		
		return lab2;
	}
	
	private Pane prepare(){
		BorderPane border = new BorderPane();
		
		HBox statusBar = createStatusBar();
		
		border.setBottom( statusBar );
		
		
		parameterMain = new SplitPane();
		
		ScrollPane parameterScroller = new ScrollPane();
		
		addToolParameters(parameterScroller);
		parameterScroller.setFitToWidth( true );
		
		parameterMain.setOrientation( Orientation.HORIZONTAL );
		
		SplitPane resultViewer = new SplitPane();
		resultViewer.setOrientation( Orientation.VERTICAL );
		
	//	ScrollPane resultScroller = new ScrollPane();
		
		BorderPane viewerPane = new BorderPane();
		
		ResultRepositoryRenderer rrr = new ResultRepositoryRenderer(viewerPane, this);
		Control c = rrr.getControl();
		
//		resultScroller.setContent( c );
		
		TabPane tabbed = new TabPane();
		tabbed.setTabClosingPolicy( TabClosingPolicy.UNAVAILABLE );
		Tab reposTab = new Tab();
		Node reposLab = getTabLabel( "Data" );
		reposTab.setGraphic( reposLab );
		reposTab.setContent( c );
		
		Transition reposTrans = getTransition( reposTab );
		rrr.addListener( new ListChangeListener(){

			@Override
			public void onChanged( Change arg0 ) {
				reposTrans.play();				
			}
			
		} );
		
		
		
		Tab protoTab = new Tab();
		Node protoLab = getTabLabel( "Protocol" );
		protoTab.setGraphic(protoLab );
		
		protocol = new FXProtocol();
		ScrollPane protSP = new ScrollPane( protocol.getTextFlow() );
		protSP.setFitToWidth( true );
		protSP.setId( "protpane" );
		
		protoTab.setContent( protSP );
		
		Transition protoTrans = getTransition( protoTab );
		
		protocol.getTextFlow().getChildren().addListener( new ListChangeListener<Node>(){

			@Override
			public void onChanged( javafx.collections.ListChangeListener.Change<? extends Node> arg0 ) {
				protoTrans.play();
			}
			
		} );
		
		
		tabbed.getTabs().addAll( reposTab, protoTab );
	
		
		resultViewer.getItems().addAll( viewerPane, tabbed );
		
		resultViewer.setDividerPositions( prefs.getDouble( "resultViewerDivider", 0.8 ) );
		resultViewer.getDividers().get( 0 ).positionProperty().addListener( new ChangeListener<Number>() {
			@Override
			public void changed( ObservableValue<? extends Number> arg0, Number arg1, Number arg2 ) {
				prefs.putDouble( "resultViewerDivider", arg2.doubleValue() );				
			}
		} );
		
		
		
		parameterMain.getItems().addAll( parameterScroller, resultViewer );
		
		
		parameterMain.setDividerPositions( prefs.getDouble( "parameterMainDivider", 0.3 ) );
		parameterMain.getDividers().get( 0 ).positionProperty().addListener( new ChangeListener<Number>() {
			@Override
			public void changed( ObservableValue<? extends Number> arg0, Number arg1, Number arg2 ) {
				prefs.putDouble( "parameterMainDivider", arg2.doubleValue() );				
			}
		} );
		
		
		border.setCenter( parameterMain );
		
		messageOverlay = new Messages();
		Pane messPane = messageOverlay.getMessagePane();
		
		StackPane p = new StackPane();
		p.getChildren().addAll( border, messPane );
		
		return p;
	}
	
	private void showApplication(Pane border, Stage primaryStage){
		Scene scene = new Scene(border, prefs.getDouble( "width", 1000 ), prefs.getDouble( "height", 700 ));
		
		scene.widthProperty().addListener( new ChangeListener<Number>() {
			@Override
			public void changed( ObservableValue<? extends Number> arg0, Number oldval, Number newval ) {
				prefs.putDouble( "width", newval.doubleValue() );
			}
		} );
		scene.heightProperty().addListener( new ChangeListener<Number>() {
			@Override
			public void changed( ObservableValue<? extends Number> arg0, Number oldval, Number newval ) {
				prefs.putDouble( "height", newval.doubleValue() );
			}
		} );
		
		//System.out.println("before stage");
		
		primaryStage.setScene(scene);
		primaryStage.getScene().getStylesheets().add( "de/jstacs/fx/application.css" );
		primaryStage.setTitle( title );
		
		mainWindow = primaryStage;
		
		primaryStage.show();
	}
	
	public void startApplication( Stage primaryStage ) throws Exception {
		
		Pane border = prepare();
		
		showApplication( border, primaryStage );
		
		
		
	}
	
}
