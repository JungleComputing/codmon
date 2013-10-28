
/**
 *@author bvl300
 *This wrapper messures the time of the module
 *that is executed.
 */
public class TimeWrapper{


	public TimeWrapper(String argv[]){
                String dir = argv[0];
                String cmd = argv[1];
                String target;
		if(argv.length==3){
			target = argv[2];
		}else{
			target = "default";
		}
                long startTime,duration;
		
		System.out.println("--------------------> "+target);
                Ant ant = new Ant(dir,target);
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
