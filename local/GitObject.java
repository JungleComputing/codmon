/**
 *@Author bvl300
 *Class for executing operations on a GIT-repository
 *
 * */
import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;


public class GitObject{

	private String user;
        private String pwd;	
	private String url;
	private String project;
	private String basePath;
	//private Repository localRepo;	
	//private File repositoryFolder;

	/**
 	*@author bvl300
	*Constructor creates a new Git object
 	*/ 	
	public GitObject(String basePath, String project, String url,String user,String pwd) throws IOException , GitAPIException{
		this.basePath = basePath;
		this.project = project;
		this.url = url;
		this.user =user;
		this.pwd =pwd;
	}


	/**
 	*@author bvl300
 	*Update a Git repository, when it's not cloned yest it clones the
	* repository.
 	* */	 
	public void update() throws IOException, NoFilepatternException,GitAPIException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, NoHeadException{
		String path = basePath+project;
		File f = new File(path);	
		if(!f.isDirectory()){ 
			cloneRepo();
		}else{
			pullRepo();
		}
	}



	public void updateLog(){
		String fileName = project +"Log.txt";
                File f = new File(basePath+"/"+fileName);
		Iterable<RevCommit> commits = getCommits();

	}

	

	private Iterable<RevCommit> getCommits(){
	
	return null;
	}

	
	/**
 	*@author bvl300
 	*Clones a Git repository
 	* */
	private void cloneRepo() throws NoFilepatternException,GitAPIException{
		CloneCommand cmd =  Git.cloneRepository();
		CredentialsProvider cp = new UsernamePasswordCredentialsProvider(user, pwd);
		cmd.setURI(url);
                cmd.setDirectory(new File(basePath+project));
		cmd.setCredentialsProvider(cp);
                cmd.call();
	}

	
	/**
 	*@author bvl300
 	*Updates a git repository by executing the git pull command
 	* */ 
	private void pullRepo() throws  IOException,GitAPIException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, NoHeadException{
		File repositoryFolder = new File(basePath+project+"/.git");
                Repository localRepo = new  FileRepositoryBuilder().setGitDir(repositoryFolder).build();
		Git  git = new Git(localRepo);
		CredentialsProvider cp = new UsernamePasswordCredentialsProvider(user, pwd);
		PullCommand cmd = git.pull();
		cmd.setCredentialsProvider(cp);
		cmd.call();    	
	}
                
}
