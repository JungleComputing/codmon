import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.*;
import org.eclipse.jgit.api.errors.*;
 
public class Checkout{
	private String basePath = "../../../";           

	/**
 	*@author bvl300
	*This method checks if it is necessary to update the logFile and if so
	*It calls the update method.
 	* */ 
	private boolean checkOldLog(String projectName,long rev){
		String fileName = projectName +"Log.txt";
		File f = new File(basePath+"/"+projectName+"/"+fileName);
		try{
			f.createNewFile();
			FileReader fr = new FileReader(f);
                	LineNumberReader ln = new LineNumberReader(fr);
                	String s = ln.readLine();
			if(s==null || !s.equals(""+rev)){
				return true;
			}
			return false;
		}catch(IOException e){
			 System.out.println(e.getMessage());
			 return false;
		}
	}

	
	/**
 	*@author bvl300
 	*Checks what versioncontrol system is used and
	*Checks out a specifc project and creates or updates the logfile
 	*/ 
	private void checkoutProject(Node project) throws VersionControlException{
		String url, type, projectName,user,pwd,command;
		if (project.getNodeType() == Node.ELEMENT_NODE) {
 			Element eElement = (Element) project;
 
			url = eElement.getElementsByTagName("location").item(0).getTextContent();
			type = eElement.getElementsByTagName("type").item(0).getTextContent();
			command = eElement.getElementsByTagName("command").item(0).getTextContent();
 			projectName = eElement.getElementsByTagName("name").item(0).getTextContent();
			user = eElement.getElementsByTagName("user").item(0).getTextContent();
                        pwd = eElement.getElementsByTagName("pwd").item(0).getTextContent();	
			if(type.equals("svn")){
				VersionControl svnRep = new SVN(basePath, projectName, user,pwd,url,command);
				fetch(svnRep,projectName);				
			}else if(type.equals("git")){			
				VersionControl gitRep = new GitObject(basePath,projectName,url,user,pwd);
				fetch(gitRep,projectName);				
			}else{
				throw new VersionControlException("Version control system not found");
			}
		}	
	}

	
	private void fetch(VersionControl vc,String projectName) throws VersionControlException{
		long rev =-1;
		vc.update();
		try{	
			rev = vc.getRev();
		}catch(MethodNotSupportedException e){
			System.out.println(e.getMessage());
		}
		if(checkOldLog(projectName,rev)){
			vc.updateLog();
		}
	}


	/**
 	*@author bvl300
 	*This method controls the different steps that are needed to checkout the test projects
	*First it reads the init.xml file, which contains information about the test projects, second
	*it checks out the projects one by one.
 	*/
	public Checkout () {
		try{
			File initFile = new File("../../local/checkoutApplications/init.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(initFile);
 			
			doc.getDocumentElement().normalize();

			NodeList projectList = doc.getElementsByTagName("project");

			for (int i = 0; i < projectList.getLength(); i++) {
				Node project = projectList.item(i);
				checkoutProject(project);
			}
		} catch (Exception e) {
			e.printStackTrace();
    		}
	}


	/**
 	*@author bvl300
	*Main function to check out the test projects
 	*/	 	
	public static void main(String argv[]){
		new Checkout();
	}

}
