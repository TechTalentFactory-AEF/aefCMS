package aefCMS.main;

import biz.opengate.zkComponents.draggableTree.DraggableTreeElement;

public class DraggableTreeElementPlus extends DraggableTreeElement {

	private PageElement pageElement;

	public DraggableTreeElementPlus(DraggableTreeElement parent, String description, PageElement pageElement) {
		super(parent, description);
		this.pageElement = pageElement;
	}

	public PageElement getPageElement() {
		return pageElement;
	}

	protected void addAt(DraggableTreeElement spacer) {
		super.addAt(spacer);
		this.getPageElement().getParent().getChildren().remove(this.getPageElement());
		DraggableTreeElementPlus newParent = (DraggableTreeElementPlus) spacer.getParent();
		this.getPageElement().setParent(newParent.getPageElement());
		int index = spacer.getParent().getChildren().indexOf(spacer) / 2;
		newParent.getPageElement().getChildren().add(index, this.getPageElement());
	}

	protected void addTo(DraggableTreeElement newParent) {
		super.addTo(newParent);
		this.getPageElement().getParent().getChildren().remove(this.getPageElement());
		DraggableTreeElementPlus newParentPlus = (DraggableTreeElementPlus) newParent;
		this.getPageElement().setParent(newParentPlus.getPageElement());
		newParentPlus.getPageElement().getChildren().add(this.getPageElement());		
	}
	
}
