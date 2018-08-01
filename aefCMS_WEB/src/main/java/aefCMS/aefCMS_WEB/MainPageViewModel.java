package aefCMS.aefCMS_WEB;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.bind.annotation.NotifyChange;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import aefCMS.aefCMS_WEB.HtmlRenderer;
import aefCMS.aefCMS_WEB.Library;
import aefCMS.aefCMS_WEB.PageElement;
import aefCMS.aefCMS_WEB.PageTree;
import aefCMS.aefCMS_WEB.PageTreeSerializer;
import biz.opengate.zkComponents.draggableTree.*;
import biz.opengate.zkComponents.draggableTree.DraggableTreeElement.DraggableTreeElementType;

public class MainPageViewModel {

	private DraggableTreeElement root;
	private DraggableTreeModel model;
	private DraggableTreeElement selectedElement;
	private String selectedFragment;
	private Map<String, String> attributeDataMap;
	private Map<String, HashMap<String, Boolean>> fragmentMap;
	private Library library; 
	private List<String> libraryElementList;
	private String selectedLibraryElement;
	private String selectedLibraryElementZul;
	
	private String selectedPopup;
	private String popupType;
	
	private String treePath;
	
	@Init
	@NotifyChange("*")
	public void init() throws Exception {

		attributeDataMap = new HashMap<String, String>();
		
		final String CONTEXT_PATH = WebApps.getCurrent().getServletContext().getRealPath("/");	//TODO change this (v. github issues)
		final String LIBRARY_PATH = 	  CONTEXT_PATH + "WEB-INF/cms_library";
		final String SAVE_PAGETREE_PATH =  CONTEXT_PATH + "WEB-INF/saved_pagetree/pageTree.json";
		
		try {
			PageTree loadedTree = PageTreeSerializer.loadTreeFromDisc(SAVE_PAGETREE_PATH, LIBRARY_PATH);
			System.out.println("LOADED TREE: ");
			loadedTree.print();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Non saved root");
			
			//page
			library= new Library(new File(LIBRARY_PATH));
			Map<String, String> pageAttributes = new HashMap<String, String>();
			pageAttributes.put("title", "My Page");
			PageElement page = new PageElement(library.getElement("stdPage"), pageAttributes);
			root= new DraggableTreeElement(null, "stdPage");
			PageTree exampleTree = new PageTree(page);
			exampleTree.print();
			
			//GENERATE OUTPUT HTML
			
			HtmlRenderer r = new HtmlRenderer(LIBRARY_PATH);
			System.out.println(r.render(page));
						
			//SAVE TREE 
//			PageTreeSerializer.saveTreeToDisc(SAVE_PAGETREE_PATH, exampleTree);
//			System.out.println("Saved default tree root");
		}
		
	}

//	///////////////////////////////////////////////////////////////////////////////////////////////////////////
//	////// COMMANDS
	
	/////POPUP WINDOWS/////
	
	@Command
	@NotifyChange({"selectedPopup","popupType"})
	public void openPopup(@BindingParam("popupType") String popupType) {
		selectedPopup = "/WEB-INF/" + "popup_" + popupType + ".zul";	//ex.  add -> /WEB-INF/popup_add.zul
		this.popupType = popupType;
	}
	
	@Command
	@NotifyChange({"selectedPopup", "popupType"})
	public void closePopup() {
		selectedPopup = null;
		popupType = null;
		forceIframeRefresh();	//TODO delete this from here?
	}
	
	
	
	
	
	
	
	
	
//	@Command
//	@NotifyChange("*")
//	public void deleteNode() throws Exception {
//		//pageManip.removeFragment(selectedElement.getTreeAttributeDataMap().get("id"));
//		//forceIframeRefresh();
//		//forceIframeRefresh();
//		DraggableTreeComponent.removeFromParent(selectedElement);
//		root.recomputeSpacersRecursive();
//		removeChildrenId(selectedElement);
//		saveTreeToDisc();
//		selectedElement = null;
//	}
//
//	@Command
//	@NotifyChange("*")
//	public void addComponent() throws Exception {
//		////// CHECK DATA
//		String errString = null;
//		errString = checkFields(idList, selectedFragment.toString(), attributeDataMap, fragmentMap.get(selectedFragment.toString()));
//
//		if ((errString.equals("")) == true) {
//			String output = currentFragmentgenerator.generateFragment(selectedFragment.toString(),attributeDataMap);
//			System.out.println(output);
//			// ADDING A NODE
//			attributeDataMap.put("fragmentType",selectedFragment);
//			DraggableTreeCmsElement newNode = new DraggableTreeCmsElement(selectedElement, attributeDataMap.get("id"),attributeDataMap);
//			// NEW NAME FOR THE SAME HASHMAP
//			Map<String, String> newElDataMap = newNode.getTreeAttributeDataMap();	//just saved for later brevity
//			newElDataMap.put("parentId", selectedElement.getTreeAttributeDataMap().get("id"));
//			newElDataMap.put("siblingsPosition", ( (Integer)selectedElement.getChildren().indexOf(newNode) ).toString());	//can't call .toString on primitive type int, so I use Integer
//			// ADDING ELEMENT TO HTML
//			System.out.println("ID " + newElDataMap.get("parentId"));
//			//pageManip.addFragment(output, newElDataMap.get("parentId"), Integer.parseInt(newElDataMap.get("siblingsPosition")));																
//			root.recomputeSpacersRecursive();
//			idList.add(attributeDataMap.get("id"));
//			// RESET WINDOW SELECTIONS OR CONTENT
//			saveTreeToDisc();
//			forceIframeRefresh(); 
//			resetAndBack();
//		} else {
//			addPopupVisibility = true;
//			Clients.showNotification(errString);
//		}
//	}
//	
//	@Command
//	@NotifyChange("*")
//	public void updateComponent() throws Exception {
//		////// CHECK DATA
//		System.out.println(selectedElement.getTreeAttributeDataMap());
//		String errString = null;
//		errString = checkFields(idList, selectedElement.getTreeAttributeDataMap().get("fragmentType"), attributeDataMap, 
//								fragmentMap.get(selectedElement.getTreeAttributeDataMap().get("fragmentType")));
//		
//		if (errString.equals("")) {
//
//			if (attributeDataMap.get("id").equals("") || (attributeDataMap.get("id") == null)) {
//				modifyPopupVisibility = true;
//				Clients.showNotification("Node Id can not be empty. Reloded previous value.");
//			} else {
//				String output = currentFragmentgenerator.generateFragment(selectedElement.getTreeAttributeDataMap().get("fragmentType"),attributeDataMap);
//				System.out.println(output);
//				///////////////////////////////////////////////////////////////////////////////////////////////////////////
//				////// NODE REMOVAL
//				idList.remove(selectedElement.getTreeAttributeDataMap().get("id"));
//				selectedElement.setTreeAttributeDataMap(attributeDataMap);
//				selectedElement.setDescription(attributeDataMap.get("id"));
//				idList.add(attributeDataMap.get("id"));
//				// RESET WINDOW SELECTIONS OR CONTENT
//				resetAndBack();
//			}
//		} else {
//			modifyPopupVisibility = true;
//			Clients.showNotification(errString);
//		}
//	}
//
//	@Command
//	@NotifyChange("*")
//	public void openPopUp(@BindingParam("popUpType") String popUpType){
//		if (popUpType.equals("add")) {
//			addPopupVisibility = true;
//		}
//		if (popUpType.equals("modify")){
//			if(selectedElement.getDescription().equals(root.getDescription())){
//			renamePopupVisibility = true;
//			} else {
//			resetHashMap(selectedElement.getTreeAttributeDataMap().get("fragmentType"));
//			modifyPopupVisibility = true;
//			}
//		}
//	}
//
//	@Command
//	@NotifyChange("attributeDataMap")
//	public void resetHashMap(@BindingParam("selectedFragment") String loadedFragment) {
//		System.out.println(selectedElement.getTreeAttributeDataMap());
//		// TAKING THE CONTROL HASHMAP AS REFERERENCE TO POPULATE THE DEFAULT EMPTY
//		// HASHMAP
//		if (loadedFragment==null) {
//			for (String currentKey : fragmentMap.get(selectedFragment.toString()).keySet()) {
//				attributeDataMap.put(currentKey, "");
//			}
//		}
//		else {
//			for (String currentKey : fragmentMap.get(loadedFragment).keySet()) {
//				attributeDataMap.put(currentKey, "");
//			}
//		}
//	}
//
//	@Command
//	@NotifyChange("*")
//	public void renameProject() throws Exception {
//		if (attributeDataMap.get("id") == "" || attributeDataMap.get("id") == null) {
//			Clients.showNotification("Project name empty. Fill the textbox.");
//		} else {
//			idList.remove(root.getDescription());
//			root.setDescription(attributeDataMap.get("id"));
//			idList.add(attributeDataMap.get("id"));
//			// SAVE TREE AND ID LIST TO FILE
//			saveTreeToDisc();
//			// RESET WINDOW SELECTIONS OR CONTENT
//			resetAndBack();
//		}
//	}
//
//	@Command
//	@NotifyChange("*")
//	public void resetAndBack() {
//		if (addPopupVisibility==true) {
//			addPopupVisibility = false;	
//			resetHashMap(selectedElement.getTreeAttributeDataMap().get("fragmentType"));
//		}
//		if (modifyPopupVisibility==true){
//			modifyPopupVisibility = false;
//			resetHashMap(selectedElement.getTreeAttributeDataMap().get("fragmentType"));
//		}
//		if (renamePopupVisibility==true){
//			renamePopupVisibility = false;
//		}
//		selectedFragment = null;
//	}
//
//	@Command
//	@NotifyChange("colorAttribute")
//	public void saveColor(@BindingParam("colorAttribute") String color) {
//		attributeDataMap.put("colorAttribute", color);
//	}
//	///////////////////////////////////////////////////////////////////////////////////////////////////////////
//	////// UTILITIES

//	////// CHECK IF ALL THE MANDATORY FIELDS ARE NOT EMPTY
//	private String checkFields(ArrayList<String> idList, String selectedType, Map<String, String> attributeMap,
//			HashMap<String, Boolean> controlComponentMap) {
//		String errMsgFun = "";
//
//		int controlKeyPosition = 0;
//		int controlCheckPosition = 0;
//
//		// CYCLE ON CONTROL MAP ID'S
//		for (String currentCheckName : controlComponentMap.keySet()) {
//			// SETTING CURRENT POSITION
//			controlCheckPosition = 0;
//			// CYCLE ON MAP BOOLEANS
//			for (Boolean currentCheckBool : controlComponentMap.values()) {
//				// ONLY ON THE DIAGONAL OF THE MATRIX
//				if (controlKeyPosition == controlCheckPosition) {
//					System.out.println(currentCheckName + " con controllo obbligatorio " + currentCheckBool);
//
//					if (currentCheckBool && (attributeMap.get(currentCheckName) == ""
//							|| attributeMap.get(currentCheckName) == null)) {
//						errMsgFun += currentCheckName;
//						errMsgFun += " \n";
//					}
//				}
//				controlCheckPosition++;
//			}
//			controlKeyPosition++;
//		}
//
//		if ((errMsgFun.equals("")) == false) {
//			errMsgFun += " empty. Please insert all the mandatory data. \n";
//		}
//
//		if (idList.contains(attributeMap.get("id"))) {
//			errMsgFun += "Node Id already exists. Please change it";
//		}
//		System.out.println(errMsgFun);
//		return errMsgFun;
//	}

	////// REMOVE CHILDREN ID
	public void removeChildrenId(DraggableTreeElement currentElement) {
		
		if (currentElement.getChildren().size() > 0) {
			int listSize = currentElement.getChildren().size();
			List<DraggableTreeElement> currentList = currentElement.getChildren();

			for (int i = 0; i < listSize; i++) {
				removeChildrenId(currentList.get(i));
			}
			System.out.println(currentElement.getChildren());
		}
	}
	
//	////// SAVE TREE	
//	private void saveTreeToDisc() throws Exception {
//		Gson gson = new Gson();
//		Writer treeWriterUpdate = new FileWriter(getTreePath());
//		try {
//			gson.toJson(root, treeWriterUpdate);
//			treeWriterUpdate.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
//	//////LOAD SAVED TREE	
//	private void reloadTree(DraggableTreeElement currentElement, JsonObject currentJsonObject, Boolean iAmRoot) throws Exception{
//		// ELEMENTS TO LOAD
//		Map<String, String> loadedMap = new HashMap<String, String>();
//		DraggableTreeElementType treeElementType=null;
//		String loadedDescription = null;
//		JsonArray currentChilds = new JsonArray();
//		Boolean loadedBoolean=null;
//		// LOADING ELEMENTS ONE BY ONE
//		Gson gson = new Gson();
//		// CURRENT ATTRIBUTE MAP 
//		loadedMap = gson.fromJson(currentJsonObject.getAsJsonObject("treeAttributeDataMap"), Map.class);
//		// ELEMENT TYPE 
//		treeElementType = gson.fromJson(currentJsonObject.getAsJsonObject().getAsJsonPrimitive("type"), DraggableTreeElementType.class);
//		// CHILDREN LIST 
//		currentChilds = currentJsonObject.getAsJsonArray("children");
//		// DESCRIPTION 
//		loadedDescription=gson.fromJson(currentJsonObject.get("description"), String.class);
//		// TREE ELEMENT OPEN 
//		loadedBoolean = gson.fromJson(currentJsonObject.get("treeElementOpen"), Boolean.class);
//		
//		if (iAmRoot) {
//			currentElement.setDescription(loadedDescription); 
//			currentElement.setTreeAttributeDataMap(loadedMap);
//			
//			if (currentChilds.size()>0) {
//				Iterator<JsonElement> localChildren = currentChilds.iterator();
//				
//				while (localChildren.hasNext()) {
//					reloadTree(currentElement, localChildren.next().getAsJsonObject(), false);
//				}
//			}	
//		}
//		else {
//			if(treeElementType.equals(DraggableTreeElementType.NORMAL)) {
//				DraggableTreeCmsElement localNode = new DraggableTreeCmsElement(currentElement, loadedDescription, loadedMap);		
//				idList.add(loadedMap.get("id"));
//				root.recomputeSpacersRecursive();	
//
//				if (currentChilds.size()>0) {
//					Iterator<JsonElement> localChildren = currentChilds.iterator();
//					while (localChildren.hasNext()) {
//						reloadTree(localNode,localChildren.next().getAsJsonObject() ,false);
//					}				
//				}	
//			}
//		}	
//	}
	
	// IFRAME REFRESH
	private void forceIframeRefresh() {
		Clients.evalJavaScript("document.getElementsByTagName(\"iframe\")[0].contentWindow.location.reload(true);");	//see: https://stackoverflow.com/questions/13477451/can-i-force-a-hard-refresh-on-an-iframe-with-javascript?lq=1
		System.out.println("**DEBUG** Forced Iframe refresh.");
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	////// GETTERS AND SETTERS

	@NotifyChange("selectedLibraryElementZul")
	public void setSelectedLibraryElement(String selectedlibraryElement) {
		this.selectedLibraryElement = selectedlibraryElement;
	}
	
	public String getSelectedLibraryElement() {
		return selectedLibraryElement;
	}

	//NOTE: zul templates must be in /WEB-INF/cms_library/ dir and their name must be *all* lowercase
	//TODO: manage to remove the "all lowercase" restriction for zul template files
	public String getSelectedLibraryElementZul() { 	//NOTE chosen type -> get the zul file (different from below)
		if (selectedLibraryElement == null) {
			return null;
		}
	
		return "/WEB-INF/cms_library/" + selectedLibraryElement + "/mask.zul";
	}
	
	public List<String> getLibraryElementList() throws IOException {
		
		Iterator<LibraryElement> libraryIterator = library.getElements().iterator();
		List <String> libraryElementList = new ArrayList<String>();
		while (libraryIterator.hasNext()) {
			libraryElementList.add(libraryIterator.next().getName());
		}
		libraryElementList.sort(null);
		return libraryElementList;
	}

	public String getSelectedPopup() {
		return selectedPopup;
	}

	public void setSelectedPopup(String selectedPopup) {
		this.selectedPopup = selectedPopup;
	}
	
	public DraggableTreeModel getModel() {

		if (model == null) {
			model = new DraggableTreeModel(root);
		}
		return model;
	}

	public void setSelectedElement(DraggableTreeElement selectedElement) {
		this.selectedElement = selectedElement;
	}

	public DraggableTreeElement getSelectedElement() {
		return selectedElement;
	}

	public DraggableTreeElement getRoot() {
		return root;
	}

	public void setRoot(DraggableTreeElement root) {
		this.root = root;
	}

	public String getSelectedFragment() {
		return selectedFragment;
	}

	public void setSelectedFragment(String selectedFragment) {
		this.selectedFragment = selectedFragment;
	}

	public Map<String, String> getAttributeDataMap() {
		return attributeDataMap;
	}

	public void setAttributeDataMap(Map<String, String> attributeDataMap) {
		this.attributeDataMap = attributeDataMap;
	}

	public String getTreePath() {

		if (treePath == "" || treePath == null)
			treePath = System.getProperty("user.home")
					+ "/git/CMSProject/zkVelocityLayout/src/main/webapp/resources/savedRoot.json";
		;

		return treePath;
	}

}
