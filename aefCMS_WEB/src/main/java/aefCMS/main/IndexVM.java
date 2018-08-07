package aefCMS.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Iframe;

import biz.opengate.zkComponents.draggableTree.DraggableTreeComponent;
import biz.opengate.zkComponents.draggableTree.DraggableTreeElement;
import biz.opengate.zkComponents.draggableTree.DraggableTreeModel;

public class IndexVM {
	
	// LOGGER
	
	private static final Logger logger = Logger.getLogger( IndexVM.class.getCanonicalName() );

	
	//PATHS
	
	private final String CONTEXT_PATH 	   = WebApps.getCurrent().getServletContext().getRealPath("/");	//TODO change this (v. github issues)
	private final String REL_LIBRARY_PATH  = "WEB-INF/cms_library";
	private final String ABS_LIBRARY_PATH  = CONTEXT_PATH + REL_LIBRARY_PATH;
	private final String POPUPS_PATH	   = "/WEB-INF/popups/" + "popup_";		//ex.   add -> /WEB-INF/popups/popup_add.zul
	private final String OUT_FILE_NAME	   = "index";							//ex. index -> index.html
	private final String SAVE_P_TREE_PATH  = CONTEXT_PATH + "WEB-INF/saved_page_tree.json";
	
	//ASSETS
	
	private Library lib;
	private HtmlRenderer iframeRenderer;
	private PageTree model;
	private File tempGeneratedWebSite;
	
	//ZK ATTRIBUTES
	
	@Wire("#iframeInsideZul")
	private Iframe iframeInsideZul;
	private String iframeWidth;
	
	private DraggableTreeModel draggableTreeModel;
	private DraggableTreeElementPlus draggableTreeRoot;
	private DraggableTreeElementPlus draggableSelectedElement;
	
	private String selectedPopupType;
	private List<LibraryElement> libraryElementList;
	private LibraryElement selectedLibraryElement;
	private Map<String, String> attributesHashMap = new HashMap<String, String>();
	
	//GETTERS SETTERS
	
	public List<LibraryElement> getLibraryElementList() {		
		if (libraryElementList == null) {
			libraryElementList = new ArrayList<LibraryElement>();
			for(LibraryElement libEl : lib.getElements()) {
				libraryElementList.add(libEl);
			}
		}
		return libraryElementList;
	}	
	public String getIframeWidth() {
		return iframeWidth;
	}

	public void setIframeWidth(String iframeWidth) {
		this.iframeWidth = iframeWidth;
	}
	
	//TODO modify this when loading from json	
	public DraggableTreeModel getDraggableTreeModel() throws IOException {	
		
		if (draggableTreeModel == null) {	
			//init draggableTree using PageTree model data
			PageElement modelRoot = model.getRoot();
			draggableTreeRoot = new DraggableTreeElementPlus(null, modelRoot.getType().getName(), modelRoot, this);
			
			if (modelRoot.getChildren().size() > 0) {
				for (PageElement child : modelRoot.getChildren()) {
					createDraggableTreeElement(child, draggableTreeRoot);
				}
			}
			draggableTreeModel = new DraggableTreeModel(draggableTreeRoot);
			draggableTreeRoot.recomputeSpacersRecursive();
		} 
		
		return draggableTreeModel;
	}
	
	
	
	
	
	
	public DraggableTreeElementPlus getDraggableTreeRoot() {
		return draggableTreeRoot;
	}
	
	public DraggableTreeElementPlus getDraggableSelectedElement() {
		return draggableSelectedElement;
	}
	
	public void setDraggableSelectedElement(DraggableTreeElement draggableSelectedElement) {
		if (draggableSelectedElement instanceof DraggableTreeElementPlus)
			this.draggableSelectedElement = (DraggableTreeElementPlus) draggableSelectedElement;
		else 
			this.draggableSelectedElement = null;
	}
	
	public String getSelectedPopupPath() {
		String path = null;
		if (selectedPopupType != null)
			path = POPUPS_PATH + selectedPopupType + ".zul";	
		return path;
	}
	
	public LibraryElement getSelectedLibraryElement() {
		return selectedLibraryElement;
	}
	
	@NotifyChange({"selectedLibraryElement","selectedLibraryElementZul","attributesHashMap"})
	public void setSelectedLibraryElement(LibraryElement selectedLibraryElement) {
		this.selectedLibraryElement = selectedLibraryElement;
		attributesHashMap.clear();	//clean the hashmap every time a different type is chosen (otherwise, when you return back to old type, the old values would still be there)
	}
	
	public String getSelectedLibraryElementZul() {
		String path = null;
		if (selectedLibraryElement != null)
			path = REL_LIBRARY_PATH + "/" + selectedLibraryElement + "/" + "mask.zul";
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
	public void init() throws ResourceNotFoundException, ParseErrorException, Exception {
		
		lib = new Library(new File(ABS_LIBRARY_PATH));
		
		iframeRenderer = new HtmlRenderer(ABS_LIBRARY_PATH);
		
		// try to reload pageTree from disk otherwise default page is created
		try {
			model = PageTreeSerializer.loadTreeFromDisc(SAVE_P_TREE_PATH, ABS_LIBRARY_PATH);
			logger.log( Level.INFO, "Loaded tree from disk");
		}
		catch (Exception e){
			e.printStackTrace();
			Map<String, String> stdPageAttributes = new HashMap<String, String>();
			stdPageAttributes.put("id", UUID.randomUUID().toString());
			stdPageAttributes.put("title", "My Web Page");
			stdPageAttributes.put("debug", "#f2f2f2");	//DEBUG  (#f2f2f2 = light gray)
			PageElement stdPage = new PageElement(lib.getElement("stdPage"), stdPageAttributes);
			model = new PageTree(stdPage);
			PageTreeSerializer.saveTreeToDisc(SAVE_P_TREE_PATH, model);
			logger.log( Level.INFO, "Default tree created and saved");
		}
		
		//create temporary file
		ServletContext webAppcontext = WebApps.getCurrent().getServletContext();
		File webAppTempDir = (File) webAppcontext.getAttribute("javax.servlet.context.tempdir");
		tempGeneratedWebSite = File.createTempFile(OUT_FILE_NAME, ".html", webAppTempDir);
		System.out.println("**DEBUG** (init) File tempGeneratedWebSite: " + tempGeneratedWebSite);
		tempGeneratedWebSite.deleteOnExit();
		
		//generate initial html and save it to file
		StringBuffer outputWebSiteHtml = iframeRenderer.render(model);
		saveWebSiteToFile(tempGeneratedWebSite, outputWebSiteHtml);
	}	
	
	@AfterCompose
	@NotifyChange("iframeWidth")
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws FileNotFoundException {
		Selectors.wireComponents(view, this, false);	//NOTE can't put this in a @init
		
		AMedia generatedWebSiteMedia = new AMedia(tempGeneratedWebSite, "text/html", null);
		iframeInsideZul.setContent(generatedWebSiteMedia);	
		
		iframeWidth = "100%";
	}
	
	//POPUPS
	
	@Command
	@NotifyChange({"selectedPopupPath", "selectedLibraryElementZul"})
	public void openPopup(@BindingParam("popupType") String popupType) {
		selectedPopupType = popupType;
		if (popupType.equals("modify")) {
			PageElement modelSelectedElement = draggableSelectedElement.getPageElement();
			selectedLibraryElement = modelSelectedElement.getType();
			attributesHashMap.putAll(modelSelectedElement.getParameters());
		}
	}
	
	@Command
	//calls NotifyChange procedurally, otherwise the notifications wouldn't happen when closePopup() is called not from the zul but from other methods
	public void closePopup() {
		selectedPopupType = null;
		selectedLibraryElement = null;   //WARNING if we don't clean it, when you open again the add popup, the old type will be already selected
		attributesHashMap.clear();		 //WARNING if we don't clean it, when you open again another popup, the old values will be selected
		
		BindUtils.postNotifyChange(null, null, this, "selectedPopupPath");
		BindUtils.postNotifyChange(null, null, this, "selectedLibraryElement");	
	}
	
	//IFRAME RESIZE
	
	@Command
	@NotifyChange("iframeWidth")
	public void resizeIFrame (@BindingParam("iframeWidth") String iframeWidth) {
		this.iframeWidth = iframeWidth;
	}
	
	//TREES OPERATIONS
	
	@Command
	@NotifyChange({"draggableTreeModel","draggableSelectedElement"})
	public void addElement() throws ResourceNotFoundException, ParseErrorException, Exception {
		attributesHashMap.put("id", UUID.randomUUID().toString());
		PageElement newPageElement = new PageElement(selectedLibraryElement, attributesHashMap);	//NOTE attributesHashMap values are *copied* inside the new element map
		model.addElement(newPageElement, draggableSelectedElement.getPageElement());
		
		DraggableTreeElementPlus newDraggableElementPlus = new DraggableTreeElementPlus(draggableSelectedElement, selectedLibraryElement.getName(), newPageElement, this);	//NOTE: the element is also added to the draggableTree
		draggableTreeRoot.recomputeSpacersRecursive();

		StringBuffer outputWebSiteHtml = iframeRenderer.render(model);
		saveWebSiteToFile(tempGeneratedWebSite, outputWebSiteHtml);
		forceIframeRefresh();
		PageTreeSerializer.saveTreeToDisc(SAVE_P_TREE_PATH, model);
		//draggableSelectedElement = newDraggableElementPlus;		//the new element will be selected after creation	//TODO TOFIX (in .zul there's only @save)
		closePopup();
		
		System.out.println("**DEBUG** (addElement) Model tree after add:");
		model.print();	
	}
	
	@Command
	@NotifyChange({"draggableTreeModel","draggableSelectedElement"})
	public void removeElement() throws ResourceNotFoundException, ParseErrorException, Exception {
		model.removeElement(draggableSelectedElement.getPageElement());
		
		DraggableTreeComponent.removeFromParent(draggableSelectedElement);
		draggableTreeRoot.recomputeSpacersRecursive();
		
		StringBuffer outputWebSiteHtml = iframeRenderer.render(model);
		saveWebSiteToFile(tempGeneratedWebSite, outputWebSiteHtml);
		forceIframeRefresh();
		PageTreeSerializer.saveTreeToDisc(SAVE_P_TREE_PATH, model);
		draggableSelectedElement = null;	//if not set null I could still select "add" button on the removed element!
		//TODO the father should be the selected element after the removal
		closePopup();
		
		System.out.println("**DEBUG** (removeElement) Model tree after remove:");
		model.print();	
	}
	
	@Command
	public void editElement() throws ResourceNotFoundException, ParseErrorException, Exception {
		draggableSelectedElement.getPageElement().setParameters(attributesHashMap);		//NOTE attributesHashMap values are *copied* inside the new element map
		
		StringBuffer outputWebSiteHtml = iframeRenderer.render(model);
		saveWebSiteToFile(tempGeneratedWebSite, outputWebSiteHtml);
		forceIframeRefresh();
		PageTreeSerializer.saveTreeToDisc(SAVE_P_TREE_PATH, model);
		closePopup();
		
		System.out.println("**DEBUG** (editElement) Model tree after edit:");
		model.print();	
	}
	
	//gets called by DraggableTreeElemenrPlus.java
	public void moveElement() throws ResourceNotFoundException, ParseErrorException, Exception {	
		System.out.println("**DEBUG** (moveElement) ***An element has been moved***");
		System.out.println("**DEBUG** (moveElement) The updated model tree:");
		model.print();
		StringBuffer outputWebSiteHtml = iframeRenderer.render(model);
		System.out.println("**DEBUG** (moveElement) The updated html:");
		System.out.println(outputWebSiteHtml);
		System.out.println("* * * * * * * * * * * * * * * * * * *");
		saveWebSiteToFile(tempGeneratedWebSite, outputWebSiteHtml);
		PageTreeSerializer.saveTreeToDisc(SAVE_P_TREE_PATH, model);
		forceIframeRefresh();
	}

	//EXPORT HTML
	
	@Command
	public void exportHtml() throws FileNotFoundException {
		Filedownload.save(tempGeneratedWebSite, "text/html");
	}
	
	//UTILITIES

	private void saveWebSiteToFile(File destinationFile , StringBuffer sourceHtml) throws IOException {
		FileUtils.writeStringToFile(destinationFile, sourceHtml.toString(), "UTF-8"); 
	}
	
	private void forceIframeRefresh() {	
		Clients.evalJavaScript("document.getElementsByTagName(\"iframe\")[0].contentWindow.location.reload(true);");	//see: https://stackoverflow.com/questions/13477451/can-i-force-a-hard-refresh-on-an-iframe-with-javascript?lq=1
		System.out.println("**DEBUG** (forceIframeRefresh) ***Done forced Iframe refresh***");
	}
	
	private void createDraggableTreeElement(PageElement node, DraggableTreeElement parent) {
		DraggableTreeElementPlus draggableTreeNode = new DraggableTreeElementPlus(parent, node.getType().getName(), node, this);
		if (node.getChildren().size() > 0) {
			for (PageElement child : node.getChildren()) {
				createDraggableTreeElement(child, draggableTreeNode);
			}
		}
	}
	
	/************************** MASKS CODE **************************/
	
	/* MASK1 */
	
	//
	
	/* MASKN */
	
	//	
	
}	
	