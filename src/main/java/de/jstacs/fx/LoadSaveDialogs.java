package de.jstacs.fx;

import java.io.File;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Class providing static methods for displaying load and save dialogs.
 * @author Jan Grau
 *
 */
public class LoadSaveDialogs {

	/**
	 * the initial directory for loading files
	 */
	public static File initialLoadDirectory;
	/**
	 * the initial directory for storing files
	 */
	public static File initialSaveDirectory;
	

	/**
	 * Shows a load dialog tied to the owner window with a given description and an array of allowed file extensions.
	 * @param owner the owner window, may be <code>null</code>
	 * @param fileDescription the description of allowed files
	 * @param fileExtensions the extensions of allowed files
	 * @return the selected {@link File} or null if no file has been selected
	 */
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
	
	/**
	 * Shows a save dialog tied to the owner window with a given description and an array of allowed file extensions.
	 * @param owner the owner window, may be <code>null</code>
	 * @param initialFileName the initial file name for saving
	 * @param fileDescription the description of allowed files
	 * @param fileExtensions the extensions of allowed files
	 * @return the selected {@link File} or null if no file has been selected
	 */
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
	
	
	/**
	 * Shows a dialog tied to the owner window for selecting a directory.
	 * @param owner the owner window, may be <code>null</code>
	 * @return the selected directory
	 */
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
