
/**
 *@author bvl300
 *This wrapper messures the time of the module
 *that is executed.
 */
public class TimeWrapper{


	/**
 	*@author bvl300
 	* */	
	public static void main(String argv[]){
		String dir = argv[0];
		String cmd = argv[1];
		long startTime,duration;
		Ant ant = new Ant(); 
		//TODO goto directory
		
		startTime = System.nanoTime();
	
		duration = System.nanoTime-startTime();
		
	}
 
}


