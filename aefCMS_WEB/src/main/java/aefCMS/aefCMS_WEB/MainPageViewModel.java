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
import biz.opengate.zkComponents.draggableTree.*;
import biz.opengate.zkComponents.draggableTree.DraggableTreeElement.DraggableTreeElementType;

public class MainPageViewModel {

	private DraggableTreeElement root;
	private DraggableTreeModel model;
	private DraggableTreeElement selectedElement;
	private String selectedFragment;
	private Map<String, String> attributeDataMap;
	private ArrayList<String> idList = new ArrayList<String>();
	private Map<String, HashMap<String, Boolean>> fragmentMap;
	private List<String> fragmentList;
	private boolean addPopupVisibility = false;
	private boolean modifyPopupVisibility = false;
	private boolean renamePopupVisibility = false;
	
	private String treePath;
	
	@Init
	@NotifyChange("*")
	public void init() throws Exception {

		attributeDataMap = new HashMap<String, String>();
	
		try {
			attributeDataMap.put("id", "root");
			root = new DraggableTreeElement(null, "root");
			// LOAD MAIN JSON OBJECT
			
			FileReader reader = new FileReader(getTreePath());
			JsonParser parser = new JsonParser();
			// MAIN	JSON OBJECT
			JsonObject draggableTreeCmsElement = (JsonObject) parser.parse(reader);
			reloadTree(root, draggableTreeCmsElement, true);
																														//NOTE the main page file must be inside the root dir of the webapp
			//DEBUG 
			 System.out.println("**DEBUG** ZK returned mainPagePath: " + mainPagePath); 			

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Non saved root");
			attributeDataMap.put("id", "generated-page-root");
			root = new DraggableTreeElement(null, "generated-page-root");
			saveTreeToDisc(); 
			System.out.println("Saved default tree root");
		}
		
		
	    //initialize FragmentGenerator
		String templatesFolderName = "templateFolder";	//NOTE the folder for the templates must be inside WEB-INF
		currentFragmentgenerator = new FragmentGenerator(templatesFolderName);
		System.out.println(templatesFolderName);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	////// COMMANDS
	@Command
	@NotifyChange("*")
	public void deleteNode() throws Exception {
		//pageManip.removeFragment(selectedElement.getTreeAttributeDataMap().get("id"));
		//forceIframeRefresh();
		//forceIframeRefresh();
		DraggableTreeComponent.removeFromParent(selectedElement);
		root.recomputeSpacersRecursive();
		removeChildrenId(selectedElement);
		saveTreeToDisc();
		selectedElement = null;
	}

	@Command
	@NotifyChange("*")
	public void addComponent() throws Exception {
		////// CHECK DATA
		String errString = null;
		errString = checkFields(idList, selectedFragment.toString(), attributeDataMap, fragmentMap.get(selectedFragment.toString()));

		if ((errString.equals("")) == true) {
			String output = currentFragmentgenerator.generateFragment(selectedFragment.toString(),attributeDataMap);
			System.out.println(output);
			// ADDING A NODE
			attributeDataMap.put("fragmentType",selectedFragment);
			DraggableTreeCmsElement newNode = new DraggableTreeCmsElement(selectedElement, attributeDataMap.get("id"),attributeDataMap);
			// NEW NAME FOR THE SAME HASHMAP
			Map<String, String> newElDataMap = newNode.getTreeAttributeDataMap();	//just saved for later brevity
			newElDataMap.put("parentId", selectedElement.getTreeAttributeDataMap().get("id"));
			newElDataMap.put("siblingsPosition", ( (Integer)selectedElement.getChildren().indexOf(newNode) ).toString());	//can't call .toString on primitive type int, so I use Integer
			// ADDING ELEMENT TO HTML
			System.out.println("ID " + newElDataMap.get("parentId"));
			//pageManip.addFragment(output, newElDataMap.get("parentId"), Integer.parseInt(newElDataMap.get("siblingsPosition")));																
			root.recomputeSpacersRecursive();
			idList.add(attributeDataMap.get("id"));
			// RESET WINDOW SELECTIONS OR CONTENT
			saveTreeToDisc();
			forceIframeRefresh(); 
			resetAndBack();
		} else {
			addPopupVisibility = true;
			Clients.showNotification(errString);
		}
	}
	
	@Command
	@NotifyChange("*")
	public void updateComponent() throws Exception {
		////// CHECK DATA
		System.out.println(selectedElement.getTreeAttributeDataMap());
		String errString = null;
		errString = checkFields(idList, selectedElement.getTreeAttributeDataMap().get("fragmentType"), attributeDataMap, 
								fragmentMap.get(selectedElement.getTreeAttributeDataMap().get("fragmentType")));
		
		if (errString.equals("")) {

			if (attributeDataMap.get("id").equals("") || (attributeDataMap.get("id") == null)) {
				modifyPopupVisibility = true;
				Clients.showNotification("Node Id can not be empty. Reloded previous value.");
			} else {
				String output = currentFragmentgenerator.generateFragment(selectedElement.getTreeAttributeDataMap().get("fragmentType"),attributeDataMap);
				System.out.println(output);
				///////////////////////////////////////////////////////////////////////////////////////////////////////////
				////// NODE REMOVAL
				idList.remove(selectedElement.getTreeAttributeDataMap().get("id"));
				selectedElement.setTreeAttributeDataMap(attributeDataMap);
				selectedElement.setDescription(attributeDataMap.get("id"));
				idList.add(attributeDataMap.get("id"));
				// RESET WINDOW SELECTIONS OR CONTENT
				resetAndBack();
			}
		} else {
			modifyPopupVisibility = true;
			Clients.showNotification(errString);
		}
	}

	@Command
	@NotifyChange("*")
	public void openPopUp(@BindingParam("popUpType") String popUpType){
		if (popUpType.equals("add")) {
			addPopupVisibility = true;
		}
		if (popUpType.equals("modify")){
			if(selectedElement.getDescription().equals(root.getDescription())){
			renamePopupVisibility = true;
			} else {
			resetHashMap(selectedElement.getTreeAttributeDataMap().get("fragmentType"));
			modifyPopupVisibility = true;
			}
		}
	}

	@Command
	@NotifyChange("attributeDataMap")
	public void resetHashMap(@BindingParam("selectedFragment") String loadedFragment) {
		System.out.println(selectedElement.getTreeAttributeDataMap());
		// TAKING THE CONTROL HASHMAP AS REFERERENCE TO POPULATE THE DEFAULT EMPTY
		// HASHMAP
		if (loadedFragment==null) {
			for (String currentKey : fragmentMap.get(selectedFragment.toString()).keySet()) {
				attributeDataMap.put(currentKey, "");
			}
		}
		else {
			for (String currentKey : fragmentMap.get(loadedFragment).keySet()) {
				attributeDataMap.put(currentKey, "");
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void renameProject() throws Exception {
		if (attributeDataMap.get("id") == "" || attributeDataMap.get("id") == null) {
			Clients.showNotification("Project name empty. Fill the textbox.");
		} else {
			idList.remove(root.getDescription());
			root.setDescription(attributeDataMap.get("id"));
			idList.add(attributeDataMap.get("id"));
			// SAVE TREE AND ID LIST TO FILE
			saveTreeToDisc();
			// RESET WINDOW SELECTIONS OR CONTENT
			resetAndBack();
		}
	}

	@Command
	@NotifyChange("*")
	public void resetAndBack() {
		if (addPopupVisibility==true) {
			addPopupVisibility = false;	
			resetHashMap(selectedElement.getTreeAttributeDataMap().get("fragmentType"));
		}
		if (modifyPopupVisibility==true){
			modifyPopupVisibility = false;
			resetHashMap(selectedElement.getTreeAttributeDataMap().get("fragmentType"));
		}
		if (renamePopupVisibility==true){
			renamePopupVisibility = false;
		}
		selectedFragment = null;
	}

	@Command
	@NotifyChange("colorAttribute")
	public void saveColor(@BindingParam("colorAttribute") String color) {
		attributeDataMap.put("colorAttribute", color);
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	////// UTILITIES

	////// CHECK IF ALL THE MANDATORY FIELDS ARE NOT EMPTY
	private String checkFields(ArrayList<String> idList, String selectedType, Map<String, String> attributeMap,
			HashMap<String, Boolean> controlComponentMap) {
		String errMsgFun = "";

		int controlKeyPosition = 0;
		int controlCheckPosition = 0;

		// CYCLE ON CONTROL MAP ID'S
		for (String currentCheckName : controlComponentMap.keySet()) {
			// SETTING CURRENT POSITION
			controlCheckPosition = 0;
			// CYCLE ON MAP BOOLEANS
			for (Boolean currentCheckBool : controlComponentMap.values()) {
				// ONLY ON THE DIAGONAL OF THE MATRIX
				if (controlKeyPosition == controlCheckPosition) {
					System.out.println(currentCheckName + " con controllo obbligatorio " + currentCheckBool);

					if (currentCheckBool && (attributeMap.get(currentCheckName) == ""
							|| attributeMap.get(currentCheckName) == null)) {
						errMsgFun += currentCheckName;
						errMsgFun += " \n";
					}
				}
				controlCheckPosition++;
			}
			controlKeyPosition++;
		}

		if ((errMsgFun.equals("")) == false) {
			errMsgFun += " empty. Please insert all the mandatory data. \n";
		}

		if (idList.contains(attributeMap.get("id")) && modifyPopupVisibility == false) {
			errMsgFun += "Node Id already exists. Please change it";
		}
		System.out.println(errMsgFun);
		return errMsgFun;
	}

	////// REMOVE CHILDREN ID
	public void removeChildrenId(DraggableTreeElement currentElement) {
		idList.remove(currentElement.getDescription());
		if (currentElement.getChildren().size() > 0) {
			int listSize = currentElement.getChildren().size();
			List<DraggableTreeElement> currentList = currentElement.getChildren();

			for (int i = 0; i < listSize; i++) {
				removeChildrenId(currentList.get(i));
			}
			System.out.println(currentElement.getChildren());
		}
	}
	
	////// SAVE TREE	
	private void saveTreeToDisc() throws Exception {
		Gson gson = new Gson();
		Writer treeWriterUpdate = new FileWriter(getTreePath());
		try {
			gson.toJson(root, treeWriterUpdate);
			treeWriterUpdate.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//////LOAD SAVED TREE	
	private void reloadTree(DraggableTreeCmsElement currentElement, JsonObject currentJsonObject, Boolean iAmRoot) throws Exception{
		// ELEMENTS TO LOAD
		Map<String, String> loadedMap = new HashMap<String, String>();
		DraggableTreeElementType treeElementType=null;
		String loadedDescription = null;
		JsonArray currentChilds = new JsonArray();
		Boolean loadedBoolean=null;
		// LOADING ELEMENTS ONE BY ONE
		Gson gson = new Gson();
		// CURRENT ATTRIBUTE MAP 
		loadedMap = gson.fromJson(currentJsonObject.getAsJsonObject("treeAttributeDataMap"), Map.class);
		// ELEMENT TYPE 
		treeElementType = gson.fromJson(currentJsonObject.getAsJsonObject().getAsJsonPrimitive("type"), DraggableTreeElementType.class);
		// CHILDREN LIST 
		currentChilds = currentJsonObject.getAsJsonArray("children");
		// DESCRIPTION 
		loadedDescription=gson.fromJson(currentJsonObject.get("description"), String.class);
		// TREE ELEMENT OPEN 
		loadedBoolean = gson.fromJson(currentJsonObject.get("treeElementOpen"), Boolean.class);
		
		if (iAmRoot) {
			currentElement.setDescription(loadedDescription); 
			currentElement.setTreeAttributeDataMap(loadedMap);
			
			if (currentChilds.size()>0) {
				Iterator<JsonElement> localChildren = currentChilds.iterator();
				
				while (localChildren.hasNext()) {
					reloadTree(currentElement, localChildren.next().getAsJsonObject(), false);
				}
			}	
		}
		else {
			if(treeElementType.equals(DraggableTreeElementType.NORMAL)) {
				DraggableTreeCmsElement localNode = new DraggableTreeCmsElement(currentElement, loadedDescription, loadedMap);		
				idList.add(loadedMap.get("id"));
				root.recomputeSpacersRecursive();	

				if (currentChilds.size()>0) {
					Iterator<JsonElement> localChildren = currentChilds.iterator();
					while (localChildren.hasNext()) {
						reloadTree(localNode,localChildren.next().getAsJsonObject() ,false);
					}				
				}	
			}
		}	
	}
	
	// IFRAME REFRESH
	private void forceIframeRefresh() {
		Clients.evalJavaScript("document.getElementsByTagName(\"iframe\")[0].contentWindow.location.reload(true);");	//see: https://stackoverflow.com/questions/13477451/can-i-force-a-hard-refresh-on-an-iframe-with-javascript?lq=1
		System.out.println("**DEBUG** Forced Iframe refresh.");
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	////// GETTERS AND SETTERS
	public List<String> getFragmentList() {
		return FragmentMap.getFragmentList();
	}

	public DraggableTreeModel getModel() {

		if (model == null) {
			model = new DraggableTreeModel(root);
		}
		return model;
	}

	public void setSelectedElement(DraggableTreeCmsElement selectedElement) {
		this.selectedElement = selectedElement;
	}

	public DraggableTreeCmsElement getSelectedElement() {
		return selectedElement;
	}

	public DraggableTreeElement getRoot() {
		return root;
	}

	public void setRoot(DraggableTreeCmsElement root) {
		this.root = root;
	}

	public ArrayList<String> getIdList() {
		return idList;
	}

	public void setIdList(ArrayList<String> idList) {
		this.idList = idList;
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

	public boolean isModifyPopupVisibility() {
		return modifyPopupVisibility;
	}

	public void setModifyPopupVisibility(boolean modifyPopupVisibility) {
		this.modifyPopupVisibility = modifyPopupVisibility;
	}

	public boolean isAddPopupVisibility() {
		return addPopupVisibility;
	}

	public void setAddPopupVisibility(boolean addPopupVisibility) {
		this.addPopupVisibility = addPopupVisibility;
	}

	public boolean isRenamePopupVisibility() {
		return renamePopupVisibility;
	}

	public void setRenamePopupVisibility(boolean renamePopupVisibility) {
		this.renamePopupVisibility = renamePopupVisibility;
	}

	public String getTreePath() {

		if (treePath == "" || treePath == null)
			treePath = System.getProperty("user.home")
					+ "/git/CMSProject/zkVelocityLayout/src/main/webapp/resources/savedRoot.json";
		;

		return treePath;
	}

}
