package biz.opengate.zkComponents.draggableTree;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.annotation.ComponentAnnotation;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Treeitem;

import biz.opengate.zkComponents.draggableTree.DraggableTreeElement.DraggableTreeElementType;

/**
http://localhost:8080/ZkComponentsTester/demo.zul

https://www.zkoss.org/wiki/ZK_Client-side_Reference/Customization/Drag-and-Drop_Effects
http://zkfiddle.org/sample/1b13tbb/1-Custom-Drag-message
https://www.zkoss.org/javadoc/7.0.0/jsdoc/zk/Widget.html
http://zkfiddle.org/sample/1hisp5k/3-Use-zAu-send-to-send-data-to-server-from-client#source-1
http://zkfiddle.org/sample/2vpcnq9/1-Update-by-client#
http://forum.zkoss.org/question/69044/access-java-code-and-component-attributesproperties-from-javascript-code/
https://www.zkoss.org/wiki/ZK_Component_Development_Essentials/Packing_as_a_Jar
http://forum.zkoss.org/question/23321/load-a-zul-from-inside-a-jar/
https://www.zkoss.org/wiki/ZK_Client-side_Reference/Language_Definition/Samples
*/
@ComponentAnnotation("selectedElement:@ZKBIND(ACCESS=both,SAVE_EVENT=onEdited)")
public class DraggableTreeComponent extends HtmlMacroComponent implements org.zkoss.zk.ui.ext.AfterCompose {
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	DEFINITION

	private static final long serialVersionUID = 1L;
	private final static Logger logger = Logger.getLogger(DraggableTreeComponent.class.getCanonicalName());
	
	protected DraggableTreeViewModel viewModel;
		
	public DraggableTreeComponent() {
		setMacroURI("~./draggableTreeComponent.zul");
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	UTILITIES

	@Override
	public void afterCompose() {
	    super.afterCompose(); //create components
	}
	
	protected void onDrop(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event) {		
		try {
			///////////////////////////////////////////////////////////////////
			//	MUST BE DRAGGING OUR ENTITIES
			final Object dragItem = event.getDragged();
			final Object dropItem = event.getTarget();
			if (!(dragItem instanceof Treeitem)) {
				return;
			}
			if (!(dropItem instanceof Treeitem)) {
				return;
			}
			
			final DraggableTreeElement dragged = (DraggableTreeElement) ((Treeitem) dragItem).getValue();
			final DraggableTreeElement dropped = (DraggableTreeElement) ((Treeitem) dropItem).getValue();
	
			logger.info("dragged:"+dragged.getDescription()+" dropped:"+dropped.getDescription());
			///////////////////////////////////////////////////////////////////
			
			if (!move(dragged,dropped)) {
				return;
			}
		}
		catch (Exception e) {
			logger.log(Level.WARNING,"",e);
		}
	}
		

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	PRIVATE UTILITIES
	
	private boolean containsRecursive(DraggableTreeElement object, DraggableTreeElement possibleChild) {
		if (object.equals(possibleChild)) {
			return true;
		}
		
		for (DraggableTreeElement child: object.getChildren()) {
			if (containsRecursive(child, possibleChild)) {
				return true;
			}
		}
		
		return false;
	}

	private boolean move(DraggableTreeElement dragged, DraggableTreeElement dropped) {
		///////////////////////////////////////////////////////////////////////
		//	NO LOOPS IN THE TREE
		if (containsRecursive(dragged, dropped)) {
			showWarning("Parent can not be moved into children.","Drag and drop warning");
			return false;
		}
		///////////////////////////////////////////////////////////////////////
		
		if (dropped.getType()!=DraggableTreeElementType.SPACER) {
			addTo(dragged, dropped);		
			return true;	
		}
		
		addAt(dragged, dropped);
		return false;
	}

	private void addAt(DraggableTreeElement element, DraggableTreeElement spacer) {
		//siblings
		if (element.getParent().equals(spacer.getParent())) {
			DraggableTreeElement parent=element.getParent();
			parent.removeChild(element);
			int index=parent.getChildren().indexOf(spacer);
			parent.getChildren().add(index, element);
			parent.recomputeSpacers();
		}
		else {
			removeFromParent(element);
			int index=spacer.getParent().getChildren().indexOf(spacer);
			spacer.getParent().getChildren().add(index, element);
			spacer.getParent().recomputeSpacers();
		}
	}

	private void addTo(DraggableTreeElement element, DraggableTreeElement newParent) {
		removeFromParent(element);
		element.setParent(newParent);
		newParent.getChildren().add(element);
		newParent.recomputeSpacers();
	}

	public static void removeFromParent(DraggableTreeElement element) {
		DraggableTreeElement parent=element.getParent();
		parent.removeChild(element);		
		parent.recomputeSpacers();
		element.setParent(null);
	}
	
	private int showWarning(String message, String title) {
		return showMessage(message, title, Messagebox.OK, Messagebox.EXCLAMATION);
	}
	
	private int showMessage(String message, String title, int buttons, String icon, Object ...params) {
		return Messagebox.show(message,title,buttons,icon);
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	GETTERS / SETTERS
	
	public DraggableTreeModel getModel() {
		if (viewModel==null) return null;
		return viewModel.getModel();
	}

	public void setModel(DraggableTreeModel model) {
		if (viewModel==null) return;
		viewModel.setModel(model);
	}


	public DraggableTreeElement getSelectedElement() {
		return viewModel.getSelectedElement();
	}


	public void setSelectedElement(DraggableTreeElement selectedElement) {
		viewModel.setSelectedElement(selectedElement);
		Events.postEvent("onEdited", DraggableTreeComponent.this, null);
//		BindUtils.postNotifyChange(null, null, this, "selectedElement");
		
		
	}
	
}
