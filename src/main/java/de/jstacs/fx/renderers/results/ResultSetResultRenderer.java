package de.jstacs.fx.renderers.results;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import de.jstacs.results.ResultSetResult;
import de.jstacs.tools.ToolResult;

/**
 * Renderer for a {@link ResultSetResult}. Currently, the {@link ResultSetResultRenderer#render(ResultSetResult, Pane)} method only returns an empty {@link VBox}
 * but may be extended to a structured view on a set of results in the future.
 * @author Jan Grau
 *
 */
public class ResultSetResultRenderer implements ResultRenderer<ResultSetResult> {

	/**
	 * Registers this {@link ResultRenderer} for classes {@link ResultSetResult} and {@link ToolResult}.
	 */
	public static void register(){
		ResultRendererLibrary.register( ResultSetResult.class, new ResultSetResultRenderer() );
		ResultRendererLibrary.register( ToolResult.class, new ResultSetResultRenderer() );
	}
	
	private ResultSetResultRenderer() {
		
	}

	@Override
	public Node render( ResultSetResult result, Pane parent ) {
		//ResultSet set = result.getRawResult()[0];
		
		final VBox box = new VBox();
		
		
		
		
		return box;
		
		/*ScrollPane sp = new ScrollPane( box );//TODO
		sp.setFitToWidth( true );
		
		box.setFillWidth( true );
		for(int i=0;i<set.getNumberOfResults();i++){
			Result res = set.getResultAt( i );
			ResultRenderer rend = ResultRendererLibrary.getRenderer( res ); 
			if(rend != null){
				Node n = rend.render( res, parent );
				if(n != null){
					box.getChildren().add( n );
				}else{
					System.out.println("null for: "+res.getName());
				}
			}
		}
		
		if(box.getChildren().size() > 0){
			
			return sp;
		}else{
			return null;
		}*/
		
	}

}
