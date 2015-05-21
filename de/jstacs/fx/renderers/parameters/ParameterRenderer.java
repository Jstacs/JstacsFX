package de.jstacs.fx.renderers.parameters;

import javafx.scene.layout.Pane;
import de.jstacs.fx.Application.ToolReady;
import de.jstacs.parameters.Parameter;


public interface ParameterRenderer<T extends Parameter> {

	public void render(T parameter, Pane parent, ToolReady ready);
	
}
