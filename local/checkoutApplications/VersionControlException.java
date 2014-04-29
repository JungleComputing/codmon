/**
 *@author bvl300
 *Class to catch VersionControl Exceptions
 **/
public class VersionControlException extends Exception {
  public VersionControlException() { super(); }
  public VersionControlException(String message) { super(message); }
  public VersionControlException(String message, Throwable cause) { super(message, cause); }
  public VersionControlException(Throwable cause) { super(cause); }
}
