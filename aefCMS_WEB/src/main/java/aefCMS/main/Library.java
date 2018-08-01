package aefCMS.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Library {
	
	private List<LibraryElement> elements;

	public Library(File root) throws IOException {
		elements = new ArrayList<LibraryElement>();
		for (File directory : root.listFiles()) {
			elements.add(new LibraryElement(directory.getName(), directory.getPath() + "/template.vm"));
		}
	}
	
	public List<LibraryElement> getElements() {
		return elements;
	}
	
	public LibraryElement getElement(String name) {
		for (LibraryElement element : elements) {
			if (element.getName().equals(name)) {
				return element;
			}
		}
		return null;
	}

}