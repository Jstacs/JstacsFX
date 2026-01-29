package de.jstacs.fx.renderers.parameters;

import de.jstacs.fx.Application.ToolReady;
import de.jstacs.parameters.ParameterSetContainer;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class ParameterSetContainerRenderer extends AbstractParameterRenderer<ParameterSetContainer> {

	/**
	 * Registers this {@link ParameterRenderer} for the class {@link SimpleParameter}
	 */
	public static void register(){
		ParameterRendererLibrary.register( ParameterSetContainer.class, new ParameterSetContainerRenderer() );
	}
	
	@Override
	protected void addInputs(ParameterSetContainer parameter, Pane parent, Label name, Node comment, Label error,
			ToolReady ready) {
		ParameterSetRenderer renderer = new ParameterSetRenderer();
		
		Pane pane = renderer.render( parameter.getValue(), ready );
		
		parent.getChildren().add( pane );		
	}

}
