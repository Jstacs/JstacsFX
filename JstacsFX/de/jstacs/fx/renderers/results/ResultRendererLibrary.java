package de.jstacs.fx.renderers.results;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.jstacs.results.Result;

/**
 * Library of {@link ResultRenderer}s that are currently available. Each {@link ResultRenderer} must be registered
 * to this library using the {@link ResultRendererLibrary#register(Class, ResultRenderer)} method.
 * The appropriate renderer for a given parameter is obtained from {@link ResultRendererLibrary#getRenderer(Result)}.
 * 
 * 
 * @author Jan Grau
 *
 */
public class ResultRendererLibrary {

	static{
		TextResultRenderer.register();
		PlotGeneratorResultRenderer.register();
		ResultSetResultRenderer.register();
		ListResultRenderer.register();
		DataSetResultRenderer.register();
	}
	
	private static HashMap<Class<? extends Result>, ResultRenderer> map;
	
	/**
	 * Registers the given {@link ResultRenderer} for the given {@link Result} subclass.
	 * @param clazz the class
	 * @param renderer the renderer
	 */
	public static <T extends Result> void register(Class<? extends T> clazz, ResultRenderer<T> renderer){
		if(map == null){
			map = new HashMap<>();
		}
		map.put( clazz, renderer );
		//System.out.println("registered "+clazz+" "+renderer);
	}
	
	/**
	 * Gets an appropriate {@link ResultRenderer} for the given {@link Result}.
	 * If no {@link ResultRenderer} for the specific {@link Result} class can be found, 
	 * a {@link ResultRenderer} registered for a superclass is returned. If such a {@link ResultRenderer} is also
	 * not found, this method returns <code>null</code>
	 * @param result the result
	 * @return the renderer
	 */
	public static <T extends Result> ResultRenderer<T> getRenderer(T result){
		
		ResultRenderer ren = map.get( result.getClass() );
		if(ren == null){
			Set<Class<? extends Result>> clazzes = map.keySet();
			Iterator<Class<? extends Result>> it = clazzes.iterator();
			while(it.hasNext()){
				Class<? extends Result> clazz = it.next();
				if(clazz.isAssignableFrom( result.getClass() )){
					return map.get( clazz );
				}
			}
		}
		if(ren == null){
			System.err.println( "Did not find a renderer for "+result.getClass()+". Custom renderers need to be registered by "+ResultRendererLibrary.class.getName()+".register()." );
		}
		return ren;
	}
	
}
