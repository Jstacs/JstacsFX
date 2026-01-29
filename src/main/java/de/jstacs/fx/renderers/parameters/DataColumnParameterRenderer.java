package de.jstacs.fx.renderers.parameters;

import java.util.HashMap;

import de.jstacs.fx.Application.ToolReady;
import de.jstacs.fx.renderers.results.TextResultRenderer;
import de.jstacs.parameters.FileParameter;
import de.jstacs.parameters.ParameterSet;
import de.jstacs.parameters.SimpleParameter.IllegalValueException;
import de.jstacs.tools.DataColumnParameter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * Class for rendering a {@link DataColumnParameter} in the JavaFX GUI.
 * 
 * The {@link DataColumnParameter} is rendered as a drop-down list of all admissible columns. 
 * If the reference table (referenced by name in{@link DataColumnParameter#getDataRef()}) appears to have a header (the first
 * row fails to convert to a number in all columns), these column headers are displayed, otherwise "Column 1", "Column 2",...
 * 
 * @see TextResultRenderer#getHeader(de.jstacs.parameters.FileParameter.FileRepresentation)
 * 
 * @author Jan Grau
 *
 */
public class DataColumnParameterRenderer extends AbstractParameterRenderer<DataColumnParameter> {

	
	/**
	 * Registers this {@link ParameterRenderer} for class {@link DataColumnParameter} in the {@link ParameterRendererLibrary}.
	 */
	public static void register(){
		ParameterRendererLibrary.register( DataColumnParameter.class, new DataColumnParameterRenderer() );
	}
	
	private HashMap<DataColumnParameter,FileParameter> cache;
	
	private DataColumnParameterRenderer() {
		cache = new HashMap<>();
	}

	@Override
	protected void addInputs(DataColumnParameter parameter, Pane parent, Label name, Node comment, Label error, ToolReady ready) {
		
		if(!cache.containsKey(parameter)){
			updateCache(parameter);
		}
		
		ObservableList<String> options = FXCollections.observableArrayList();
		
		final ChoiceBox<String> cb = new ChoiceBox<>();
		
		parent.getChildren().add(cb);
		
		cb.setItems( options );
		
		update(cb,parameter);
		
		cb.getSelectionModel().selectedIndexProperty().addListener( new ChangeListener<Number>(){

			@Override
			public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue ) {
				try {
					parameter.setValue(newValue.intValue()+1);
					
				} catch ( IllegalValueException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					ready.testReady();
					error.setText( parameter.getErrorMessage() );
				}
			}
			
		} );
		
		
		ChoiceBox box = FileParameterRenderer.getBox(cache.get(parameter));
		
		box.getSelectionModel().selectedItemProperty().addListener( new ChangeListener(){

			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				update(cb,parameter);				
			}
			
		} );
		
	}
	
	
	private void update(ChoiceBox<String> box, DataColumnParameter parameter){
		FileParameter cols = cache.get(parameter);

		if(cols.getFileContents() != null){

			Integer val = (Integer)parameter.getValue();
			
			int index = box.getSelectionModel().getSelectedIndex();
			
			if(val != null){
				index = val-1;
			}
			
			String[] opts = TextResultRenderer.getHeader(cols.getFileContents());

			if(opts[0] == null){
				for(int i=0;i<opts.length;i++){
					opts[i] = "Column "+(i+1);
				}
			}
			
			box.getItems().setAll(opts);
			
			if(index >= 0 && index < box.getItems().size()){
				box.getSelectionModel().select(index);
			}
			
		}else{
			box.getItems().clear();
		}
	}

	

	private void updateCache(DataColumnParameter parameter) {
		
		ParameterSet top = parameter.getParent();
		
		FileParameter found = DataColumnParameter.find(top,parameter.getDataRef());
		if(found != null){
			cache.put(parameter, found);
		}
	}

	

}
