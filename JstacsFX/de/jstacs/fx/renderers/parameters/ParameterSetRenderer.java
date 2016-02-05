package de.jstacs.fx.renderers.parameters;

import javafx.scene.control.Separator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import de.jstacs.fx.Application.ToolReady;
import de.jstacs.parameters.Parameter;
import de.jstacs.parameters.ParameterSet;

/**
 * Class for renderering all {@link Parameter}s in a {@link ParameterSet}.
 * 
 * @author Jan Grau
 *
 */
public class ParameterSetRenderer {

	/**
	 * Renders all parameters in the given {@link ParameterSet}.
	 * @param parameters the set of parameters
	 * @param ready object that checks if all parameters have been specified.
	 * @return a {@link Pane} containg the rendered parameters
	 */
	public Pane render(ParameterSet parameters, ToolReady ready){
		
		if(parameters.getNumberOfParameters() == 0){
			return new VBox();
		}
		
		VBox box = new VBox();
		
		box.getStyleClass().add( "vbox" );
		
		for(int i=0;i<parameters.getNumberOfParameters();i++){
			Parameter par = parameters.getParameterAt( i );
			ParameterRenderer renderer = ParameterRendererLibrary.getRenderer( par );
			renderer.render( par, box, ready );
			box.getChildren().add( new Separator() );
		}
		
		
		return box;
		
	}
	
}
