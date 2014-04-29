
/**
@author bvl300 
Basic interface for version control modules
*/
public interface VersionControl{
	
		public void update() throws VersionControlException;
		
		public void updateLog() throws VersionControlException;
}
