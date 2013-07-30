/**
 *@Author bvl300
 *Class for executing operations on a GIT-repository
 *
 * */
import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitObject{
	
	private String url;
	private String project;
	private String basePath;
	private Repository localRepo;	
	private File repositoryFolder;

	/**
 	*@author bvl300
	*Constructor creates a new Git object
 	*/ 	
	public GitObject(String basePath, String project, String url) throws IOException , GitAPIException{
		this.basePath = basePath+"/"+project;
		this.project = project;
		this.url = url;
		repositoryFolder = new File("basePath");
		localRepo = new  FileRepositoryBuilder().setGitDir(repositoryFolder).build();
	}


	/**
 	*@author bvl300
 	*Update a Git repository, when it's not cloned yest it clones the
	* repository.
 	* */	 
	public void update() throws IOException, NoFilepatternException,GitAPIException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, NoHeadException{
		File f = new File(basePath);
		if(!f.isDirectory()){ 
			cloneRepo();
		}else{
			pullRepo();
		}
	}

	
	/**
 	*@author bvl300
 	*Clones a Git repository
 	* */
	private void cloneRepo() throws NoFilepatternException,GitAPIException{
		 Git.cloneRepository()
                .setURI(url)
                .setDirectory(new File(basePath))
                .call();
	}

	
	private void pullRepo() throws  IOException,GitAPIException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, NoHeadException{
		Git  git = new Git(localRepo);
		git.pull().call();    	
	}
                
}
