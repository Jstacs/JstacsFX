package de.jstacs.fx;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jstacs.tools.JstacsTool;


/**
 * Class for displaying the help text of a {@link JstacsTool} ({@link JstacsTool#getHelpText()}) in the JavaFX GUI.
 * The help text is displayed in a separate window and the help text is converted from re-structured text to HTML.
 *  
 * @author Jan Grau
 *
 */
public class HelpViewer extends Viewer {

	/**
	 * Creates a new {@link HelpViewer} for a specific {@link JstacsTool}.
	 * @param tool the tool
	 */
	public HelpViewer(JstacsTool tool){
		super(tool);
		
		
	}
	
	
	protected String parse(JstacsTool tool){
		
		String restruct = tool.getHelpText();
		String[] lines = restruct.split( "\n" );
		
		Pattern bold = Pattern.compile( "\\*\\*(.+?)\\*\\*" );
		Pattern italics = Pattern.compile( "\\*(.+?)\\*" );
		Pattern tt = Pattern.compile( "\\`\\`(.+?)\\`\\`" );
		Pattern amp = Pattern.compile( "\"" );
		
		Pattern link = Pattern.compile( "^\\.\\.\\s+\\_(.*?)\\s*\\:\\s*(.*)$" );
		
		HashMap<Pattern, String> linkTargets = new HashMap<>();
		
		for(int i=0;i<lines.length;i++){
			
			Matcher m = bold.matcher( lines[i] );
			lines[i] = m.replaceAll( "<b>$1</b>" );
			
			m = italics.matcher( lines[i] );
			lines[i] = m.replaceAll( "<i>$1</i>" );
			
			m = tt.matcher( lines[i] );
			lines[i] = m.replaceAll( "<kbd>$1</kbd>" );
			
			m = amp.matcher( lines[i] );
			lines[i] = m.replaceAll( "&quot;" );
			
			
			
			m = link.matcher( lines[i] );
			
			if(m.matches()){
				String key = m.group( 1 );
				String target = m.group( 2 );
				linkTargets.put( Pattern.compile( "\\`?("+key+")\\`?\\_" ), target );
				lines[i] = "";
			}else{
				lines[i] = lines[i]+"<br>";
			}
			
		}
		
		
		
		for(int i=0;i<lines.length;i++){
			Set<Pattern> pats = linkTargets.keySet();
			Iterator<Pattern> it = pats.iterator();
			while( it.hasNext() ){
				Pattern pat = it.next();
				Matcher m = pat.matcher( lines[i] );
				lines[i] = m.replaceAll( "<a href=\""+linkTargets.get( pat )+"\">$1</a>" );
			}
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("<div style=\"font-family:sans-serif;\">");
		for(int i=0;i<lines.length;i++){
			sb.append( lines[i] );
			sb.append( "\n" );
		}
		sb.append("</div>");
		return sb.toString();
	}


	@Override
	protected String getTitle(JstacsTool tool) {
		return "Help for "+tool.getToolName();
	}
	
	
	
}
