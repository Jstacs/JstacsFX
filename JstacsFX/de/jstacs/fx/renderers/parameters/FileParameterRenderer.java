package de.jstacs.fx.renderers.parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import de.jstacs.fx.Application;
import de.jstacs.fx.LoadSaveDialogs;
import de.jstacs.fx.Application.ToolReady;
import de.jstacs.fx.repository.ResultRepository;
import de.jstacs.fx.repository.ResultRepository.ResultConsumer;
import de.jstacs.parameters.FileParameter;
import de.jstacs.parameters.FileParameter.FileRepresentation;
import de.jstacs.parameters.SimpleParameter.IllegalValueException;
import de.jstacs.results.Result;
import de.jstacs.results.ResultSetResult;
import de.jstacs.results.TextResult;
import de.jstacs.results.savers.ResultSaver;
import de.jstacs.results.savers.ResultSaverLibrary;

/**
 * Class for rendering a {@link FileParameter} in the JavaFX GUI.
 * 
 * The {@link FileParameter} is rendered as a drop-down list of all admissible files (or results) that are already in the {@link ResultRepository} and
 * a button for loading new files from disk. This button opens a file dialog for selecting files from disk.
 * 
 * @author Jan Grau
 *
 */
public class FileParameterRenderer extends AbstractParameterRenderer<FileParameter> {

	private static class ResultContainer{
		
		private Result res;
		
		private FileRepresentation frint2;
		
		public ResultContainer(Result fr){
			this.res = fr;
		}
		
		public FileRepresentation getFileRepresentation(){
			if(frint2 == null){
				if(res !=null){
					if(res instanceof TextResult){
						frint2 = ((TextResult)res).getValue();
					}else{
						ResultSaver saver = ResultSaverLibrary.getSaver( res.getClass() );
						if(saver != null){
							StringBuffer sb = new StringBuffer();
							saver.writeOutput( res, sb );
							frint2 = new FileRepresentation( "", sb.toString() );
							frint2.setExtension( saver.getFileExtensions( res )[0] );
						}
					}
				}
			}
			return frint2;
		}
		
		public String toString(){
			if(res == null){
				return "--- None ---";
			}else{
				return res.getName();
			}
		}
		
		public boolean equals(Object other){
			if(other instanceof ResultContainer && res != null ){
				return res.equals( ((ResultContainer)other).res );
			}else{
				return false;
			}
		}
		
		
	}
	
	/**
	 * Registers this {@link ParameterRenderer} for class {@link FileParameter} in the {@link ParameterRendererLibrary}.
	 */
	public static void register(){
		ParameterRendererLibrary.register( FileParameter.class, new FileParameterRenderer() );
	}
	
	/**
	 * Returns the {@link ChoiceBox} that represents this provided {@link FileParameter} in the GUI.
	 * @param parameter the parameter
	 * @return the box
	 */
	public static ChoiceBox getBox(FileParameter parameter){
		ArrayList<ResultConsumer> consumers = ResultRepository.getInstance().getConsumers();
		for(int i=0;i<consumers.size();i++){
			ResultConsumer cons = consumers.get(i);
			if(cons instanceof FileResultConsumer){
				if( ((FileResultConsumer)cons).parameter == parameter ){
					return ((FileResultConsumer) cons).box;
				}
			}
		}
		return null;
	}
	
	private FileParameterRenderer(){
		
	}
	
	private static class FileResultConsumer implements ResultConsumer{
		
		private FileParameter parameter;
		private ChoiceBox<ResultContainer> box;
		
		public FileResultConsumer(FileParameter parameter, ChoiceBox<ResultContainer> box){
			this.parameter = parameter;
			this.box = box;
		}
		
		public ChoiceBox getBox(){
			return box;
		}
		
		/*private void collectForAddition( Result added, LinkedList<ResultContainer> toAdd ){
			if(added instanceof TextResult && TextResult.equals( parameter.getAcceptedMimeType(), ((TextResult)added).getMime() )){
				//box.getItems().add( new ResultContainer( (TextResult)added ) );
				toAdd.add( new ResultContainer( (TextResult)added ) );
			}else if(added instanceof ResultSetResult){
				Result[] temp = ((ResultSetResult)added).getRawResult()[0].getResults();
				for(int i=0;i<temp.length;i++){
					collectForAddition( temp[i], toAdd );
				}
			}else{
				ResultSaver saver = ResultSaverLibrary.getSaver( added );
				if(saver != null){
					String[] exts = saver.getFileExtensions( added );
					if(TextResult.equals( exts, parameter.getAcceptedMimeType() )){
						box.getItems().add( new ResultContainer( added ) );
					}
				}
			}
		}*/
		
		@Override
		public void notifyAdded( Result added ) {
			LinkedList<ResultContainer> li = new LinkedList<>();
			collectForModification( added, li );
			
			box.getItems().addAll( li );
		}
		
		
		private void collectForModification(Result result, LinkedList<ResultContainer> toModify){
			if(result instanceof TextResult && TextResult.equals( parameter.getAcceptedMimeType(), ((TextResult)result).getMime() )){
				//box.getItems().remove( new ResultContainer( (TextResult)removed ) );
				toModify.add( new ResultContainer( (TextResult)result ) );
			}else if(result instanceof ResultSetResult){
				Result[] temp = ((ResultSetResult)result).getRawResult()[0].getResults();
				for(int i=0;i<temp.length;i++){
					collectForModification( temp[i], toModify );
				}
			}else{
				ResultSaver saver = ResultSaverLibrary.getSaver( result.getClass() );
				if(saver != null){
					String[] exts = saver.getFileExtensions( result );
					if(TextResult.equals( exts, parameter.getAcceptedMimeType() )){
						//box.getItems().remove( cont );
						toModify.add( new ResultContainer( result ) );
					}
				}
			}
		}
		
		@Override
		public void notifyRemoved( Result removed ) {
			
			LinkedList<ResultContainer> li = new LinkedList<>();
			collectForModification( removed, li );
			
			box.getItems().removeAll( li );
		}

		@Override
		public void notifyRefresh(Result renamed) {
			if(!box.getItems().isEmpty()){
				box.getItems().set(0, box.getItems().get(0));
			}
		}
		
		
		
	}
	
	
	private static ResultContainer[] getItems(FileParameter parameter){
		List<Result> files = ResultRepository.getInstance().filterByMimeAndExtendedType( parameter.getAcceptedMimeType(), null );
		
		ResultContainer[] items = new ResultContainer[files.size()];
		
		for(int i=0;i<files.size();i++){
			items[i] = new ResultContainer( files.get( i ) );
		}
		return items;
	}
	
	/**
	 * Loads the contents of a file. Opens a file dialog for selecting the input file, adds the corresponding {@link FileRepresentation}
	 * to the {@link ResultRepository}, and selects the loaded file in the drop-down list of admissible files.
	 * Extensions of accepted files are obtained from the {@link FileParameter#getAcceptedMimeType()} method.
	 * @param parameter the {@link FileParameter} that is rendered
	 * @param box the drop-down list of admissible files
	 * @param error the error label, required if a file could not be loaded
	 * @param ready the object for checking if all parameters have been set
	 */
	protected void loadFromFile(FileParameter parameter, ChoiceBox<ResultContainer> box, Label error, ToolReady ready){
		
		String[] ft = parameter.getAcceptedMimeType().split( "\\," );
		for(int i=0;i<ft.length;i++){
			ft[i] = "*."+ft[i];
		}
		
		File f = LoadSaveDialogs.showLoadDialog(Application.mainWindow, parameter.getAcceptedMimeType().toUpperCase(), ft);
		
		if(f == null){
			return;
		}
		
		FileRepresentation fr = new FileRepresentation(f.getAbsolutePath());
		TextResult fres = new TextResult( f.getName(), "", fr, parameter.getAcceptedMimeType(), "Loaded from file", parameter.getExtendedType(), false );
		
		try {
			
			fres.fill( parameter );
			//System.out.println("adding");
			ResultRepository.getInstance().add( fres );
			//System.out.println("added");

			box.getSelectionModel().select( new ResultContainer( fres ) );
			//System.out.println("selected: "+box.getSelectionModel().getSelectedIndex());
			
		} catch ( IllegalValueException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch ( CloneNotSupportedException ex ) {
			ex.printStackTrace();
		} finally {
			ready.testReady();
			error.setText( parameter.getErrorMessage() );
		}
		
	}
	
	@Override
	protected void addInputs( final FileParameter parameter, Pane parent, Label name, Node comment, Label error, ToolReady ready ) {
		
		ResultContainer[] items = getItems( parameter );
		
		
		final ChoiceBox<ResultContainer> box = new ChoiceBox<ResultContainer>();
		
		ObservableList<ResultContainer> ilist= FXCollections.observableArrayList( items );
		ilist.add(0, new ResultContainer(null));
		box.setItems( ilist );
		parent.getChildren().add( box );
		
		box.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<ResultContainer>(){

			@Override
			public void changed( ObservableValue<? extends ResultContainer> arg0, ResultContainer arg1, ResultContainer arg2 ) {
				try{
					//System.out.println("selected "+arg2);
					if(arg2 == null){
						parameter.reset();
					}else if(arg2.res instanceof TextResult){
						((TextResult)arg2.res).fill( parameter );
					}else if(arg2.getFileRepresentation() != null){
						parameter.setValue( arg2.getFileRepresentation() );
					}
				} catch ( IllegalValueException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (CloneNotSupportedException ex){
					ex.printStackTrace();
				}finally{
					ready.testReady();
					error.setText( parameter.getErrorMessage() );
				}
			}
			
			
			
		} );
		
		if(parameter.getFileContents() != null){
			String filename = parameter.getFileContents().getFilename();
			//System.out.println(">>"+filename+"<<");
			boolean found = false;
			if(filename != null && filename.length() > 0){
				
				for(int i=0;i<items.length && !found;i++){
					FileRepresentation temp = items[i].getFileRepresentation();
					//System.out.println(filename+" <-> "+temp.getFilename());
					if(temp != null && filename.equals( temp.getFilename() )){
						box.getSelectionModel().select( items[i] );
						//System.out.println("selected "+filename);
						found = true;
						break;
					}
				}
				
			}
			if(!found){
				String contents = parameter.getFileContents().getContent();
				//System.out.println(contents);
				for(int i=0;i<items.length ;i++){
					FileRepresentation temp = items[i].getFileRepresentation();
					//System.out.println(i+" content:");
					//System.out.println(temp.getContent());
					if(temp != null && contents.equals( temp.getContent() )){
						box.getSelectionModel().select( items[i] );
						//System.out.println("selected "+filename);
						found = true;
						break;
					}
				}
			}
			if(!found){
				error.setText( "Previous file \""+filename+"\" not found in repository." );
			}
		}
		
		ResultRepository.getInstance().register(new FileResultConsumer( parameter, box ));
		
		
		
		/*box.getSelectionModel().selectedIndexProperty().addListener( new ChangeListener<Number>(){

			@Override
			public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue ) {

				try {
					if((Integer)newValue < 0){
						parameter.setValue( null );
					}else{
						List<Result> files = ResultRepository.getInstance().filterByMimeAndExtendedType( parameter.getAcceptedMimeType(), parameter.getExtendedType() );
						TextResult fr = (TextResult)files.get( (Integer)newValue );
						fr.fill( parameter );
					}
				} catch ( IllegalValueException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					ready.testReady();
					error.setText( parameter.getErrorMessage() );
				}

			}
		} );*/
		
		Button but = new Button( "Load from file..." );
		parent.getChildren().add( but );
		
		but.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				loadFromFile( parameter, box, error, ready );
				ready.testReady();
			}
		});
		
		
	}
	
	
	

}
