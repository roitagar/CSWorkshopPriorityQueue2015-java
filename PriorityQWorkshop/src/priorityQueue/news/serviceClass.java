package priorityQueue.news;

import java.util.concurrent.ThreadLocalRandom;

public class serviceClass {
	
	public static int randomStep(int max) {
		return ThreadLocalRandom.current().nextInt(max+1);
	}
	
	//TODO: Check if static if OK
	public static  int randomLevel(int maxAllowedHeight) {
		return randomStep(maxAllowedHeight);
	}
}
