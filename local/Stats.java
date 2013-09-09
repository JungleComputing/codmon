/**
 *@author bvl300
 *programm is used to start the codmon programm
 * */
public class Stats{
       
	
	private void run(String sensor){

	}

	
	public Stats(String sensor){
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
