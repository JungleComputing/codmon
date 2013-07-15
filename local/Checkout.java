import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import java.io.IOException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;

//TODO sort out location of the programm, files and pathnames
public class Checkout{
	String basePath = "../../testApplications/";
		
	private long svnCheckOut(String url,String projectName,String user, String pwd) throws SVNException{
		File dstPath = new File(basePath+projectName);
		SVNURL SVNUrl = SVNURL.parseURIEncoded(url);
		SVNClientManager cm = SVNClientManager.newInstance(null,user,pwd); 
                SVNUpdateClient updateClient = cm.getUpdateClient();
		updateClient.setIgnoreExternals(false);
		System.out.println(SVNUrl);
		return updateClient.doCheckout(SVNUrl, dstPath, SVNRevision.UNDEFINED,SVNRevision.HEAD,SVNDepth.INFINITY,true);		
	}


	private void getSVNLogInfo(File f, long rev,SVNURL url)throws SVNException{
		SVNRepository repository = SVNRepositoryFactory.create(url);


	}

	/**
 	*Updates the log file only when the  new version number is not equal to the 
	*version number in the log.
 	*TODO:Implement better log policy
 	*/
	private void updateSVNLog(String projectName, Long rev, File f,SVNURL url)throws IOException,SVNException{
		FileReader fr = new FileReader(f);
		LineNumberReader ln = new LineNumberReader(fr);
		String s = ln.readLine();
              	if(s==null || !s.equals(""+rev)){
			//First clean the log file
			PrintWriter writer = new PrintWriter(f);
			writer.print("");
			writer.append(""+rev+"\n\n");
			//add the new log content
			getSVNLogInfo(f,rev, url);
			writer.close();
		}
	}


	private void updateLog(String projectName, long rev, String  type, SVNURL url)throws SVNException{
		String fileName = projectName +"Log.txt";
		File f = new File(basePath+"/"+fileName);
		try{
			f.createNewFile();
			if("svn".equals(type)){
                        	updateSVNLog(projectName,rev,f,url);
	                }

		}catch(IOException e){
			 System.out.println(e.getMessage());
		}
	}


	private void checkoutProject(Node project) throws SVNException{
		String url, type, projectName,user,pwd;
		long rev= -1;
		if (project.getNodeType() == Node.ELEMENT_NODE) {
 			Element eElement = (Element) project;
 
			url = eElement.getElementsByTagName("location").item(0).getTextContent();
			type = eElement.getElementsByTagName("versionControl").item(0).getTextContent();
 			projectName = eElement.getElementsByTagName("name").item(0).getTextContent();
			user = eElement.getElementsByTagName("user").item(0).getTextContent();
                        pwd = eElement.getElementsByTagName("pwd").item(0).getTextContent();
		
			//transfer url into SVNURL
			SVNURL SVNUrl = SVNURL.parseURIEncoded(url);
				
			if(type.equals("svn")){
				 rev = svnCheckOut(url,projectName,user,pwd);
				 updateLog(projectName,rev,type,SVNUrl);

			}else if(type.equals("git")){
				//TODO git check out
			}else{
		//		throw SVNException("test");
			}
		}	
	}


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

	
	public static void main(String argv[]){
		new Checkout();
	}

}
