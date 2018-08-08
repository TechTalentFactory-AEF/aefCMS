package aefCMS.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class LibraryElement {
	
	private String name;
	private transient String path;
	private List<String> attributes;
	private List<String> allowedChildren;
	
	//CONSTRUCTOR
	
	public LibraryElement(String name, String path) throws IOException {
		this.name = name;
		this.path = path;
		attributes = new ArrayList<String>();
		allowedChildren = new ArrayList<String>();
		File templateFile = new File(path + "/template.vm");
		String templateString;
		File allowedChildrenFile = new File(path + "/allowedChildren.txt");
		String allowedChildrenString;

		templateString = FileUtils.readFileToString(templateFile, "UTF-8");
		Pattern pattern = Pattern.compile("\\$\\!\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(templateString);
		while(matcher.find()) {
		    attributes.add(matcher.group(1));
		}
		
		allowedChildrenString = FileUtils.readFileToString(allowedChildrenFile, "UTF-8");
		pattern = Pattern.compile("\\$\\{(.*?)\\}");
		matcher = pattern.matcher(allowedChildrenString);
		while(matcher.find()) {
			allowedChildren.add(matcher.group(1));
		}
	}
	
	//GETTERS SETTERS

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
	
	public List<String> getAllowedChildren() {
		return allowedChildren;
	}
	
}
