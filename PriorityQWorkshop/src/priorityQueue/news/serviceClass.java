package priorityQueue.news;

import java.util.concurrent.ThreadLocalRandom;

public class serviceClass {
	
	public static int randomStep(int max) {
		return ThreadLocalRandom.current().nextInt(max+1);
	}
	
	public static  int randomLevel(int maxAllowedHeight) {	
		int x = ThreadLocalRandom.current().nextInt(1, (int) Math.pow(2, maxAllowedHeight +1) + 1);
		/* we want x to be a random number that uniformly distributed in the range {1,...,2^(maxAllowedHeight+1)}.
		 * we can see that there are 2^(maxAllowedHeight+1) numbers in this range.
		 * For the level we choose we want it to be between 0 to maxAllowedHeight (including) such that we will get:
		 * 0 w.p ~ 0.5
		 * 1 w.p ~ 0.25
		 * 2 w.p ~ 0.125
		 * ...
		 * i w.p ~ (0.5)^(maxAllowedHeight + 1)
		 */
		
		int n = (int) Math.floor(Math.log(x)/Math.log(2)); //this is floor of log2(x) => n is between {0,...,maxAllowedHeight+1}
		if (n > maxAllowedHeight ) {
			/* This is  the asymmetric case  - occurs when x = 2^(maxAllowedHeight+1) */
			n = maxAllowedHeight;
		}
		
		/*We have to reverse it */
		return (maxAllowedHeight - n);
	}
}
