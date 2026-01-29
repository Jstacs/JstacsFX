package de.jstacs.fx.renderers.parameters;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import de.jstacs.DataType;
import de.jstacs.fx.Application.ToolReady;
import de.jstacs.parameters.ParameterSet;
import de.jstacs.parameters.SelectionParameter;
import de.jstacs.parameters.SimpleParameter.IllegalValueException;

/**
 * {@link ParameterRenderer} for rendering {@link SelectionParameter} as a drop-down list ({@link ChoiceBox}) of possible options.
 * 
 * @author Jan Grau
 *
 */
public class SelectionParameterRenderer extends AbstractParameterRenderer<SelectionParameter> {
	
	/**
	 * Registers this {@link ParameterRenderer} for the class {@link SelectionParameter}.
	 */
	public static void register(){
		ParameterRendererLibrary.register( SelectionParameter.class, new SelectionParameterRenderer() );
	}
	
	private SelectionParameterRenderer() {
		
	}
	
	@Override
	protected void addInputs( final SelectionParameter parameter, Pane parent, Label name, Node comment, Label error, ToolReady ready ) {
		
		ParameterSet ps = parameter.getParametersInCollection();
		
		final String[] names = new String[ps.getNumberOfParameters()];
		for(int i=0;i<names.length;i++){
			names[i] = ps.getParameterAt( i ).getName();
		}
		
		final ChoiceBox<String> cb = new ChoiceBox<>();
		cb.setItems( FXCollections.observableArrayList( names ) );
		
		cb.getSelectionModel().select( parameter.getSelected() );
		
		parent.getChildren().add( cb );
		
		final Pane pane;
		final ParameterSetRenderer renderer;
		
		if(parameter.getDatatype() == DataType.PARAMETERSET){
			
			renderer = new ParameterSetRenderer();
			
			
			pane = renderer.render( (ParameterSet)parameter.getValue(), ready );
			
			parent.getChildren().add( pane );
			
		}else{
			pane = null;
			renderer = null;
		}
		
		cb.getSelectionModel().selectedIndexProperty().addListener( new ChangeListener<Number>(){

			@Override
			public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue ) {
				try {
					parameter.setValue( names[(Integer)newValue] );
					
					if(parameter.getDatatype() == DataType.PARAMETERSET){
						Pane temp = renderer.render( (ParameterSet)parameter.getValue(), ready );
						pane.getChildren().clear();
						pane.getChildren().addAll( temp.getChildren() );
						pane.getStyleClass().clear();
						pane.getStyleClass().addAll(temp.getStyleClass());
					}
					
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
