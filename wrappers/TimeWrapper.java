
/**
 *@author bvl300
 *This wrapper messures the time of the module
 *that is executed.
 */
public class TimeWrapper{


	public TimeWrapper(String argv[]){
                String dir = argv[0];
                String cmd = argv[1];

                long startTime,duration;

                Ant ant = new Ant(dir);
                ant.init();

                startTime = System.nanoTime();
                ant.run();
                duration = System.nanoTime()-startTime;
	}


	/**
 	*@author bvl300
 	* */	
	public static void main(String argv[]){
		new TimeWrapper(argv);	
	}
 
}


