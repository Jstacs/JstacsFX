package de.jstacs.fx.renderers.parameters;


import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import de.jstacs.fx.Application.ToolReady;
import de.jstacs.parameters.Parameter;

/**
 * Abstract class for providing a default implementation of {@link ParameterRenderer#render(Parameter, Pane, Application.ToolReady)} that renders
 * all typical elements (name, comment, error messages) that should be common to all {@link Parameter} types.
 * 
 * @author Jan Grau
 *
 * @param <T> the class of the parameter
 */
public abstract class AbstractParameterRenderer<T extends Parameter> implements ParameterRenderer<T> {
	
	/**
	 * Adds the parameter-specific input fields to the provided parent pane.
	 * @param parameter the parameter that is rendered
	 * @param parent the parent pane containing the rendered parameter
	 * @param name the name label that has been created by {@link AbstractParameterRenderer#render(Parameter, Pane, Application.ToolReady)}
	 * @param comment the comment label that has been created by {@link AbstractParameterRenderer#render(Parameter, Pane, Application.ToolReady)}
	 * @param error the error label that has been created by {@link AbstractParameterRenderer#render(Parameter, Pane, Application.ToolReady)}
	 * @param ready object that checks if all parameters have been specified. Any change to a parameter value should issue the method {@link ToolReady#testReady()} to test this condition.
	 */
	protected abstract void addInputs(T parameter, Pane parent, Label name, Node comment, Label error, ToolReady ready);
	
	@Override
	public void render( final T parameter, Pane parent, ToolReady ready ) {
		
		Label name = new Label( parameter.getName()+(parameter.isRequired() ? "" : " (optional)") );
		parent.getChildren().add( name );
		name.getStyleClass().add( "name" );
		
		Label comment = new Label( parameter.getComment() );
		comment.setWrapText( true );
		comment.getStyleClass().add( "comment" );
		
		Label error = new Label( parameter.getErrorMessage() );
		error.setWrapText( true );
		error.getStyleClass().add( "error" );
		
		ready.addErrorLabel( parameter, error );
		
		addInputs(parameter,parent,name,comment,error,ready);
		
		parent.getChildren().add( comment );
		
		parent.getChildren().add( error );
		
		

	}

}
