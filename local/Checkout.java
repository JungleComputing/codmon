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
	
	/**
 	*TODO return current version number
 	*
 	*/ 	
	private int svnCheckOut(String url) throws SVNException{
		File dstPath = new File("testBerend");
		SVNURL SVNUrl = SVNURL.parseURIEncoded(url);
		SVNClientManager cm = SVNClientManager.newInstance(null,"bvl300","maYl1nda"); 
                SVNUpdateClient updateClient = cm.getUpdateClient();
		updateClient.setIgnoreExternals(false);
		System.out.println(SVNUrl);
		updateClient.doCheckout(SVNUrl, dstPath, SVNRevision.UNDEFINED,SVNRevision.HEAD,SVNDepth.INFINITY,true);	
	
		return 1;	
	}


	private void checkoutProject(Node project) throws SVNException{
		String url, type;
		if (project.getNodeType() == Node.ELEMENT_NODE) {
 			Element eElement = (Element) project;
 
			url = eElement.getElementsByTagName("location").item(0).getTextContent();
			type = eElement.getElementsByTagName("versionControl").item(0).getTextContent();
 			
			if(type.equals("svn")){
				int rev = svnCheckOut(url);
			}else if(type.equals("git")){
				//TODO git check out
			}else{
				//Throw versie betheer error
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
