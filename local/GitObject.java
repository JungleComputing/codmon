/**
 *@Author bvl300
 *Class for executing operations on a GIT-repository
 *
 * */
import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.GitAPIException;

public class GitObject{
	
	private String url;
	private String project;
	private String basePath;	

	/**
 	*@author bvl300
	*Constructor creates a new Git object
 	*/ 	
	public GitObject(String basePath, String project, String url){
		this.basePath = basePath+"/"+project;
		this.project = project;
		this.url = url;
	}


	/**
 	*@author bvl300
 	*Clones a git object's git  repository
 	* */	 
	public void cloneRepo() throws NoFilepatternException,GitAPIException{
		Git.cloneRepository()
		.setURI(url)
		.setDirectory(new File(basePath))
           	.call();  
	}

}
