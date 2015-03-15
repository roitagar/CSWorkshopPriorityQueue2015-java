package priorityQueue.tests;

public class TestUtils {

	
	public static void saveResult(String[] result){
		// TODO: Change this to print to file? or just use output redirection
		StringBuffer buff = new StringBuffer();
		for(String s:result)
		{
			buff.append(s);
			buff.append("\t");
		}
		
		System.out.println(buff.toString());
	}
}
