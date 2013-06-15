//Just a simple test programm
//TODO implement a program that just returns a simple value
//
public class ReturnValue {
	
	public static void returnValue(int i){
		System.out.println(""+i);
	}
	
	
	public static void main(String[] args){
		returnValue(Integer.parseInt(args[0]));
	}
}
						
