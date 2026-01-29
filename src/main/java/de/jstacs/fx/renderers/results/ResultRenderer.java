package de.jstacs.fx.renderers.results;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import de.jstacs.results.Result;

/**
 * Interface for classes that render (display) results in the JavaFX GUI.
 * 
 * @author Jan Grau
 *
 * @param <T> the class of the result
 */
public interface ResultRenderer<T extends Result> {

	/**
	 * Renders the provided result as a JavaFX {@link Node} assuming that the returned node is later added to the given {@link Pane}.
	 * @param result the result
	 * @param parent the containing pane
	 * @return the node that should be added to the pane
	 */
	public Node render(T result, Pane parent);
	
	
}
