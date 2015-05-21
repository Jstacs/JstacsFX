package de.jstacs.fx.renderers.parameters;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import de.jstacs.fx.Application.ToolReady;
import de.jstacs.parameters.SimpleParameter;
import de.jstacs.parameters.SimpleParameter.IllegalValueException;


public class SimpleParameterRenderer extends AbstractParameterRenderer<SimpleParameter> {
	
	public static void register(){
		ParameterRendererLibrary.register( SimpleParameter.class, new SimpleParameterRenderer() );
	}
	
	private SimpleParameterRenderer() {
		
	}
	
	@Override
	protected void addInputs(final SimpleParameter parameter, Pane parent, Label name, Node comment, final Label error, ToolReady ready) {
		
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
					e.printStackTrace();
				}finally{
					error.setText( parameter.getErrorMessage() );
					ready.testReady();
				}
				
			}
			
		};
		
		input.focusedProperty().addListener( cl );
		
		input.setOnAction( new EventHandler<ActionEvent>() {
			
			@Override
			public void handle( ActionEvent event ) {
				try {
					parameter.setValue( input.getText() );
					input.setText( parameter.getValue().toString() );
				} catch ( IllegalValueException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					ready.testReady();
					error.setText( parameter.getErrorMessage() );
				}				
			}
		} );

	}

}
