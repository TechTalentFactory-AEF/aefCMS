package CMSPROT.CMSPROT_ZK_FragmentsGen_PageAsm;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;

public class ParagraphVM {
	private Map<String, String> pipeHashMap = new HashMap<String, String>();
	
	public Map<String, String> getPipeHashMap() {
		return pipeHashMap;
	}

	public void setPipeHashMap(Map<String, String> pipeHashMap) {
		this.pipeHashMap = pipeHashMap;
	}
	
	@AfterCompose
	public void initFragmentType() {
		pipeHashMap.put("fragmentType", "PARAGRAPH");
	}
	
	@Command
	public void saveElement() {
		Map<String, Object> wrapperMap = new HashMap<String, Object>();
		wrapperMap.put("pipeHashMap", pipeHashMap);
		BindUtils.postGlobalCommand(null, null, "addElementGlobal", wrapperMap);
	}
	
	//get draggableTree selected element data to fill the form in the "modify" popup
	@GlobalCommand
	@NotifyChange("pipeHashMap")
	public void getDataToFillPopupInner(@BindingParam("selectedElement") DraggableTreeCmsElement selectedElement) {
		//System.out.println("**DEBUG** EXECUTING GLOBAL COMMAND getDataToFillPopupInner");
		//System.out.println("**DEBUG** getDataToFillPopupInner received selectedElement: " + selectedElement);
		pipeHashMap = selectedElement.getElementDataMap();
	}
	
//	//NOTE: doesn't hide, but *detaches* the window from the DOM
//	@Command
//	public void detachPopup(@ContextParam(ContextType.VIEW) Window component) {
//		component.detach();
//	}
}
