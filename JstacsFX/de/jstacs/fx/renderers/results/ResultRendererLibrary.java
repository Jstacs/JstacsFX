package de.jstacs.fx.renderers.results;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.jstacs.results.Result;


public class ResultRendererLibrary {

	static{
		TextResultRenderer.register();
		PlotGeneratorResultRenderer.register();
		ResultSetResultRenderer.register();
		ListResultRenderer.register();
		DataSetResultRenderer.register();
	}
	
	private static HashMap<Class<? extends Result>, ResultRenderer> map;
	
	
	public static <T extends Result> void register(Class<? extends T> clazz, ResultRenderer<T> renderer){
		if(map == null){
			map = new HashMap<>();
		}
		map.put( clazz, renderer );
		//System.out.println("registered "+clazz+" "+renderer);
	}
	
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
