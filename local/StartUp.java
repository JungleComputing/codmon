import java.io.File;
import java.io.IOException;



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
		for(int i =0;i<directories.length;i++){	
				move(directories[i],directories[i+1]);					
		}		
	}


	/**
	 *@author bvl300
	 *Creates the differs directories where the results are stored.
	 **/ 
	private File[] createDdayDirectories(int numberDirs){
		File[] directories = new File[numberDirs];
		for(int i=0;i<=directories.length;i++){
			directories[i] = new File("dday"+i);
			if(!directories[i].exists()){directories[i].mkdir();}

		}	
		return directories;
	}


	private void loadProperties(){

	}

	/**
 	*@author bvl300
 	*initilezes the program. creates and copy dday files to the right directories
 	*/ 	 	
	private void init(){
		loadProperties();
		File[] directories = createDdayDirectories(4);
		copyData(directories);
	}


	private void run(String sensor){
		
		
	}

	
	public StartUp(String sensor){ 
		init();
		run(sensor);		
	}



	/**
 	*@author bvl300
 	*Checks the paramters and if correct, starts a new stats programm
 	* */ 	 
	public static void main(String argv[]){
		if(argv.length ==1){
			new StartUp(argv[0]);
		}else{
			System.out.println("Stats expects a sensor as a paramater");
		}
	}
}
