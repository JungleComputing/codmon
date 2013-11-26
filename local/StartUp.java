import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.net.MalformedURLException;
import java.lang.SecurityException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 *@author bvl300
 *programm is used to start the codmon programm
 * */
public class StartUp{


	/**
 	*@author bvl300
	*Moves files from source directory  to the  target directory
 	**/ 	
	private void move(File source, File target){
		File[] fileList = source.listFiles();
		for(int i = 0; i < fileList.length; i++) {
    			fileList[i].renameTo(new File(target.getName()+"/"+fileList[i].getName())); 
     		} 
	}


	/**
 	*@author bvl300
 	*Copies the the data from dday to dday1 etc etc
 	* */
	private void copyData(File[] directories){
		File last = directories[directories.length-1];
		//Verwijder dday3
		directories[directories.length-1] = null;
		//kopieer de mappen
		(new File("../dday2")).renameTo(new File("../dday3"));
        	(new File("../dday1")).renameTo(new File("../dday2"));
       		(new File("../dday")).renameTo(new File("../dday1"));
		//maak dday1 opnieuw aan
		File dday = new File("../dday");
		if(!dday.exists()){dday.mkdir();}
		/*for(int i =directories.length-2;i>=0;i--){	
				move(directories[i],directories[i+1]);					
		}*/		
	}


	/**
 	*@author bvl300
 	*Creates the Files that must be available befor the programm runs
 	* */ 
	/*private void createBasicFiles(){
		try{
			PrintWriter writer = new PrintWriter("../dday1/allin1.xml");
			writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			writer.println("<?xml-stylesheet type=\"text/xsl\" href=\"../libs/merge.xml\"?>");
			writer.println("<xml/>");
			writer.close();
		}catch(FileNotFoundException e){
			System.out.println(e.getMessage());
		}
	}*/


	/**
	 *@author bvl300
	 *Creates the differs directories where the results are stored.
	 **/ 
	private File[] createDdayDirectories(int numberDirs){
		File[] directories = new File[numberDirs];
		for(int i=0;i<directories.length;i++){
			if(i ==0) {
				directories[i] = new File("../dday");
			}
			else{
				directories[i] = new File("../dday"+i);
			}
			if(!directories[i].exists()){directories[i].mkdir();}

		}	
		return directories;
	}


	
	private String[] getJars(){
		String[] jars = new String[1];
                jars[0] = "../build/codmon.jar";
		return jars;
	}
	
	
	/**
 	*@author bvl300
	*Returns Classloader that contains the necessary jars
	**/	 	
	private ClassLoader getClassLoader(String[] jars) throws MalformedURLException, SecurityException{
		ArrayList<URL> paths = new ArrayList<URL>();

     		for (String externalJar : jars) {
         		paths.add(new File(externalJar).toURI().toURL());
			System.out.println(paths.get(0));
     		}
     		
		URL[] urls = paths.toArray(new URL[paths.size()]);
		return new URLClassLoader(urls);
	}	

	/**
	 *@author bvl300
	 *Loads codmon.jar so I can Use it here
	 **/
	private Method getStartMethod(String[] argv){	
		Method m = null;
		Class<?> cl = null;
		String[] jars = getJars();;
		try{
		        ClassLoader loader = getClassLoader(jars);
			cl = loader.loadClass("Stats");
			m = cl.getMethod("main", new Class[] { argv.getClass() });
		}catch(Exception e){
			System.out.println(e.getMessage());
		}

		if(!m.isAccessible()){
			final Method temporary_method = m;
			AccessController.doPrivileged(new PrivilegedAction<Object>() {
            			 public Object run() {
               		  		temporary_method.setAccessible(true);
                 			return null;
             			}
         		});
		}
		return m;
	} 


	/**
 	*@author bvl300
 	*initilezes the program. creates and copy dday files to the right directories
 	*/ 	 	
	private Method init(String[] argv){
		File[] directories = createDdayDirectories(4);
	//	createBasicFiles();
		copyData(directories);
		return getStartMethod(argv);
	}


	private void run(Method m,String[] argv){
		String sensor = argv[0];
		String[] statsArgs = new String[2];
		statsArgs[0] = "../sensors-"+sensor+".xml";
		statsArgs[1] = "../dday/shot-"+sensor+".xml";
		try{
			m.invoke(null,new Object[]{statsArgs});
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	
	public StartUp(String[] argv){ 
		Method startMethod = init(argv);
		run(startMethod,argv);		
	}



	/**
 	*@author bvl300
 	*Checks the paramters and if correct, starts a new stats programm
 	* */ 	 
	public static void main(String argv[]){
		if(argv.length ==1){
			new StartUp(argv);
		}else{
			System.out.println("Stats expects a sensor as a paramater");
		}
	}
}
