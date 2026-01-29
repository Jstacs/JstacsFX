package de.jstacs.fx.renderers.parameters;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import de.jstacs.fx.Application.ToolReady;
import de.jstacs.parameters.ExpandableParameterSet;
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
			VBox temp = new VBox();
			Parameter par = parameters.getParameterAt( i );
			ParameterRenderer renderer = ParameterRendererLibrary.getRenderer( par );
			renderer.render( par, temp, ready );
			temp.getChildren().add( new Separator() );
			box.getChildren().add(temp);
		}
		
		if(parameters instanceof ExpandableParameterSet){
			
			
			
			final HBox hbox = new HBox();
			Button plus = new Button("+");
			Button minus = new Button("-");
			if(parameters.getNumberOfParameters()==0){
				minus.setDisable(true);
			}
			
			hbox.getChildren().add(minus);
			hbox.getChildren().add(plus);
			hbox.setSpacing(20);
			box.getChildren().add(hbox);
			
			plus.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					try {
						((ExpandableParameterSet)parameters).addParameterToSet();
						
						Parameter added = ((ExpandableParameterSet)parameters).getParameterAt(((ExpandableParameterSet)parameters).getNumberOfParameters()-1);
						
						ParameterRenderer renderer = ParameterRendererLibrary.getRenderer( added );
						
						
						box.getChildren().remove(box.getChildren().size()-1);
						
						VBox temp = new VBox();
						
						renderer.render( added, temp, ready );
						
						temp.getChildren().add( new Separator() );
						box.getChildren().add(temp);
						
						box.getChildren().add(hbox);
						
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					if(parameters.getNumberOfParameters()>0){
						minus.setDisable(false);
					}
				}
			});
			
			minus.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					((ExpandableParameterSet)parameters).removeParameterFromSet();
					
					box.getChildren().remove(box.getChildren().size()-2);
					minus.setDisable(parameters.getNumberOfParameters()==0);
				}
			});
			
		}
		
		return box;
		
	}
	
}
