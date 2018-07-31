package aefCMS.aefCMS_WEB;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {
	
	public static void main(String[] args) throws Exception {
			
			final String BASE_PATH = 			System.getProperty("user.home") + "/git/aefCMS/aefCMS";
			final String LIBRARY_PATH = 		BASE_PATH + "/cms_library";
			final String SAVE_PAGETREE_PATH =  BASE_PATH + "/saved_pagetree/pageTree.json";
			
			//GENERATE LIBRARY
			
			Library lib = new Library(new File(LIBRARY_PATH));
			
			//CREATE BOGUS PageTree
			
			//page
					
			Map<String, String> pageAttributes = new HashMap<String, String>();
			pageAttributes.put("title", "My Page");
			
			PageElement page = new PageElement(lib.getElement("page"), pageAttributes);
			
			//title
			
			Map<String, String> titleAttributes = new HashMap<String, String>();
			titleAttributes.put("h-text", "HELLO WORLD TITLE");
			
			PageElement title = new PageElement(lib.getElement("title"), titleAttributes);
			
			//paragraph
			
			Map<String, String> paragraphAttributes = new HashMap<String, String>();
			paragraphAttributes.put("p-text", "Hello World Paragraph");
			paragraphAttributes.put("p-text-color", "red");
			
			PageElement paragraph = new PageElement(lib.getElement("paragraph"), paragraphAttributes);
			
			//container
			
			Map<String, String> containerAttributes = new HashMap<String, String>();
			containerAttributes.put("border-style", "dotted");
			 
			PageElement container = new PageElement(lib.getElement("container"), containerAttributes);
			
			//paragraph inside container
			
			Map<String, String> paragraph2Attributes = new HashMap<String, String>();
			paragraph2Attributes.put("p-text", "Hello World Inside");
			paragraph2Attributes.put("p-text-color", "blue");
			
			PageElement paragraph2 = new PageElement(lib.getElement("paragraph"), paragraph2Attributes);
			
			//create the tree
			
			PageTree exampleTree = new PageTree(page);
			
			exampleTree.addElement(title, page);
			exampleTree.addElement(paragraph, page, 1);
			exampleTree.addElement(container, page);
			exampleTree.addElement(paragraph2, container);
			
			exampleTree.print();
			
			//GENERATE OUTPUT HTML
			
			HtmlRenderer r = new HtmlRenderer(LIBRARY_PATH);
			System.out.println(r.render(page));
			
			//SAVE TREE 
			PageTreeSerializer.saveTreeToDisc(SAVE_PAGETREE_PATH, exampleTree);
			
//			//LOAD TREE
//			PageTree loadedTree = PageTreeSerializer.loadTreeFromDisc(SAVE_PAGETREE_PATH);
//			System.out.println(loadedTree);

	}
}
	
