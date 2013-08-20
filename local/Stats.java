/**
 *@author bvl300
 *programm is used to start the codmon programm
 * */
public class Stats{
	
	private static final String CODMON_HOME = "../../";
       
	
	public Stats(String sensor){

	}



	/**
 	*@author bvl300
 	*Checks the paramters and if correct, starts a new stats programm
 	* */ 	 
	public static void main(String argv[]){
		if(argv.length ==1){
			new Stats(argv[0]);
		}else{
			System.out.println("Stats expects 1 sensor as a paramater");
		}
	}
}
