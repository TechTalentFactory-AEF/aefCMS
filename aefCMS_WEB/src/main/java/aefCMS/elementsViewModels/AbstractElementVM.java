package aefCMS.elementsViewModels;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;

public class AbstractElementVM {
	
	protected Map<String, String> pipeHashMap = new HashMap<String, String>();	//WARNING the same pipeHashMap is reused at every passage! 
																				//I solved this problem in the draggableTree element creation: the pipeHashMap gets cloned

	//GETTERS SETTERS
	
	public Map<String, String> getPipeHashMap() {
	return pipeHashMap;
	}
	
	public void setPipeHashMap(Map<String, String> pipeHashMap) {
	this.pipeHashMap = pipeHashMap;
	}
	
	//INITIALIZATION
	
	@Init
	public void init(@ExecutionArgParam("selectedPopupType") String selectedPopupType, @ExecutionArgParam("dataToLoad") HashMap<String, String> dataToLoad) {
		if (loadOldValues)	//VM was called by modify popup
			pipeHashMap = new HashMap<String, String>(dataToLoad);	//load the Element old attributes into the form;   NOTE we need a *copy* here!
	}
	
	//TREE OPERATIONS
	
	@GlobalCommand
	public void saveElement() {
		Map<String, Object> wrapperMap = new HashMap<String, Object>();
		wrapperMap.put("pipeHashMap", pipeHashMap);
		BindUtils.postGlobalCommand(null, null, (operationType + "ElementGlobal"), wrapperMap);		//NOTE the possible commands are "addElementGlobal" or "modifyElementGlobal"
}	
}
