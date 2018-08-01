package aefCMS.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PageElement {

	private transient PageElement parent;
	private LibraryElement type;
	private Map<String, String> parameters;
	private List<PageElement> children;
	
	//CONSTRUCTORS
	
	public PageElement(LibraryElement type, Map<String, String> parameters) {
		this.type = type;
		this.parameters = parameters;
		this.children = new ArrayList<PageElement>();
	}
	
	public PageElement(PageElement parent, LibraryElement type, Map<String, String> parameters) {
		this(type,parameters);
		if (parent != null) {
			this.parent = parent;
			parent.getChildren().add(this);
		}
	}

	//GETTERS SETTERS
	
	public PageElement getParent() {
		return parent;
	}

	public void setParent(PageElement parent) {
		this.parent = parent;
	}

	public LibraryElement getType() {
		return type;
	}

	public void setType(LibraryElement type) {
		this.type = type;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public List<PageElement> getChildren() {
		return children;
	}

	public void setChildren(List<PageElement> children) {
		this.children = children;
	}

	//PRINT ELEMENT
	
	public void printRecursive() {
		printRecursive("-");
	}
	
	private void printRecursive(String dist) {
		
		System.out.print(dist + " type=" + type.getName() + ", parameters={ ");
		for (Entry<String, String> par : parameters.entrySet()) 
			System.out.print(par + ", ");
		System.out.println("}");
		
		if(! children.isEmpty()) {
			for (PageElement child : children)
				child.printRecursive(dist + " " + dist);
		}
	}

}
