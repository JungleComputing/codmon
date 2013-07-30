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
import org.tmatesoft.svn.core.SVNException;
import org.eclipse.jgit.api.errors.*;
 
//TODO -Log policy
//     -Revission nr of application to be testedi
//     -git
public class Checkout{
	//private String basePath = "../../testApplications/";
	private String basePath = "../../";
	

	/**
 	*@author bvl300
 	*Updates a projects logfile with its current repository information
 	*TODO Implement better Log policy
 	**/ 
	private void writeLog(Collection repInfo,File f,SVN svnRep) throws IOException{
	        //first clean log and writ new revision number
		PrintWriter writer = new PrintWriter(f);
                writer.print("");
                writer.append(""+svnRep.getRev()+"\n\n");
		svnRep.writeLog(repInfo,f,writer);
		writer.close();
	}

	
	/**
	*@author bvl300
	*This method updates a log file
	*TODO check withe instanceof
	* */ 
	private void updateLog(SVN svnRep,File f)throws SVNException, IOException{
		Collection logCollection = null;
		//if("svn".equals(type)){ 
                	logCollection = svnRep.getSvnInfo();
                //}else if("git".equals(type)){
                        //TODO  
               // }
		writeLog(logCollection,f,svnRep);          
	}


	/**
 	*@author bvl300
	*This method checks if it is necessary to update the logFile and if so
	*It calls the update method.
 	*TODO make suitable for all kind of Logs
	*TODO dubbke check if it works also when file exists
 	* */ 
	private void checkLog(SVN svnRep)throws SVNException{
		String fileName = svnRep.getProject() +"Log.txt";
		File f = new File(basePath+"/"+fileName);
		try{
			f.createNewFile();
			FileReader fr = new FileReader(f);
                	LineNumberReader ln = new LineNumberReader(fr);
                	String s = ln.readLine();
			if(s==null || !s.equals(""+svnRep.getRev())){
				updateLog(svnRep,f);
			}
		}catch(IOException e){
			 System.out.println(e.getMessage());
		}
	}

	
	/**
 	*@author bvl300
 	*Checks out a specifc project and creates or updates the logfile
 	*/ 
	private void checkoutProject(Node project) throws SVNException, NoFilepatternException,GitAPIException,WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, NoHeadException,IOException {
		String url, type, projectName,user,pwd,command;
		long rev= -1;
		if (project.getNodeType() == Node.ELEMENT_NODE) {
 			Element eElement = (Element) project;
 
			url = eElement.getElementsByTagName("location").item(0).getTextContent();
			type = eElement.getElementsByTagName("type").item(0).getTextContent();
			command = eElement.getElementsByTagName("command").item(0).getTextContent();
 			projectName = eElement.getElementsByTagName("name").item(0).getTextContent();
			user = eElement.getElementsByTagName("user").item(0).getTextContent();
                        pwd = eElement.getElementsByTagName("pwd").item(0).getTextContent();
				
			if(type.equals("svn")){
				SVN svnRep = new SVN(basePath, projectName, user,pwd,url);
				if("checkout".equals(command)){
				 	svnRep.checkout();
				}
				checkLog(svnRep);
				//updateLog(projectName,rev,type,SVNUrl,user,pwd);

			}else if(type.equals("git")){
				GitObject gitRep = new GitObject(basePath,projectName,url,user,pwd);
				if("clone".equals(command)){
					gitRep.update();
				}
			}else{
		//		throw SVNException("test");
			}
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
			File initFile = new File("init.xml");
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
