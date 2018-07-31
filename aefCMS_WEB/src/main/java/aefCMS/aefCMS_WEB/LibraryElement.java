package aefCMS.aefCMS_WEB;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class LibraryElement {
	
	private String name;
	private transient String template;
	private List<String> attributes;
	
	//CONSTRUCTOR
	
	public LibraryElement(String name, String template) throws IOException {
		this.name = name;
		this.template = template;
		attributes = new ArrayList<String>();
		File file = new File(template);
		String string;

		string = FileUtils.readFileToString(file, "UTF-8");
		Pattern pattern = Pattern.compile("\\$\\!\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(string);
		while(matcher.find()) 
		    attributes.add(matcher.group(1));
	}
	
	//GETTERS SETTERS

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
	
}
