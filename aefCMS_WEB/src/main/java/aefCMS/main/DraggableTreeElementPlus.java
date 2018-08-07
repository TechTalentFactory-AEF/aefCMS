package aefCMS.main;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import biz.opengate.zkComponents.draggableTree.DraggableTreeElement;

public class DraggableTreeElementPlus extends DraggableTreeElement {

	private IndexVM fatherVm;
	private PageElement pageElement;

	public DraggableTreeElementPlus(DraggableTreeElement parent, String description, PageElement pageElement, IndexVM fatherVm) {	//added father to notify it of the movement
		super(parent, description);
		this.pageElement = pageElement;
		this.fatherVm=fatherVm;
	}

	public PageElement getPageElement() {
		return pageElement;
	}
	
	@Override
	protected void addAt(DraggableTreeElement spacer) {
		DraggableTreeElementPlus newParent = (DraggableTreeElementPlus) spacer.getParent();
		int oldIndex = pageElement.getParent().getChildren().indexOf(pageElement);
		int newIndex = spacer.getParent().getChildren().indexOf(spacer) / 2;
		newParent.getPageElement().getChildren().add(newIndex, pageElement);
		if (newParent != this.getParent()) {
			pageElement.getParent().getChildren().remove(oldIndex);
		} else {
			if (newIndex > oldIndex) {
				pageElement.getParent().getChildren().remove(oldIndex);
			} else {
				pageElement.getParent().getChildren().remove(oldIndex + 1);
			}
		}
		pageElement.setParent(newParent.getPageElement());
		super.addAt(spacer);
		
		try {
			notifyFatherOfElementsMovement();
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void addTo(DraggableTreeElement newParent) {
		DraggableTreeElementPlus newParentPlus = (DraggableTreeElementPlus) newParent;
		newParentPlus.getPageElement().getChildren().add(pageElement);	
		pageElement.getParent().getChildren().remove(this.getPageElement());
		pageElement.setParent(newParentPlus.getPageElement());
		super.addTo(newParent);
		
		try {
			notifyFatherOfElementsMovement();
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void notifyFatherOfElementsMovement() throws ResourceNotFoundException, ParseErrorException, Exception {
		fatherVm.moveElement();
	}
}
