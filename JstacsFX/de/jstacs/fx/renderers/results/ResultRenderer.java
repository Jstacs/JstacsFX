package de.jstacs.fx.renderers.results;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import de.jstacs.results.Result;


public interface ResultRenderer<T extends Result> {

	public Node render(T result, Pane parent);
	
	
}
