package de.jstacs.fx;

import de.jstacs.tools.JstacsTool;

public class CitationViewer  extends Viewer {

	public CitationViewer(JstacsTool tool) {
		super(tool);
	}


	@Override
	protected String parse(JstacsTool tool) {
		String toolname = tool.getToolName();
		String[] refs = tool.getReferences();
		
		StringBuffer content = new StringBuffer();
		content.append("<b>If you use &quot;"+toolname+"&quot; in your research, please cite the following publication"+(refs != null && refs.length>1 ? "s" : "")+":</b><br>");
		
		content.append("<i>BibTeX format:</i><br><br>");
		for(int i=0;i<refs.length;i++) {
			String temp = refs[i].replaceAll("\n", "<br>");
			content.append(temp);
			content.append("<br><br><br>");
		}
		
		return content.toString();
	}

	@Override
	protected String getTitle(JstacsTool tool) {
		String[] refs = tool.getReferences();
		
		return "Citation"+(refs != null && refs.length>1 ? "s" : "")+" for "+tool.getToolName();
	}

	
	
}
