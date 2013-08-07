/**
 *@author Bvl300
 *Class for exucuting operations on SVN-repositories
 */
import java.util.*;
import java.io.File;
import java.io.PrintWriter;
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
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil; 

public class SVN{
	private String pwd;
	private SVNURL svnUrl;
	private String url;
	private String command;
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
	public SVN(String basePath, String project, String user,String pwd, String url,String command ){
		this.project = project;
		this.basePath = basePath;
		this.user = user;
		this.pwd = pwd;
		this.url =url;
		this.command = command;
	}


        /**
        *Checks out a svn repository 
        *@author bvl300 
        *@Return svn revision number
        */
	public void update()throws SVNException{
		File dstPath = new File(basePath+project);
                SVNClientManager cm = SVNClientManager.newInstance(null,user,pwd);
                SVNUpdateClient updateClient = cm.getUpdateClient();
                updateClient.setIgnoreExternals(false);
                svnUrl = SVNURL.parseURIEncoded(url);
		if("checkout".equals(command)){
			rev =  updateClient.doCheckout(svnUrl, dstPath, SVNRevision.UNDEFINED,SVNRevision.HEAD,SVNDepth.INFINITY,true);
		}else if ("export".equals(command)){
			rev =  updateClient.doExport(svnUrl, dstPath, SVNRevision.UNDEFINED,SVNRevision.HEAD,"\n",true, SVNDepth.INFINITY);
		}
	}

	/**
 	*@author bvl300
	*@return returns A collection that contains the svnInfo and Log from a SVN repository
 	*
 	* */	 
	public Collection getSvnInfo()throws SVNException{
		SVNRepository repository = SVNRepositoryFactory.create(svnUrl);
                ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(user, pwd);
                repository.setAuthenticationManager(authManager);

                return repository.log( new String[] { "" } , null , 0 , -1 , true , true );
	}


	/**
 	*@author bvl300
 	*Method writes all the svn info formated to the logFile
 	* */
	public void writeLog(Collection logEntries,File f,PrintWriter writer){
                 for (Iterator entries = logEntries.iterator(); entries.hasNext(); ) {
                        SVNLogEntry logEntry = (SVNLogEntry) entries.next();
                        writer.println("----------------------------------------------");
                        writer.println ("revision: " + logEntry.getRevision( ) );
                        writer.println( "author: " + logEntry.getAuthor( ) );
                        writer.println( "date: " + logEntry.getDate( ) );
                        writer.println( "log message: " + logEntry.getMessage( ) );

                        if ( logEntry.getChangedPaths( ).size( ) > 0 ) {
                                writer.println( );
                                writer.println( "changed paths:" );
                                Set changedPathsSet = logEntry.getChangedPaths( ).keySet( );

                                        for ( Iterator changedPaths = changedPathsSet.iterator( ); changedPaths.hasNext( ); ) {
                                                SVNLogEntryPath entryPath = ( SVNLogEntryPath ) logEntry.getChangedPaths( ).get( changedPaths.next( ) );
                                                writer.println( " "
                                                + entryPath.getType( )
                                                + " "
                                                + entryPath.getPath( )
                                                + ( ( entryPath.getCopyPath( ) != null ) ? " (from "
                                                + entryPath.getCopyPath( ) + " revision "
                                                + entryPath.getCopyRevision( ) + ")" : "" ) );
                                        }
                        }
                }
        }


	//---------------------Getters and Setters---------------------/
	

	public long getRev(){
		return this.rev;
	}


	public String getProject(){
		return this.project;
	}
} 
