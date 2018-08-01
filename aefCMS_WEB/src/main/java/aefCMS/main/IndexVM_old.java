package aefCMS.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.util.Clients;

import biz.opengate.zkComponents.draggableTree.DraggableTreeComponent;
import biz.opengate.zkComponents.draggableTree.DraggableTreeElement;
import biz.opengate.zkComponents.draggableTree.DraggableTreeModel;

public class IndexVM_old {
	
	private final String CONTEXT_PATH = WebApps.getCurrent().getServletContext().getRealPath("/");	//TODO change this (v. github issues)
	private final String LIBRARY_PATH 	   = CONTEXT_PATH + "WEB-INF/cms_library";
	private final String PAGETREE_SAVEFILE = CONTEXT_PATH + "saved_pagetree/pageTree.json";
	private final String POPUPS_PATH	   = "/WEB-INF/popups/" + "popup_";		//ex.  add -> /WEB-INF/popups/popup_add.zul
	private final String ELEM_ZUL_PATH     = "/WEB-INF/cms_library/";
	
	private Library lib;
	private HtmlRenderer iframeRenderer;
	
	private List<String> libraryElementList;
	
	private PageTree model;

	private DraggableTreeModel draggableTreeModel;
	private DraggableTreeElement draggableSelectedElement;
	
	private String selectedPopupType;
	private String selectedLibraryElement;
	
	Map<String, String> attributesHashMap = new HashMap<String, String>();
	
	//GETTERS SETTERS

	public List<String> getLibraryElementList() {		
		if (libraryElementList == null) {
			libraryElementList = new ArrayList<String>();
			for(LibraryElement libEl : lib.getElements())
				libraryElementList.add(libEl.getName());
			libraryElementList.sort(null);	
		}
		return libraryElementList;
	}
	
	public DraggableTreeModel getDraggableTreeModel() {
		PageElement root = model.getRoot();
		DraggableTreeElement draggableTreeRoot = new DraggableTreeElement(null, root.getType().getName());
		if (root.getChildren().size() > 0) {
			for (PageElement child : root.getChildren()) {
				createDraggableTreeElement(child, draggableTreeRoot);
			}
		}
		draggableTreeRoot.recomputeSpacersRecursive();
		return new DraggableTreeModel(draggableTreeRoot);
	}

	public DraggableTreeElement getDraggableSelectedElement() {
		return draggableSelectedElement;
	}

	public void setDraggableSelectedElement(DraggableTreeElement draggableSelectedElement) {
		this.draggableSelectedElement = draggableSelectedElement;
	}
	
	public String getSelectedPopupPath() {
		String path = null;
		if (selectedPopupType != null)
			path = POPUPS_PATH + selectedPopupType + ".zul";	
		return path;
	}
	
	public String getSelectedLibraryElement() {
		return selectedLibraryElement;
	}
	
	@NotifyChange("selectedLibraryElementZul")
	public void setSelectedLibraryElement(String selectedLibraryElement) {
		this.selectedLibraryElement = selectedLibraryElement;
	}
	
	public String getSelectedLibraryElementZul() {
		String path = null;
		if (selectedLibraryElement != null)
			path = ELEM_ZUL_PATH + selectedLibraryElement + "/" + "mask.zul";
		return path;
	}
	
	public Map<String, String> getAttributesHashMap() {
		return attributesHashMap;
	}

	public void setAttributesHashMap(Map<String, String> attributesHashMap) {
		this.attributesHashMap = attributesHashMap;
	}
	
	//INITIALIZATION

	@Init
	@NotifyChange("draggableTreeModel")
	public void init() throws Exception {
		
		lib = new Library(new File(LIBRARY_PATH));
		
		iframeRenderer = new HtmlRenderer(LIBRARY_PATH);
		
		//TODO DRAFT
		//init pageTree
		Map<String, String> stdPageAttributes = new HashMap<String, String>();
		stdPageAttributes.put("title", "My Web Page");
		PageElement stdPage = new PageElement(lib.getElement("stdPage"), stdPageAttributes);
		model = new PageTree(stdPage);
		
	}
	
	//POPUPS
	
	@Command
	@NotifyChange("selectedPopupPath")
	public void openPopup(@BindingParam("popupType") String popupType) {
		selectedPopupType = popupType;
	}
	
	@Command
	@NotifyChange("selectedPopupPath")
	public void closePopup() {
		selectedPopupType = null;
	}
	
	@Command
	public void saveElement() {
		
	}	
	
	//UTILITIES
	
	private void createDraggableTreeElement(PageElement node, DraggableTreeElement parent) {
		DraggableTreeElement draggableTreeNode = new DraggableTreeElement(parent, node.getType().getName());
		if (node.getChildren().size() > 0) {
			for (PageElement child : node.getChildren()) {
				createDraggableTreeElement(child, draggableTreeNode);
			}
		}
	}
	
	private void forceIframeRefresh() {		
		//force iframe refresh (using client-side js)
		Clients.evalJavaScript("document.getElementsByTagName(\"iframe\")[0].contentWindow.location.reload(true);");	//see: https://stackoverflow.com/questions/13477451/can-i-force-a-hard-refresh-on-an-iframe-with-javascript?lq=1
		System.out.println("**DEBUG** Forced Iframe refresh.");
	}
	
}
	