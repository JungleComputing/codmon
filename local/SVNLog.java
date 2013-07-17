/**
 *@author Bvl300
 *Class for creating and manipulating subversionLogs
 */
import java.io.File;

public class SVNLog{
	private String pwd;
	private String url;
	private String user;
	private String command;
	private File f;

	/**
 	*@author bvl300
 	*@param  f File to write the change log
	*@param  user svn user name
	*@param  pwd password of the username
        *@param  command used make distinction between checkout and export
	*@param  url the url of the repository location
  	*@return A SVNLog object 
 	*/
	public SVNLog(File f,String user,String pwd,String command, String url ){
		this.f =f;
		this.user = user;
		this.pwd = pwd;
		this.command = command;
		this.url=url;
	}

} 
