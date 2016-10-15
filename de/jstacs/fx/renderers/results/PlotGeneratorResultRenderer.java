package de.jstacs.fx.renderers.results;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import de.jstacs.results.PlotGeneratorResult;
import de.jstacs.utils.graphics.RasterizedAdaptor;

/**
 * Class for rendering {@link PlotGeneratorResult}s in the JavaFX GUI.
 * The image from the {@link PlotGeneratorResult} is created using the {@link RasterizedAdaptor} and may be scaled using button controls (+,-,Fit). If the image does not fit the pane,
 * scroll bars are added to the pane.
 * 
 * @author Jan Grau
 *
 */
public class PlotGeneratorResultRenderer implements ResultRenderer<PlotGeneratorResult> {

	/**
	 * Registers this {@link ResultRenderer} for class {@link PlotGeneratorResult}.
	 */
	public static void register(){
		ResultRendererLibrary.register( PlotGeneratorResult.class, new PlotGeneratorResultRenderer() );
	}
	
	private HashMap<Object, WritableImage> map;
	
	private PlotGeneratorResultRenderer() {
		map = new HashMap<>();
	}

	@Override
	public Node render( PlotGeneratorResult result, Pane parent ) {

		try{
			
			WritableImage img2 = null;
			
			if(result.isStatic() && map.containsKey( result )){
				img2 = map.get( result );
			}else{
			
				RasterizedAdaptor ra = new RasterizedAdaptor( "png" );

				result.getValue().generatePlot( ra );

				BufferedImage im = ra.getImage();

				img2 = SwingFXUtils.toFXImage( im, null );
				
				if(result.isStatic()){
					map.put( result, img2 );
				}
			}
			
			final WritableImage img = img2;
			
			ImageView view = new ImageView( img );

			view.setCache( false );
			view.setSmooth( true );
			
			view.setPreserveRatio( true );
			
//			view.fitWidthProperty().bind( parent.widthProperty() );
			//System.out.println("parent: "+parent+" w: "+parent.getWidth());
		
			
			ScrollPane pane = new ScrollPane( view );
			
			HBox bar = new HBox(20);
			
			Button plus = new Button( "+" );
			plus.setOnAction( new EventHandler<ActionEvent>() {
				
				@Override
				public void handle( ActionEvent arg0 ) {
					view.setFitWidth( view.getFitWidth()*1.1 );
					view.setFitHeight(-1);
				}
			} );
			Button minus = new Button( "-" );
			minus.setOnAction( new EventHandler<ActionEvent>() {
				
				@Override
				public void handle( ActionEvent arg0 ) {
					view.setFitWidth( view.getFitWidth()/1.1 );	
					view.setFitHeight(-1);
				}
			} );
			
			double ratw = (parent.getWidth()-5)/img.getWidth();
			double rath = (parent.getHeight() - bar.getHeight()-5)/img.getHeight();
			final double rat = Math.min(ratw, rath );
			
			Button fit = new Button( "Fit" );
			fit.setOnAction( new EventHandler<ActionEvent>() {
				
				@Override
				public void handle( ActionEvent arg0 ) {
					view.setFitWidth( img.getWidth()*rat );
					view.setFitHeight( img.getHeight()*rat );
				}
			} );
			
			Button fitw = new Button( "Fit width" );
			fitw.setOnAction( new EventHandler<ActionEvent>() {
				
				@Override
				public void handle( ActionEvent arg0 ) {
					view.setFitWidth( parent.getWidth()-5 - (rath<ratw ? 15 : 0) );
					view.setFitHeight( -1 );
				}
			} );
			
			Button fith = new Button( "Fit height" );
			fith.setOnAction( new EventHandler<ActionEvent>() {
				
				@Override
				public void handle( ActionEvent arg0 ) {
					view.setFitWidth( -1 );
					view.setFitHeight(parent.getHeight() - bar.getHeight()-5 - (ratw<rath ? 15 : 0));
				}
			} );
			
			
			bar.getChildren().addAll( plus, minus, fit, fitw, fith );
			
			BorderPane both = new BorderPane();
			both.setTop( bar );
			both.setCenter( pane );
		
			view.setFitWidth( img.getWidth()*rat );
			view.setFitHeight( img.getHeight()*rat );
			
			return both;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

}
