import java.io.File;


/**
 *@author bvl300
 *programm is used to start the codmon programm
 * */
public class Stats{
       	File dday, dday1,dday2,dday3;


	/**
 	*@author bvl300
 	*Copies the the data from dday to dday1 etc etc
 	* */
	private void copyData(){

	}


	/**
	 *@author bvl300
	 *Creates the differs directories where the results are stored.
	 **/ 
	private void createDdayDirectories(){
		dday = new File("dday");
		if(!dday.exists()){dday.mkdir();}

		dday1 = new File("dday1");
     		if(!dday1.exists()){dday1.mkdir();}

		dday2 = new File("dday2");
		if(!dday2.exists()){dday2.mkdir();}

		dday3 = new File("dday3");
		if(!dday3.exists()){dday3.mkdir();}
	}


	private void loadProperties(){

	}

	/**
 	*@author bvl300
 	*initilezes the program. creates and copy dday files to the right directories
 	*/ 	 	
	private void init(){
		loadProperties();
		createDdayDirectories();
		copyData();
	}


	private void run(String sensor){
		
		
	}

	
	public Stats(String sensor){
		init();
		run(sensor);		
	}



	/**
 	*@author bvl300
 	*Checks the paramters and if correct, starts a new stats programm
 	* */ 	 
	public static void main(String argv[]){
		if(argv.length ==1){
			new Stats(argv[0]);
		}else{
			System.out.println("Stats expects a sensor as a paramater");
		}
	}
}
