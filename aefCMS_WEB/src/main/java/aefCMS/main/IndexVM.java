package aefCMS.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.zkoss.zul.Iframe;

import biz.opengate.zkComponents.draggableTree.DraggableTreeComponent;
import biz.opengate.zkComponents.draggableTree.DraggableTreeModel;

public class IndexVM {
	
	//PATHS
	
	private final String CONTEXT_PATH 	   = WebApps.getCurrent().getServletContext().getRealPath("/");	//TODO change this (v. github issues)
	private final String REL_LIBRARY_PATH  = "WEB-INF/cms_library";
	private final String ABS_LIBRARY_PATH  = CONTEXT_PATH + REL_LIBRARY_PATH;
	private final String POPUPS_PATH	   = "/WEB-INF/popups/" + "popup_";		//ex.   add -> /WEB-INF/popups/popup_add.zul
	private final String OUT_FILE_NAME	   = "index";							//ex. index -> index.html
	
	//ASSETS
	
	private Library lib;
	private HtmlRenderer iframeRenderer;
	private PageTree model;
	private File tempGeneratedWebSite;
	
	//ZK ATTRIBUTES
	
	@Wire("#iframeInsideZul")
	private Iframe iframeInsideZul;
	
	private DraggableTreeModel draggableTreeModel;
	private DraggableTreeElementPlus draggableTreeRoot;
	private DraggableTreeElementPlus draggableSelectedElement;
	
	private String selectedPopupType;
	private List<String> libraryElementList;
	private String selectedLibraryElement;
	private Map<String, String> attributesHashMap = new HashMap<String, String>();
	
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
	
	//TODO STUB
	public DraggableTreeModel getDraggableTreeModel() {	
		if (draggableTreeModel == null) {
			//init draggableTree using PageTree model data
			PageElement modelRoot = model.getRoot();
			draggableTreeRoot = new DraggableTreeElementPlus(null, modelRoot.getType().getName(), modelRoot);
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

	public void setDraggableSelectedElement(DraggableTreeElementPlus draggableSelectedElement) {
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
	
	@NotifyChange({"selectedLibraryElement","selectedLibraryElementZul","attributesHashMap"})
	public void setSelectedLibraryElement(String selectedLibraryElement) {
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
		
		//TODO STUB: init pageTree model
		Map<String, String> stdPageAttributes = new HashMap<String, String>();
		stdPageAttributes.put("title", "My Web Page");
		stdPageAttributes.put("debug", "lightBlue");	//DEBUG
		PageElement stdPage = new PageElement(lib.getElement("stdPage"), stdPageAttributes);
		model = new PageTree(stdPage);
		
		//create temporary file
		ServletContext webAppcontext = WebApps.getCurrent().getServletContext();
		File webAppTempDir = (File) webAppcontext.getAttribute("javax.servlet.context.tempdir");
		tempGeneratedWebSite = File.createTempFile(OUT_FILE_NAME, ".html", webAppTempDir);
		System.out.println("**DEBUG** tempGeneratedWebSite: " + tempGeneratedWebSite);
		tempGeneratedWebSite.deleteOnExit();
		
		//generate initial html and save it to file
		StringBuffer outputWebSiteHtml = iframeRenderer.render(model);
		saveWebSiteToFile(tempGeneratedWebSite, outputWebSiteHtml);
	}	
	
	@AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws FileNotFoundException {
		Selectors.wireComponents(view, this, false);	//NOTE can't put this in a @init
		
		AMedia generatedWebSiteMedia = new AMedia(tempGeneratedWebSite, "text/html", null);
		iframeInsideZul.setContent(generatedWebSiteMedia);	
	}
	
	//POPUPS
	
	@Command
	@NotifyChange({"selectedPopupPath", "selectedLibraryElementZul"})
	public void openPopup(@BindingParam("popupType") String popupType) {
		selectedPopupType = popupType;
		if (popupType.equals("modify"))
			selectedLibraryElement = draggableSelectedElement.getPageElement().getType().getName();
	}
	
	@Command
	//calls NotifyChange procedurally, otherwise the notifications wouldn't happen when closePopup() is called not from the zul but from other methods
	public void closePopup() {
		selectedPopupType = null;
		selectedLibraryElement = null;   //if we don't clean it, when you open again the add popup, the old type will be already selected
		
		BindUtils.postNotifyChange(null, null, this, "selectedPopupPath");
		BindUtils.postNotifyChange(null, null, this, "selectedLibraryElement");	
	}
	
	//TREES OPERATIONS
	
	@Command
	@NotifyChange({"draggableTreeModel","draggableSelectedElement"})
	public void addElement() throws ResourceNotFoundException, ParseErrorException, Exception {
		PageElement newPageElement = new PageElement(lib.getElement(selectedLibraryElement), attributesHashMap);
		model.addElement(newPageElement, draggableSelectedElement.getPageElement());
		
		DraggableTreeElementPlus newDraggableElementPlus = new DraggableTreeElementPlus(draggableSelectedElement, selectedLibraryElement, newPageElement);	//NOTE: the element is also added to the draggableTree
		draggableTreeRoot.recomputeSpacersRecursive();

		StringBuffer outputWebSiteHtml = iframeRenderer.render(model);
		saveWebSiteToFile(tempGeneratedWebSite, outputWebSiteHtml);
		forceIframeRefresh();
		
		//draggableSelectedElement = newDraggableElementPlus;		//the new element will be selected after creation	//TODO TOFIX (in .zul there's only @save)
		closePopup();
		
		model.print();	//DEBUG
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
		
		draggableSelectedElement = null;	//if not set null I could still select "add" button on the removed element!
		//TODO the father should be the selected element after the removal
		closePopup();
		
		model.print();	//DEBUG
	}
	
	@Command
	public void editElement() throws ResourceNotFoundException, ParseErrorException, Exception {
		draggableSelectedElement.getPageElement().setParameters(attributesHashMap);
		
		StringBuffer outputWebSiteHtml = iframeRenderer.render(model);
		saveWebSiteToFile(tempGeneratedWebSite, outputWebSiteHtml);
		forceIframeRefresh();
		
		closePopup();
		
		model.print();	//DEBUG
	}
	
	//UTILITIES

	private void saveWebSiteToFile(File destinationFile , StringBuffer sourceHtml) throws IOException {
		FileUtils.writeStringToFile(destinationFile, sourceHtml.toString(), "UTF-8"); 
	}
	
	private void forceIframeRefresh() {	
		Clients.evalJavaScript("document.getElementsByTagName(\"iframe\")[0].contentWindow.location.reload(true);");	//see: https://stackoverflow.com/questions/13477451/can-i-force-a-hard-refresh-on-an-iframe-with-javascript?lq=1
		System.out.println("**DEBUG** Forced Iframe refresh.");
	}
	
}	
	
/*****************************************************************************************/
	
	
//	private DraggableTreeCmsElement root;
//	private DraggableTreeModel model;
//	private DraggableTreeCmsElement selectedElement;
//	
//	private List<String> fragmentTypeList;
//	private String selectedFragmentType;
//	
//	private String selectedPopup;
//	private String popupType;
//
//	private PageManipulator pageManip;
//	private FragmentGenerator fragmentGen;
//	
//	
//	/////GETTERS SETTERS/////
//	
//	public DraggableTreeCmsElement getRoot() {
//		if (root == null) {
//			Map<String, String> rootDataMap = new HashMap<String, String>();
//			rootDataMap.put("id", "generated-page-root");
//			root = new DraggableTreeCmsElement(null, rootDataMap);
//		}
//		return root;
//	}
//	
//	public DraggableTreeModel getModel() {
//		
//		if (model == null) {
//			model = new DraggableTreeModel(getRoot());
//		}
//		return model;
//	}
//	
//	public void setSelectedElement(DraggableTreeCmsElement selectedElement) {
//		this.selectedElement = selectedElement;
//	}
//	
//	public DraggableTreeCmsElement getSelectedElement() {
//		return selectedElement;
//	}
//
//	
//	public List<String>  getFragmentTypeList() {
//		if (fragmentTypeList == null) {
//			fragmentTypeList = new ArrayList<String>();
//			for (FragmentType i : FragmentType.values()) {
//				fragmentTypeList.add(i.toString());
//			}
//		}
//		return fragmentTypeList;
//	}
//	
//	public String getSelectedFragmentType() {
//		return selectedFragmentType;
//	}
//	
//	@NotifyChange("selectedFragmentTypeZul")
//	public void setSelectedFragmentType(String selectedFragmentType) {
//		this.selectedFragmentType = selectedFragmentType;
//	}
//	
//	//NOTE: zul templates must be in /WEB-INF/zul_templates dir and their name must be *all* lowercase
//	//TODO: manage to remove the "all lowercase" restriction for zul template files
//	public String getSelectedFragmentTypeZul() { 	//NOTE chosen type -> get the zul file (different from below)
//		if (selectedFragmentType == null) {
//			return null;
//		}
//	
//		return "/WEB-INF/zul_templates/" + selectedFragmentType.toLowerCase() + ".zul";
//	}
//	
//	public String getSelectedElementTypeZul() {		//NOTE chosen element -> get the type -> get the zul file (different from above)
//		return "/WEB-INF/zul_templates/" + selectedElement.getElementDataMap().get("fragmentType").toLowerCase() + ".zul";
//	}
//	
//	
//	public String getSelectedPopup() {
//		return selectedPopup;
//	}
//
//	public void setSelectedPopup(String selectedPopup) {
//		this.selectedPopup = selectedPopup;
//	}
//	
//	public String getPopupType() {
//		return popupType;
//	}
//
//	public void setPopupType(String popupType) {
//		this.popupType = popupType;
//	}
//	
//	
//	/////STARTUP/////
//	
//	@AfterCompose
//	public void startup() throws Exception {
//	    
//	    //initialize PageManipulator
//		String mainPagePath = WebApps.getCurrent().getServletContext().getRealPath("generated-page.html");	//uses ZK to resolve the path to the mainpage
//																											//NOTE the main page file must be inside the root dir of the webapp
//		/* DEBUG 
//		 * System.out.println("**DEBUG** ZK returned mainPagePath: " + mainPagePath); 
//		 */
//		pageManip = new PageManipulator(new File(mainPagePath), true, true);	//the last arg is to activate debug output
//	
//	    //initialize FragmentGenerator
//		String templatesFolderName = "velocity_templates";	//NOTE the folder for the templates must be inside WEB-INF
//		fragmentGen = new FragmentGenerator(templatesFolderName);
//	    
//	}
//	
//	
//	/////POPUP WINDOWS/////
//	
//	@Command
//	@NotifyChange({"selectedPopup","popupType"})
//	public void openPopup(@BindingParam("popupType") String popupType) {
//		selectedPopup = "/WEB-INF/" + "popup_" + popupType + ".zul";	//ex.  add -> /WEB-INF/popup_add.zul
//		this.popupType = popupType;
//	}
//	
//	@Command
//	@NotifyChange({"selectedPopup", "popupType"})
//	public void closePopup() {
//		selectedPopup = null;
//		popupType = null;
//		forceIframeRefresh();	//TODO delete this from here?
//	}
//		
////	create new component and attach it to DOM
////	Executions.createComponents("/WEB-INF/zul_templates/title.zul", null,null);		
//
//	/////TREE OPERATIONS/////
//	
//	@GlobalCommand
//	@NotifyChange({"model","selectedFragmentType","selectedPopup", "popupType"})
//	public void addElementGlobal(@BindingParam("pipeHashMap") Map<String, String> pipeHashMap) throws Exception {
//		System.out.println("**DEBUG** addElementGlobal received pipeHashMap: " + pipeHashMap);
//		
//		/*create new node and save it into draggableTree*/
//		DraggableTreeCmsElement newDraggableTreeCmsElement =  new DraggableTreeCmsElement(selectedElement, pipeHashMap);	//NOTE the pipeHashMap is COPIED into the new element
//		Map<String, String> newElDataMap = newDraggableTreeCmsElement.getElementDataMap();	//just saved for later brevity
//		newElDataMap.put("parentId", selectedElement.getElementDataMap().get("id"));
//		newElDataMap.put("siblingsPosition", ( (Integer)selectedElement.getChilds().indexOf(newDraggableTreeCmsElement) ).toString());	//can't call .toString on primitive type int, so I use Integer
//	
//		/*create new DOM node and write it to output page*/
//		//generate fragment code with Velocity
//		String newFragmentHtml = fragmentGen.generateFragmentHtml(FragmentType.valueOf(newElDataMap.get("fragmentType")), newElDataMap);
//		//rebuild the output page with the new fragment
//		pageManip.addFragment(newFragmentHtml, newElDataMap.get("parentId"), Integer.parseInt(newElDataMap.get("siblingsPosition")));																
//		
//		forceIframeRefresh();
//		closePopup();
//		selectedFragmentType = null;	//reset to show empty values to the next add
//	}
//	
//	@Command
//	@NotifyChange({"model","selectedPopup","popupType"})
//	public void removeElement() throws Exception {
//		//remove from output page
//		pageManip.removeFragment(selectedElement.getElementDataMap().get("id"));
//		forceIframeRefresh();
//		
//		//remove from draggableTree
//		DraggableTreeComponent.removeFromParent(selectedElement);
//		root.recomputeSpacersRecursive();
//		
//		closePopup();
//	}
//	
//	@GlobalCommand
//	@NotifyChange({"model","selectedPopup","popupType"})
//	public void modifyElementGlobal(@BindingParam("pipeHashMap") Map<String, String> pipeHashMap) throws Exception {
//		System.out.println("**DEBUG** modifyElementGlobal received pipeHashMap: " + pipeHashMap);
//		
//		String oldId = selectedElement.getDescription();	//NOTE the description field of DraggableTreeElement is the one shown in the tree, so we previously set it to equal the id
//		
//		//update draggableTree
//		selectedElement.setDescription(pipeHashMap.get("id"));	//we now set the description with the *updated* id
//		selectedElement.setElementDataMap(pipeHashMap);
//		
//		//update DOM e html page
//		String newFragmentHtml = fragmentGen.generateFragmentHtml(FragmentType.valueOf(pipeHashMap.get("fragmentType")), pipeHashMap);
//		pageManip.updateFragment(newFragmentHtml, oldId);
//
//		forceIframeRefresh();
//		closePopup();
//	}
//	
//	//TODO ***************  moveElement()  ************************
//	
//	
//	/////UTILITIES/////
//	private void forceIframeRefresh() {		
//		//force iframe refresh (using client-side js)
//		Clients.evalJavaScript("document.getElementsByTagName(\"iframe\")[0].contentWindow.location.reload(true);");	//see: https://stackoverflow.com/questions/13477451/can-i-force-a-hard-refresh-on-an-iframe-with-javascript?lq=1
//		System.out.println("**DEBUG** Forced Iframe refresh.");
//	}
//	
//}




///////////////////////////////////////////////////////////////////////////////////////////////////////////
//////COMMANDS		
//@Command
//@NotifyChange("*")
//public void deleteNode(){
//DraggableTreeComponent.removeFromParent(selectedElement);
//root.recomputeSpacersRecursive();
//}

//@NotifyChange("*")
//public void addComponent() throws Exception{
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////// CHECK DATA
//String errString=null;

//System.out.println( fragmentMap.get(selectedFragment));
//System.out.println( fragmentMap.get(selectedFragment).toString());

//errString=checkFields(idList, selectedFragment, attributeDataMap, fragmentMap.get(selectedFragment));
//if ((errString.equals(""))==true) {
//
//Clients.showNotification("Tutto OK");	
//
////attributeDataMap=generateFragment();
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////// INITIALIZING COMPONENT ELEMENTS WITH MAIN PAGE ELEMENTS
////componentIdList=mainPageIdList;
////componentSelectedElement = mainPageselectedElement;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////// NODE ADDING
////new DraggableTreeCmsElement(componentSelectedElement, fragmentId, selectedFragment, attributeDataMap);
////componentIdList.add(fragmentId);
////Map<String, Object> args = new HashMap<String, Object>();
////args.put("selectedElement", componentSelectedElement);
////args.put("idList", componentIdList);
////BindUtils.postGlobalCommand(null, null, "reloadMainPageTree", args);
////// RESET WINDOW SELECTIONS OR CONTENT
////resetPopUpSelectionAndBack();
//}else {
//addPopupVisibility=true;
//Clients.showNotification(errString);	
//}
//}

//@Command
//@NotifyChange("attributeDataMap")
//public void resetHashMap() {
//for(String currentKey:attributeDataMap.values()) {
//attributeDataMap.put(currentKey, "");
//}


//}

//@Command
//@NotifyChange("*")
//public void resetPopUpSelectionAndBack() {
//resetHashMap();
//addPopupVisibility=false;
//}

