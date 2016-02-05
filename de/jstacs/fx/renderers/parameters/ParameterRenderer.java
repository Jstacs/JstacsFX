package de.jstacs.fx.renderers.parameters;

import javafx.scene.layout.Pane;
import de.jstacs.fx.Application.ToolReady;
import de.jstacs.parameters.Parameter;

/**
 * Interface for classes that render (display) parameters in the JavaFX GUI.
 * 
 * @author Jan Grau
 *
 * @param <T> the class of the parameter
 */
public interface ParameterRenderer<T extends Parameter> {

	/**
	 * Renders the given parameter in the JavaFX GUI.
	 * @param parameter the parameter that is rendered
	 * @param parent the parent pane that will contain the rendered parameter
	 * @param ready class that checks if all parameters have been specified. Any change to a parameter value should issue the method {@link ToolReady#testReady()} to test this condition.
	 */
	public void render(T parameter, Pane parent, ToolReady ready);
	
}
