package de.jstacs.fx.renderers.parameters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.jstacs.parameters.Parameter;

/**
 * Library of {@link ParameterRenderer}s that are currently available. Each {@link ParameterRenderer} must be registered
 * to this library using the {@link ParameterRendererLibrary#register(Class, ParameterRenderer)} method.
 * The appropriate renderer for a given parameter is obtained from {@link ParameterRendererLibrary#getRenderer(Parameter)}.
 * 
 * 
 * @author Jan Grau
 *
 */
public class ParameterRendererLibrary {

	static{
		FileParameterRenderer.register();
		SelectionParameterRenderer.register();
		SimpleParameterRenderer.register();
		DataColumnParameterRenderer.register();
	}
	
	
	private static HashMap<Class<? extends Parameter>, ParameterRenderer> map;
	
	/**
	 * Registers the given {@link ParameterRenderer} for the given {@link Parameter} subclass.
	 * @param clazz the class
	 * @param renderer the renderer
	 */
	public static <T extends Parameter> void register(Class<T> clazz, ParameterRenderer<T> renderer){
		//System.out.println("registered "+renderer+" "+clazz.getName());
		if(map == null){
			map = new HashMap<>();
		}
		map.put( clazz, renderer );
	}
	
	/**
	 * Gets an appropriate {@link ParameterRenderer} for the given {@link Parameter}.
	 * If no {@link ParameterRenderer} for the specific {@link Parameter} class can be found, 
	 * a {@link ParameterRenderer} registered for a superclass is returned. If such a {@link ParameterRenderer} is also
	 * not found, this method returns <code>null</code>
	 * @param parameter the parameter
	 * @return the renderer
	 */
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
