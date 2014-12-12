package priorityQueue.tests;

import priorityQueue.IPriorityQueue;
import priorityQueue.NaiveLockNativePriorityQueue;
import priorityQueue.NaiveLockSprayListPriorityQueue;

public class maintest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		IPriorityQueue pq;
		int res;
		pq = new NaiveLockSprayListPriorityQueue(5);
//		pq = new NaiveLockNativePriorityQueue();
		
		pq.insert(25);
		pq.insert(18);
		pq.insert(2);
		pq.insert(104);
		pq.insert(5);
		pq.insert(1005);
		while(!pq.isEmpty())
		{
			res = pq.deleteMin();
			System.out.println("got " + res);
		}
	}

}
