package de.jstacs.fx.renderers.parameters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.jstacs.parameters.Parameter;


public class ParameterRendererLibrary {

	static{
		FileParameterRenderer.register();
		SelectionParameterRenderer.register();
		SimpleParameterRenderer.register();
	}
	
	
	private static HashMap<Class<? extends Parameter>, ParameterRenderer> map;
	
	
	public static <T extends Parameter> void register(Class<T> clazz, ParameterRenderer<T> renderer){
		//System.out.println("registered "+renderer+" "+clazz.getName());
		if(map == null){
			map = new HashMap<>();
		}
		map.put( clazz, renderer );
	}
	
	public static <T extends Parameter> ParameterRenderer<T> getRenderer(T parameter){
		
		ParameterRenderer ren = map.get( parameter.getClass() );
		if(ren == null){
			Set<Class<? extends Parameter>> clazzes = map.keySet();
			Iterator<Class<? extends Parameter>> it = clazzes.iterator();
			while(it.hasNext()){
				Class<? extends Parameter> clazz = it.next();
				if(clazz.isAssignableFrom( parameter.getClass() )){
					return map.get( clazz );
				}
			}
		}
		if(ren == null){
			System.err.println( "Did not find a renderer for "+parameter.getClass()+". Custom renderers need to be registered by "+ParameterRendererLibrary.class.getName()+".register()." );
		}
		return ren;
	}
	
}
