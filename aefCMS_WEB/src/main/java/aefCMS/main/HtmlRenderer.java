package aefCMS.main;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

public class HtmlRenderer {
	
	private final String LOADER_TYPE = "file";
	private final String LOADER_CLASS = "org.apache.velocity.runtime.resource.loader.FileResourceLoader";
	
	private VelocityEngine ve;
	private String velocityTemplatesPath;

	public HtmlRenderer(String velocityTemplatesPath) throws Exception {
		
		this.velocityTemplatesPath = velocityTemplatesPath;
		
		Properties p = new Properties();
	    p.setProperty("resource.loader", LOADER_TYPE);
	    p.setProperty("file.resource.loader.class", LOADER_CLASS);
	    p.setProperty("file.resource.loader.path", velocityTemplatesPath);
	    
	    ve = new VelocityEngine();
		ve.init(p);
	}
	
	public StringBuffer render(PageElement pe) throws ResourceNotFoundException, ParseErrorException, Exception {

		Template peTemplate = ve.getTemplate(StringUtils.difference(velocityTemplatesPath, pe.getType().getTemplate()));
		VelocityContext peContext = new VelocityContext(pe.getParameters());
		
		if (! (pe.getChildren() == null || pe.getChildren().isEmpty())) {
			StringBuffer childrenOut = new StringBuffer();
			for (PageElement peChild : pe.getChildren()) {
				childrenOut.append(render(peChild));
			}
		peContext.put("children", childrenOut);		//WARNING the attribute is also added to the model!
		}
		
		StringWriter peOutput = new StringWriter();
		peTemplate.merge(peContext, peOutput);	
		peContext.remove("children");					//I remove it because I don't want the html code in the model
		
		return peOutput.getBuffer();
	}
	
	public StringBuffer render(PageTree tree) throws ResourceNotFoundException, ParseErrorException, Exception {
		return render(tree.getRoot());
	}
	
}
