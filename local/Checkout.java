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


	private void updateSVNLog(String projectName, Long rev){


	}

	private void updateLog(String projectName, long rev, String type){
		if("svn".equals(type)){
			updateSVNLog(projectName,rev);
			
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
			
			if(type.equals("svn")){
				 rev = svnCheckOut(url,projectName,user,pwd);
			}else if(type.equals("git")){
				//TODO git check out
			}else{
		//		throw SVNException("test");
			}
			updateLog(projectName,rev,type);
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
