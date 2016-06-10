package de.jstacs.fx;

import java.io.File;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class LoadSaveDialogs {

	
	public static File initialLoadDirectory;//TODO for Linux
	public static File initialSaveDirectory;
	

	public static File showLoadDialog(Window owner, String fileDescription, String... fileExtensions ){
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(initialLoadDirectory);
		fc.getExtensionFilters().add( new FileChooser.ExtensionFilter( fileDescription, fileExtensions ) );
		
		File f = fc.showOpenDialog( owner );
		if(f != null){
			initialLoadDirectory = f.getParentFile();
		}
		return f;
	}
	
	public static File showSaveDialog(Window owner, String initialFileName, String fileDescription, String... fileExtensions ){
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(initialSaveDirectory);
		
		fc.getExtensionFilters().add( new FileChooser.ExtensionFilter( fileDescription, fileExtensions ) );
		fc.setInitialFileName(initialFileName);
		File f = fc.showSaveDialog( owner );
		if(f != null){
			initialSaveDirectory = f.getParentFile();
		}
		return f;
	}
	
	
	public static File showDirectoryDialog(Window owner ){
		DirectoryChooser dir = new DirectoryChooser();
		dir.setInitialDirectory(initialSaveDirectory);
		File directory = dir.showDialog( owner );
		if(directory != null){
			initialSaveDirectory = directory;
		}
		return directory;
	}
	
}
