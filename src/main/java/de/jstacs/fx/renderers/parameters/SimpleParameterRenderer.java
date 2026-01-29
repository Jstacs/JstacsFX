package de.jstacs.fx.renderers.parameters;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import de.jstacs.DataType;
import de.jstacs.fx.Application.ToolReady;
import de.jstacs.parameters.SimpleParameter;
import de.jstacs.parameters.SimpleParameter.IllegalValueException;

/**
 * Parameter renderer for {@link SimpleParameter}s as a simple input field ({@link TextField}).
 * @author Jan Grau
 *
 */
public class SimpleParameterRenderer extends AbstractParameterRenderer<SimpleParameter> {
	
	/**
	 * Registers this {@link ParameterRenderer} for the class {@link SimpleParameter}
	 */
	public static void register(){
		ParameterRendererLibrary.register( SimpleParameter.class, new SimpleParameterRenderer() );
	}
	
	private SimpleParameterRenderer() {
		
	}
	
	@Override
	protected void addInputs(final SimpleParameter parameter, Pane parent, Label name, Node comment, final Label error, ToolReady ready) {
		
		if(parameter.getDatatype() == DataType.BOOLEAN){
			
			final CheckBox cb = new CheckBox();
			cb.setIndeterminate(false);
			
			Boolean val = (Boolean) parameter.getValue();
			if(val == null) {
				val = false;
				try {
					parameter.setValue(false);
				} catch (IllegalValueException e) {
					e.printStackTrace();
				}
			}
			
			cb.setSelected(val);
			
			parent.getChildren().add(cb);
			
			cb.setOnAction( new EventHandler<ActionEvent>() {

				@Override
				public void handle( ActionEvent event ) {
					try {
						parameter.setValue( cb.isSelected() );
					} catch ( IllegalValueException e ) {
						try {
							parameter.setValue(null);
						} catch (IllegalValueException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}finally{
						ready.testReady();
						error.setText( parameter.getErrorMessage() );
					}				
				}
			} );
			
			
		}else{

			final TextField input = new TextField();
			if(parameter.hasDefaultOrIsSet()){
				input.setText( parameter.getValue().toString() );
			}
			parent.getChildren().add( input );
			
			
			ChangeListener<Boolean> cl = new ChangeListener<Boolean>() {

				@Override
				public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) {
					try {
						parameter.setValue( input.getText() );
						input.setText( parameter.getValue().toString() );
					} catch ( IllegalValueException e ) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}finally{
						error.setText( parameter.getErrorMessage() );
						ready.testReady();
					}
					
				}
				
			};
			
			input.focusedProperty().addListener( cl );
			
			
			
			
			ChangeListener<String> cl2 = new ChangeListener<String>() {
				
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					try{
						parameter.setValue(input.getText());
					}catch( IllegalValueException e ){
						
					}finally{
						error.setText( parameter.getErrorMessage() );
						ready.testReady();
					}
					
				}
			};
			
			input.textProperty().addListener(cl2);
			
			
			
			
			
			
			input.setOnAction( new EventHandler<ActionEvent>() {
				
				@Override
				public void handle( ActionEvent event ) {
					try {
						parameter.setValue( input.getText() );
						input.setText( parameter.getValue().toString() );
					} catch ( IllegalValueException e ) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}finally{
						ready.testReady();
						error.setText( parameter.getErrorMessage() );
					}				
				}
			} );

		}

	}

}
