package de.jstacs.fx.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import de.jstacs.fx.Application;
import de.jstacs.fx.renderers.parameters.FileParameterRenderer;
import de.jstacs.io.FileManager;
import de.jstacs.io.NonParsableException;
import de.jstacs.io.XMLParser;
import de.jstacs.results.Result;
import de.jstacs.results.ResultSetResult;
import de.jstacs.results.TextResult;
import de.jstacs.results.savers.ResultSaver;
import de.jstacs.results.savers.ResultSaverLibrary;
import de.jstacs.utils.Compression;

/**
 * Singleton repository of all {@link Result} that have been created in an {@link Application} run.
 *  
 * @author Jan Grau
 *
 */
public class ResultRepository {

	/**
	 * Interface for classes that consume {@link Result}s, i.e., that need to react to the addition or removal
	 * of results from the repository. One prominent example of a {@link ResultConsumer} is the {@link FileParameterRenderer}
	 * that adjusts its drop-down list of admissible values to the contents of the {@link ResultRepository}, and the {@link ResultRepositoryRenderer} that
	 * always displays the current contents of the {@link ResultRepository}.
	 * 
	 * @author Jan Grau
	 *
	 */
	public static interface ResultConsumer{
		
		/**
		 * Notifies the {@link ResultConsumer} that a {@link Result} has been added to the {@link ResultRepository}.
		 * @param added the {@link Result} added
		 */
		public void notifyAdded(Result added);
		
		/**
		 * Notifies the {@link ResultConsumer} that a {@link Result} has been removed from the {@link ResultRepository}.
		 * @param removed the {@link Result} removed
		 */
		public void notifyRemoved(Result removed);
		
		public void notifyRefresh(Result renamed);
		
	}
	
	/**
	 * Global property for an {@link Application} run, if the workspace is automatically saved to disk.
	 */
	public static BooleanProperty autosave = new SimpleBooleanProperty( false );
	private static ResultRepository instance;
	
	private LinkedList<Result> results;
	private ArrayList<ResultConsumer> consumers;
	
	/**
	 * Returns the singleton instance of the {@link ResultRepository}.
	 * @return the repository
	 */
	public static ResultRepository getInstance(){
		if(instance == null){
			instance = new ResultRepository();
		}
		return instance;
	}
	
	private ResultRepository(){
		this.results = new LinkedList<>();
		this.consumers = new ArrayList<>();
		if(autosave.get()){
			File f = new File("autosave.jst");
			if(f.exists()){
				try{
					StringBuffer sb = FileManager.readFile( f );
					sb = new StringBuffer( Compression.unzip( sb.toString() ) );
					restoreResultsFromXML( sb );
				}catch(Exception e){
					e.printStackTrace( );
				}
			}
		}
	}
	
	/*public StringBuffer storeContents(){
		StringBuffer sb = new StringBuffer();
		XMLParser.appendObjectWithTags( sb, results.toArray( new Result[0] ), "results" );
		return sb;
	}*/
	
	/*public void loadContents(StringBuffer sb) throws NonParsableException{
		Result[] res = (Result[])XMLParser.extractObjectForTags( sb, "results" );
		Collections.addAll( results, res );
	}*/
	
	
	private void notifyAdd(Result result){
		for(int i=0;i<consumers.size();i++){
			consumers.get( i ).notifyAdded( result );
		}
	}
	
	public void notifyRefresh(Result renamed){
		for(int i=0;i<consumers.size();i++){
			consumers.get(i).notifyRefresh(renamed);
		}
	}
	
	/**
	 * Adds a result to the repository
	 * @param result the {@link Result} added
	 */
	public void add(Result result){
		add(result,true);
	}
	
	/**
	 * Adds a result to the repository. The repository is only autosaved if <code>store</code> is <code>true</code>.
	 * As many successive autostores could be inefficient for sequential addition of many objects, <code>store</code>
	 * should be <code>true</code> only for the last of a successive series of additions.
	 * @param result the {@link Result} added
	 * @param store if autosave should be considered.
	 */
	public void add(Result result, boolean store){
		this.results.add( result );
		notifyAdd( result );
		if(store){
			autostore();
		}
	}
	
	/**
	 * If {@link ResultRepository#autosave} is set to <code>true</code>, the contents of the repository
	 * are stored to disk in a separate {@link Thread}.
	 * @return the state of {@link ResultRepository#autosave} 
	 */
	public boolean autostore(){
		if(autosave.get()){
			new Thread( new Runnable(){

				@Override
				public void run() {
					try{
						FileManager.writeFile( "autosave.jst", Compression.zip( storeResultsToXML().toString() ) );
					}catch(IOException e){
						e.printStackTrace();
					}
				}
				
			} ).start();
			
			return true;
		}
		return false;
	}
	
	private void notifyRemove(Result result){
		for(int i=0;i<consumers.size();i++){
			consumers.get( i ).notifyRemoved( result );
		}
	}
	
	/**
	 * Removes a top-level result (i.e., no {@link Result} that are contained in {@link ResultSetResult}s)
	 * from the repository.
	 * @param result the {@link Result} removed
	 * @return if the {@link Result} was removed
	 */
	public boolean remove(Result result){//Only top-level results
		boolean b = this.results.remove( result );
		if(b){
			notifyRemove( result );
			autostore();
		}
		return b;
	}
	
	/**
	 * Returns all results (top-level and nested in {@link ResultSetResult}s) in the repository.
	 * @return the results
	 */
	public List<Result> getAllResults(){
		List<Result> res = getResults();
		for(int i=0;i<this.results.size();i++){
			Result temp = results.get( i );
			if(temp instanceof ResultSetResult){
				Result[] temp2 = ((ResultSetResult)temp).getRawResult()[0].getResults();
				addAllResults(res,temp2);
			}
		}
		return res;
	}
	
	private void addAllResults(List<Result> res, Result[] add){
		for(int i=0;i<add.length;i++){
			res.add( add[i] );
			if(add[i] instanceof ResultSetResult){
				Result[] temp2 = ((ResultSetResult)add[i]).getRawResult()[0].getResults();
				addAllResults(res,temp2);
			}
		}
	}
	
	/**
	 * Returns only the top-level {@link Result}s of this repository.
	 * @return the top-level {@link Result}s
	 */
	public List<Result> getResults(){
		return new ArrayList<>( this.results );
	}
	
	
	
	/**
	 * Returns all {@link Result}s in the repository that match the filter criteria.
	 * @param mime the mime type (or extension) of the {@link Result} that are to be returned
	 * @param extendedType an extended type, also {@link TextResult}s with extended type <code>null</code> are returned if they match <code>mime</code>
	 * @return the {@link Result}s
	 */
	public List<Result> filterByMimeAndExtendedType(String mime, String extendedType){
		ArrayList<Result> filtered = new ArrayList<>();
		Iterator<Result> it = getAllResults().iterator();
		while(it.hasNext()){
			Result r = it.next();
			if( r instanceof TextResult ){
				if(mime == null || TextResult.equals( mime, ((TextResult) r ).getMime() ) ){
					if(extendedType == null || ((TextResult) r).getExtendedType() == null || TextResult.equals( extendedType, ((TextResult) r).getExtendedType() ) ){
						filtered.add( r );
					}
				}
			}else{
				ResultSaver saver = ResultSaverLibrary.getSaver( r.getClass() );
				if(saver != null){
					String[] exts = saver.getFileExtensions( r );
					if(TextResult.equals( exts, mime )){
						filtered.add( r );
					}
				}
			}
		}
		return filtered;
	}

	/**
	 * Registers a {@link ResultConsumer} to this repository. All {@link ResultConsumer} registered will be notified
	 * for changes (additions or removals) in the repository.
	 * @param resultConsumer the consumer
	 */
	public void register( ResultConsumer resultConsumer ) {
		this.consumers.add( resultConsumer );
	}
	
	/**
	 * Returns the list of {@link ResultConsumer}s registered for the current {@link ResultRepository}.
	 * @return the consumers
	 */
	public ArrayList<ResultConsumer> getConsumers(){
		return this.consumers;
	}
	
	/**
	 * Restores the contents of a repository from its XML representation.
	 * The restored {@link Result}s are added to the current contents of the repository.
	 * @param sb the XML representation
	 * @throws NonParsableException if the XML could not be parsed
	 */
	public void restoreResultsFromXML(StringBuffer sb) throws NonParsableException{
		Result[] temp = (Result[])XMLParser.extractObjectForTags( sb, "results" );
		for(int i=0;i<temp.length;i++){
			this.add( temp[i], false );
		}
	}
	
	/**
	 * Stores the current contents of the repository to an XML representation.
	 * @return the XML representation
	 */
	public StringBuffer storeResultsToXML(){
		StringBuffer sb = new StringBuffer();
		Result[] temp = results.toArray( new Result[0] );
		XMLParser.appendObjectWithTags( sb, temp, "results" );
		return sb;
	}
	
	
}
