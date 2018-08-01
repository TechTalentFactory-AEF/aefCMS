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

}
