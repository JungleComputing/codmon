/**
 *@Author bvl300
 *Class for executing operations on a GIT-repository
 *
 * */
import java.util.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;


public class GitObject implements VersionControl{

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
	public GitObject(String basePath, String project, String url,String user,String pwd){
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
	public void update() throws VersionControlException{
		String path = basePath+project;
		File f = new File(path);	
		try{
			if(!f.isDirectory()){ 
				cloneRepo();
			}else{
				pullRepo();
			}
                }catch(IOException e){
			throw new VersionControlException(e);
	        }catch(GitAPIException e){
			throw new VersionControlException(e);
		}
	}




	/**
 	*@author bvl300
	*Updates the log file from a git project
	*
	**/
	public void updateLog()throws VersionControlException{
		try{		
			Iterator<RevCommit> commits = getCommits().iterator();	
			writeLog(commits);
		}catch(IOException e){
			throw new VersionControlException(e);
	        }catch(GitAPIException e){
			throw new VersionControlException(e);
		}
	}

	

	/**
 	*@author bvl300
	*Fetches the Git llog
	**/
	private Iterable<RevCommit> getCommits()throws GitAPIException,IOException{
		File repositoryFolder = new File(basePath+project+"/.git");
                Repository localRepo = new  FileRepositoryBuilder().setGitDir(repositoryFolder).build();
		Git  git = new Git(localRepo);
		LogCommand cmd = git.log();
		return cmd.call();	
	}

	
	/**
	 *@author bvl300
	 *Writes the Logfile
	 * */
	private void writeLog(Iterator<RevCommit> commits)throws IOException{
		int i =0;
		String fileName = project +"/"+project+"Log.txt";
                File f = new File(basePath+"/"+fileName);
		PrintWriter writer = new PrintWriter(f);
                writer.print("");
		while(commits.hasNext() && i<10){
			writer.println("----------------------------------------------");
			RevCommit commit = commits.next();
			writer.println("Author :" + commit.getCommitterIdent().getName());
			writer.println( "Date: " + commit.getCommitterIdent().getWhen() );
			writer.print(commit.getFullMessage());
			i++;
		}
		writer.close();
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
	private void pullRepo() throws  IOException,GitAPIException{
		File repositoryFolder = new File(basePath+project+"/.git");
                Repository localRepo = new  FileRepositoryBuilder().setGitDir(repositoryFolder).build();
		Git  git = new Git(localRepo);
		CredentialsProvider cp = new UsernamePasswordCredentialsProvider(user, pwd);
		PullCommand cmd = git.pull();
		cmd.setCredentialsProvider(cp);
		cmd.call();    	
	}


	public long getRev()throws MethodNotSupportedException{
		throw new MethodNotSupportedException("This method is not supported for the Git implementation");
	}
                
}
