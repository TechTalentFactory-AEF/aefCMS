package biz.opengate.zkComponents.draggableTree;

import org.zkoss.zul.AbstractTreeModel;

public class DraggableTreeModel extends AbstractTreeModel<Object> {
	private static final long serialVersionUID = 1L;
	private DraggableTreeElement root;

	public DraggableTreeModel(DraggableTreeElement root) {
		super("Root");
		this.root = root;
	}
	
	@Override
	public Object getRoot() {
		return super.getRoot();
	};

	@Override
	public Object getChild(Object arg0, int arg1) {
		if (arg0 instanceof String) {
			return root;
		}			
		DraggableTreeElement t = (DraggableTreeElement) arg0;
		if (arg1 >= t.getChildren().size()) return null;
		return t.getChildren().get(arg1);
	}

	@Override
	public int getChildCount(Object arg0) {
		if (arg0 instanceof String) {
			return 1;
		}
		DraggableTreeElement t = (DraggableTreeElement) arg0;
		return t.getChildren().size();
	}

	@Override
	public boolean isLeaf(Object arg0) {
		return getChildCount(arg0) == 0;
	}
}