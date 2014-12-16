package priorityQueue.tests;

import priorityQueue.GrainedLockSprayListPriorityQueue;
import priorityQueue.IPriorityQueue;
import priorityQueue.NaiveLockNativePriorityQueue;
import priorityQueue.NaiveLockSprayListPriorityQueue;
import priorityQueue.TransactioalMemorySprayListPriorityQueue;

public class maintest {

	public static void main(String[] args) {
		
		testBench2();
//		// TODO Auto-generated method stub
//		IPriorityQueue pq;
//		int res;
//		pq = new TransactioalMemorySprayListPriorityQueue(5);
//		pq = new GrainedLockSprayListPriorityQueue(5);
//		pq = new NaiveLockSprayListPriorityQueue(5);
//		pq = new NaiveLockNativePriorityQueue();
//		
//		pq.insert(25);
//		pq.insert(18);
//		pq.insert(2);
//		pq.insert(104);
//		pq.insert(5);
//		pq.insert(1005);
//		while(!pq.isEmpty())
//		{
//			res = pq.deleteMin();
//			System.out.println("got " + res);
//		}
	}
  public static void testBench() {
	  
	  StopWatch timer = new StopWatch();
	  
//    PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
//    PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
//    PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);


    
	  int numWorkers = 5;
    
    
	  IPriorityQueue queue = new NaiveLockNativePriorityQueue();
    
   
	  InsertWorker[] insertWorkers = new  InsertWorker[numWorkers]; 
	  Thread[] insertWorkerThreads = new Thread[numWorkers];
    
	  for(int i=0;i<numWorkers; i++)
	  {
		  insertWorkers[i] = new InsertWorker(queue, 100*i ,100);
    	  insertWorkerThreads[i] = new Thread(insertWorkers[i]);
      }
    
	  DeleteWorker[] deleteWorkers = new  DeleteWorker[numWorkers]; 
	  Thread[] deleteWorkerThreads = new Thread[numWorkers];

	  for(int i=0;i<numWorkers; i++)
	  {
		  deleteWorkers[i] = new DeleteWorker(queue);
		  deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
	  }


	  for(int i=0;i<numWorkers;i++)
	  {
		  insertWorkerThreads[i].start();
		  deleteWorkerThreads[i].start();
	  }



	  timer.startTimer();

	  //    try {
	  //         Thread.sleep(numMilliseconds);
	  //    } catch (InterruptedException ignore) {;}


	  for(int i=0;i<numWorkers;i++)
	  {
		  try {
			  insertWorkerThreads[i].join();
			  deleteWorkerThreads[i].join();
		  } catch (InterruptedException ignore) {;}
	  }

	  timer.stopTimer();
	  // Output the statistics

	  System.out.println("time: " + timer.getElapsedTime());
  }


  public static void testBench2() {

	  StopWatch timer = new StopWatch();

	  //	    PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
	  //	    PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
	  //	    PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

	  int numWorkers = 5;

	  IPriorityQueue queue = new NaiveLockNativePriorityQueue();

	  InsertWorker[] insertWorkers = new InsertWorker[numWorkers]; 
	  Thread[] insertWorkerThreads = new Thread[numWorkers];

	  for(int i=0; i < numWorkers; i++)
	  {
		  insertWorkers[i] = new InsertWorker(queue, 100*i ,100);
		  insertWorkerThreads[i] = new Thread(insertWorkers[i]);
	  }

	  DeleteWorker[] deleteWorkers = new  DeleteWorker[numWorkers]; 
	  Thread[] deleteWorkerThreads = new Thread[numWorkers];

	  for(int i=0;i<numWorkers; i++)
	  {
		  deleteWorkers[i] = new DeleteWorker(queue);
		  deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
	  }

	  for(int i=0;i<numWorkers;i++)
	  {
		  insertWorkerThreads[i].start();
	  }

	  for(int i=0;i<numWorkers;i++)
	  {
		  deleteWorkerThreads[i].start();
	  }

	  timer.startTimer();

	  //	    try {
		  //	      Thread.sleep(numMilliseconds);
	  //	    } catch (InterruptedException ignore) {;}

	  for(int i=0;i<numWorkers;i++)
	  {
		  try {
			  insertWorkerThreads[i].join();
		  } catch (InterruptedException ignore) {;}
	  }
	  
	  for(int i=0;i<numWorkers;i++)
	  {
		  try {
			  deleteWorkerThreads[i].join();
		  } catch (InterruptedException ignore) {;}
	  }

	  timer.stopTimer();

	  // Output the statistics
	  System.out.println("time: " + timer.getElapsedTime());
  }
}


