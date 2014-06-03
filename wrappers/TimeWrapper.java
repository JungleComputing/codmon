import java.text.DecimalFormat;
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
			target = "main";
		}
                long startTime;
		double duration;
		
                Ant ant = new Ant(dir,target);
                ant.init();

                startTime = System.nanoTime();
                try{
			ant.run();
		}catch(Exception e){
			System.out.println(e+"\n<br/>\n");
			System.exit(-1);
		}finally{
                	duration = (double)((System.nanoTime()-startTime)/1000000000.0);
			DecimalFormat df = new DecimalFormat("#.##");
			System.out.println("<test id=\"time\" name=\"Time\" value=\""+df.format(duration)+"\" unit=\"s\"/>\n");
		}
	}


	/**
 	*@author bvl300
 	* */	
	public static void main(String argv[]){
		new TimeWrapper(argv);	
	}
 
}
