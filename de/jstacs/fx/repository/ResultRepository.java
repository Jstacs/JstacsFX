package de.jstacs.fx.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import de.jstacs.io.FileManager;
import de.jstacs.io.NonParsableException;
import de.jstacs.io.XMLParser;
import de.jstacs.results.Result;
import de.jstacs.results.ResultSetResult;
import de.jstacs.results.TextResult;
import de.jstacs.results.savers.ResultSaver;
import de.jstacs.results.savers.ResultSaverLibrary;
import de.jstacs.utils.Compression;


public class ResultRepository {

	public static interface ResultConsumer{
		
		public void notifyAdded(Result added);
		
		public void notifyRemoved(Result removed);
		
	}
	
	public static BooleanProperty autosave = new SimpleBooleanProperty( false );
	private static ResultRepository instance;
	
	private LinkedList<Result> results;
	private ArrayList<ResultConsumer> consumers;
	
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
	
	public void add(Result result){
		add(result,true);
	}
	
	public void add(Result result, boolean store){
		this.results.add( result );
		notifyAdd( result );
		if(store){
			autostore();
		}
	}
	
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
	
	public boolean remove(Result result){//Only top-level results
		boolean b = this.results.remove( result );
		if(b){
			notifyRemove( result );
			autostore();
		}
		return b;
	}
	
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
	
	public List<Result> getResults(){
		return new ArrayList<>( this.results );
	}
	
	
	
	
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
				ResultSaver saver = ResultSaverLibrary.getSaver( r );
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

	
	public void register( ResultConsumer resultConsumer ) {
		this.consumers.add( resultConsumer );
	}
	
	
	public void restoreResultsFromXML(StringBuffer sb) throws NonParsableException{
		Result[] temp = (Result[])XMLParser.extractObjectForTags( sb, "results" );
		for(int i=0;i<temp.length;i++){
			this.add( temp[i], false );
		}
	}
	
	public StringBuffer storeResultsToXML(){
		StringBuffer sb = new StringBuffer();
		Result[] temp = results.toArray( new Result[0] );
		XMLParser.appendObjectWithTags( sb, temp, "results" );
		return sb;
	}
	
	
}
