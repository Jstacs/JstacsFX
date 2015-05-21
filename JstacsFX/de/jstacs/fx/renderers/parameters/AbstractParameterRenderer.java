package de.jstacs.fx.renderers.parameters;


import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import de.jstacs.fx.Application.ToolReady;
import de.jstacs.parameters.Parameter;


public abstract class AbstractParameterRenderer<T extends Parameter> implements ParameterRenderer<T> {
	
	
	protected abstract void addInputs(T parameter, Pane parent, Label name, Node comment, Label error, ToolReady ready);
	
	@Override
	public void render( final T parameter, Pane parent, ToolReady ready ) {
		
		Label name = new Label( parameter.getName() );
		parent.getChildren().add( name );
		name.getStyleClass().add( "name" );
		
		Label comment = new Label( parameter.getComment()+(parameter.isRequired() ? "" : " (optional)") );
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
