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

	protected void addAt(DraggableTreeElement spacer) {
		super.addAt(spacer);
		this.getPageElement().getParent().getChildren().remove(this.getPageElement());
		DraggableTreeElementPlus newParent = (DraggableTreeElementPlus) spacer.getParent();
		this.getPageElement().setParent(newParent.getPageElement());
		int index = spacer.getParent().getChildren().indexOf(spacer) / 2;
		newParent.getPageElement().getChildren().add(index, this.getPageElement());
		
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

	protected void addTo(DraggableTreeElement newParent) {
		super.addTo(newParent);
		this.getPageElement().getParent().getChildren().remove(this.getPageElement());
		DraggableTreeElementPlus newParentPlus = (DraggableTreeElementPlus) newParent;
		this.getPageElement().setParent(newParentPlus.getPageElement());
		newParentPlus.getPageElement().getChildren().add(this.getPageElement());	
		
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
