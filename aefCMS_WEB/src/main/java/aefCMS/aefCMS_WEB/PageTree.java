package aefCMS.aefCMS_WEB;

public class PageTree {
	
	private PageElement root;
	
	//CONSTRUCTOR
	
	public PageTree(PageElement root) {
		this.root = root;
	}
	
	//GETTER
	
	public PageElement getRoot() {
		return root;
	}
	
	//TREE OPERATIONS
	
	public void addElement(PageElement element, PageElement parent, int siblingsPosition) {
		element.setParent(parent);
		parent.getChildren().add(siblingsPosition, element);
	}

	public void addElement(PageElement element, PageElement parent) {
		element.setParent(parent);
		parent.getChildren().add(element);	//append to the end of the list
	}
	
	public void removeElement(PageElement element) {
		element.getParent().getChildren().remove(element);
	}
	
	public void moveElement(PageElement element, PageElement newParent, int newSiblingsPosition) {
		removeElement(element);
		addElement(element, newParent, newSiblingsPosition);
	}
	
	//PRINT TREE
	
	public void print() {
		System.out.println("+ + + PRINTING MODEL TREE: " + root.getType().getName() + " + + +");
		root.printRecursive();
		System.out.println("+ + + + + + + + + + + + + + + + + + +");
	}
	
	
}
