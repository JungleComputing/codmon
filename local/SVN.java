/**
 *@author Bvl300
 *Class for creating and manipulating subversionLogs
 */
import java.io.File;
import java.util.*;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;

public class SVN{
	private String pwd;
	private SVNURL url;
	private String user;
	private String project;
	private String basePath;;
	private long rev;
	
	/**
 	*@author bvl300
 	*@param  f File to write the change log
	*@param  user svn user name
	*@param  pwd password of the username
        *@param  command used make distinction between checkout and export
	*@param  url the url of the repository location
  	*@return A SVNLog object 
 	*/
	public SVN(String basePath, String project, String user,String pwd, String url ) throws SVNException{
		this.project = project;
		this.basePath = basePath;
		this.user = user;
		this.pwd = pwd;
		this.url= SVNURL.parseURIEncoded(url);
	}

	
	/**
 	*Checks out a svn repository 
	*@author bvl300	
	*@Return svn revision number
	*/ 	
	public void checkout() throws SVNException{
		File dstPath = new File(basePath+project);
		SVNClientManager cm = SVNClientManager.newInstance(null,user,pwd);
               	SVNUpdateClient updateClient = cm.getUpdateClient();
                updateClient.setIgnoreExternals(false);
                rev =  updateClient.doCheckout(url, dstPath, SVNRevision.UNDEFINED,SVNRevision.HEAD,SVNDepth.INFINITY,true);
	}


	/**
 	*@author bvl300
	*@return returns A collection that contains the svnInfo and Log from a SVN repository
 	*
 	* */	 
	public Collection getSvnInfo()throws SVNException{
		SVNRepository repository = SVNRepositoryFactory.create(url);
                ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(user, pwd);
                repository.setAuthenticationManager(authManager);

                return repository.log( new String[] { "" } , null , 0 , -1 , true , true );
	}


	//---------------------Getters and Setters---------------------/
	

	public long getRev(){
		return this.rev;
	}


	public String getProject(){
		return this.project;
	}
} 
