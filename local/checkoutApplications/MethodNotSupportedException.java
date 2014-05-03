/**
 *@author bvl300
 *Exception when method is not implemented
 **/
public class MethodNotSupportedException extends Exception {
  public MethodNotSupportedException() { super(); }
  public MethodNotSupportedException(String message) { super(message); }
  public MethodNotSupportedException(String message, Throwable cause) { super(message, cause); }
  public MethodNotSupportedException(Throwable cause) { super(cause); }
}
