package priorityQueue.tests;

import priorityQueue.news.GlobalLockSprayListPriorityQueue;
import priorityQueue.news.IPriorityQueue;
import priorityQueue.news.SeqSprayListPriorityQueue;
import priorityQueue.news.TMSprayListPriorityQueue;
import priorityQueue.utils.*;


public class maintest {

	public static void main(String[] args) {

		
		IPriorityQueue pq = new GlobalLockSprayListPriorityQueue(5);
//		IPriorityQueue pq = new SeqSprayListPriorityQueue(5);
//		IPriorityQueue pq = new TMSprayListPriorityQueue(5);

		//Insert & Delete min simultaneously
//		testBench(pq);
		
		//Insert & Delete min simultaneously 
//		testBench2(pq);
		
		//Insert all and then delete all
//		testBench3(pq);
		
		//Insert in an interleaving manner, DeleteMin simultaneously, using boolean flag to stop
//		testBench4(pq);
		
		simpleTest(pq);
	}
	private static void simpleTest(IPriorityQueue pq) {
		int res;
		pq.insert(25);
		pq.insert(18);
		pq.insert(2);
		pq.insert(104);
		pq.insert(5);
		pq.insert(1005);
		pq.insert(1);
		
		while(!pq.isEmpty())
		{
			res = pq.deleteMin();
			System.out.println("got " + res);
		}

	}
	
	public static void testBench(IPriorityQueue queue) {

		StopWatch timer = new StopWatch();

		int numWorkers = 5;

		SimpleInsertWorker[] insertWorkers = new  SimpleInsertWorker[numWorkers]; 
		Thread[] insertWorkerThreads = new Thread[numWorkers];

		for(int i=0;i<numWorkers; i++)
		{
			insertWorkers[i] = new SimpleInsertWorker(queue, 100*i ,100);
			insertWorkerThreads[i] = new Thread(insertWorkers[i]);
		}

		SimpleDeleteWorker[] deleteWorkers = new  SimpleDeleteWorker[numWorkers]; 
		Thread[] deleteWorkerThreads = new Thread[numWorkers];

		for(int i=0;i<numWorkers; i++)
		{
			deleteWorkers[i] = new SimpleDeleteWorker(queue);
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


	public static void testBench2(IPriorityQueue queue) {

		StopWatch timer = new StopWatch();

		int numWorkers = 5;

		SimpleInsertWorker[] insertWorkers = new SimpleInsertWorker[numWorkers]; 
		Thread[] insertWorkerThreads = new Thread[numWorkers];

		for(int i=0; i < numWorkers; i++)
		{
			insertWorkers[i] = new SimpleInsertWorker(queue, 100*i ,100);
			insertWorkerThreads[i] = new Thread(insertWorkers[i]);
		}

		SimpleDeleteWorker[] deleteWorkers = new  SimpleDeleteWorker[numWorkers]; 
		Thread[] deleteWorkerThreads = new Thread[numWorkers];

		for(int i=0;i<numWorkers; i++)
		{
			deleteWorkers[i] = new SimpleDeleteWorker(queue);
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

	public static void testBench3(IPriorityQueue queue) {

		StopWatch timer = new StopWatch();

		int numWorkers = 5;

		SimpleInsertWorker[] insertWorkers = new  SimpleInsertWorker[numWorkers]; 
		Thread[] insertWorkerThreads = new Thread[numWorkers];

		for(int i=0;i<numWorkers; i++)
		{
			insertWorkers[i] = new SimpleInsertWorker(queue, 100*i ,100);
			insertWorkerThreads[i] = new Thread(insertWorkers[i]);
		}

		SimpleDeleteWorker[] deleteWorkers = new  SimpleDeleteWorker[numWorkers]; 
		Thread[] deleteWorkerThreads = new Thread[numWorkers];

		for(int i=0;i<numWorkers; i++)
		{
			deleteWorkers[i] = new SimpleDeleteWorker(queue);
			deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
		}


		for(int i=0;i<numWorkers;i++)
		{
			insertWorkerThreads[i].start();
		}



		timer.startTimer();

		//    try {
			//         Thread.sleep(numMilliseconds);
		//    } catch (InterruptedException ignore) {;}


		for(int i=0;i<numWorkers;i++)
		{
			try {
				insertWorkerThreads[i].join();
			} catch (InterruptedException ignore) {;}
		}

		for(int i=0;i<numWorkers;i++)
		{
			deleteWorkerThreads[i].start();
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

	private static class StepGenerator implements INumberGenerator
	{
		final int _step;
		int _seed;
		
		public StepGenerator(int seed, int step)
		{
			_step = step;
			_seed = seed;
		}
		
		@Override
		public int getNext() {
			int tmp = _seed;
			_seed += _step;
			return tmp;
		}
	}
	
	/**
	 * All workers run for a constant amount of time, interleaved values inserted
	 */
	public static void testBench4(IPriorityQueue queue)
	{
		StopWatch timer = new StopWatch();

		int numWorkers = 5;
		int numMilliseconds = 200;

		// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
		PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
		PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
		PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

		AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[numWorkers]; 
		Thread[] insertWorkerThreads = new Thread[numWorkers];

		for(int i=0;i<numWorkers; i++)
		{
			// Initialize insert workers with interleaving number generators
			insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new StepGenerator(i,  numWorkers), queue);
			insertWorkerThreads[i] = new Thread(insertWorkers[i]);
		}

		AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[numWorkers]; 
		Thread[] deleteWorkerThreads = new Thread[numWorkers];

		for(int i=0;i<numWorkers; i++)
		{
			deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, queue);
			deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
		}

		
		// Start all the workers
		for(int i=0;i<numWorkers;i++)
		{
			deleteWorkerThreads[i].start();
		}
		
		timer.startTimer();

		for(int i=0;i<numWorkers;i++)
		{
			insertWorkerThreads[i].start();
		}


		try {
			Thread.sleep(numMilliseconds);
		} catch (InterruptedException ignore) {;}

		
		// stop insert workers
		doneDispatcher.value = true;
		memFence.value = true; // memFence is a 'volatile' forcing a memory fence
							   // which means that done.value is visible to the workers
		
		for(int i=0;i<numWorkers;i++)
		{
			try {
				insertWorkerThreads[i].join();
			} catch (InterruptedException ignore) {;}
		}

		// Stop delete Workers - they are responsible for leaving the queue empty
		doneWorkers.value = true;
		memFence.value = true; // memFence is a 'volatile' forcing a memory fence
							   // which means that done.value is visible to the workers

		for(int i=0;i<numWorkers;i++)
		{
			try {
				deleteWorkerThreads[i].join();
			} catch (InterruptedException ignore) {;}
		}
		
		timer.stopTimer();
		// Output the statistics

		long totalCount = 0;
		for(int i=0;i<numWorkers;i++)
		{
			totalCount+= deleteWorkers[i]._totalPackets;
		}
		
		System.out.println("count: " + totalCount);
		System.out.println("time: " + timer.getElapsedTime());
		System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
	}
}


