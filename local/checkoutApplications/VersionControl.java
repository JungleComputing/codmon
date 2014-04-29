
/**
@author bvl300 
Basic interface for version control modules
*/
public interface VersionControl{
	
		/**
		*Fetches a repository
		**/
		public void update() throws VersionControlException;
		
		/**
		*If there is no logfile it creates the logfile. 
		*If there is a logfile it updates the existing logfile
		**/
		public void updateLog() throws VersionControlException;
}
