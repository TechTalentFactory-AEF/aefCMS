package biz.opengate.zkComponents.draggableTree;
import java.util.LinkedList;
import java.util.List;

public class DraggableTreeElement{

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	DEFINITION
	public enum DraggableTreeElementType{
		NORMAL,
		SPACER
	}

	private DraggableTreeElementType type;
	private transient DraggableTreeElement parent;
	private List<DraggableTreeElement> children=new LinkedList<DraggableTreeElement>();
	private String description;
	private boolean treeElementOpen=true;

	public DraggableTreeElement() {}
	
	public DraggableTreeElement(DraggableTreeElement parent, String description) {
		this.type=DraggableTreeElementType.NORMAL;
		this.parent=parent;
		if (parent!=null) {
			parent.children.add(this);
		}
		this.description=description;
	}
		
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	UTILITIES
	
	protected void removeChild(DraggableTreeElement child) {
		children.remove(child);
		if (children.isEmpty()) treeElementOpen=false;
	}
	
	public void recomputeSpacersRecursive() {
		recomputeSpacers();
		
		for (DraggableTreeElement child: children) {
			if (child.type!=DraggableTreeElementType.SPACER) {
				child.recomputeSpacersRecursive();
			}
		}
	}
	
	protected void recomputeSpacers() {
		if (children.size()==0) return;
		
		
		List<DraggableTreeElement> newList=new LinkedList<DraggableTreeElement>();
		addSpacer(newList);
		
		for (DraggableTreeElement child: children) {
			if (child.type!=DraggableTreeElementType.SPACER) {
				newList.add(child);
				addSpacer(newList);
			}
		}

		children.clear();
		children.addAll(newList);
	}
	
	private void addSpacer(List<DraggableTreeElement> list) {
		DraggableTreeElement spacer=new DraggableTreeElement();
		spacer.type=DraggableTreeElementType.SPACER;
		spacer.treeElementOpen=false;
		spacer.parent=this;
		list.add(spacer);		
	}
	
	
	
	protected void forceChildren(List<DraggableTreeElement> newList) {
		children.clear();
		children.addAll(newList);
		for (DraggableTreeElement child: children) {
			child.setParent(this);
		}
	}
	
	public boolean isDraggable() {
		return getParent()!=null && type!=DraggableTreeElementType.SPACER;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this==obj;
	}
	
	protected void addAt(DraggableTreeElement spacer) {
		//siblings
		if (this.getParent().equals(spacer.getParent())) {
			DraggableTreeElement parent=this.getParent();
			parent.removeChild(this);
			int index=parent.getChildren().indexOf(spacer);
			parent.getChildren().add(index, this);
			parent.recomputeSpacers();
		}
		else {
			DraggableTreeComponent.removeFromParent(this);
			this.setParent(spacer.getParent());
			int index=spacer.getParent().getChildren().indexOf(spacer);
			spacer.getParent().getChildren().add(index, this);
			spacer.getParent().recomputeSpacers();
		}
	}

	protected void addTo(DraggableTreeElement newParent) {
		DraggableTreeComponent.removeFromParent(this);
		this.setParent(newParent);
		newParent.getChildren().add(this);
		newParent.recomputeSpacers();
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	GETTERS / SETTERS
		
	public String getSclass() {
		switch (type) {
			default:
			case NORMAL: return "";
			case SPACER: return "spacerClass";
		}		
	}

	public DraggableTreeElement getParent() {
		return parent;
	}
	
	public void setParent(DraggableTreeElement parent) {
		this.parent = parent;
	}
	
	public List<DraggableTreeElement> getChildren() {
		return children;
	}

	public void setChildren(List<DraggableTreeElement> children) {
		this.children = children;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isTreeElementOpen() {
		return treeElementOpen;
	}

	public void setTreeElementOpen(boolean treeElementOpen) {
		this.treeElementOpen = treeElementOpen;
	}

	public DraggableTreeElementType getType() {
		return type;
	}

	public void setType(DraggableTreeElementType type) {
		this.type = type;
	}
}
