package biz.opengate.zkComponents.draggableTree;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zul.Tree;

/**note: this class should not contain the component logic - that should go in the component class*/
public class DraggableTreeViewModel {
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	DEFINITION
	
	private DraggableTreeComponent component;	
	private DraggableTreeModel model;
	private DraggableTreeElement selectedElement;
	private Tree tree;

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	UTILITIES

	@AfterCompose 
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		tree = (Tree) view;
		component=(DraggableTreeComponent) view.getParent();
		component.viewModel=this;
	}
	
	@Command
	@NotifyChange("*")
	public void onDrop(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event) {
		component.onDrop(event);
	}

	@Command
	@NotifyChange("*")
	public void selectedNodeChanged() {
		setSelectedElement(tree.getSelectedItem().getValue());
		component.setSelectedElement(getSelectedElement());
		
	}
	
		
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	GETTERS / SETTERS

	public DraggableTreeModel getModel() {
		return model;
	}

	public void setModel(DraggableTreeModel model) {
		this.model = model;
		BindUtils.postNotifyChange(null, null, this, "*");
	}

	public DraggableTreeElement getSelectedElement() {
		return selectedElement;
	}

	public void setSelectedElement(DraggableTreeElement selectedElement) {
		this.selectedElement = selectedElement;
	}
}
