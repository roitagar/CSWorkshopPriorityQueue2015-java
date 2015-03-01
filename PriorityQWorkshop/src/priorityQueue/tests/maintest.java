package priorityQueue.tests;

import priorityQueue.news.*;
import priorityQueue.utils.*;


public class maintest {




	public static void main(String[] args) {

		final int skiplistHeight = 10;
		final int insertWorkerCount = 5;
		final int deleteWorkerCount = 5;
		IPriorityQueue pq = new NaiveLockNativePriorityQueue();
//		IPriorityQueue pq = new GlobalLockSprayListPriorityQueue(skiplistHeight);
//		IPriorityQueue pq = new SeqSprayListPriorityQueue(skiplistHeight);
//		IPriorityQueue pq = new TMSprayListPriorityQueue(skiplistHeight, true);
//		IPriorityQueue pq = new LockFreeSprayListPriorityQueue(skiplistHeight);
//		IPriorityQueue pq = new CoolSprayListPriorityQueue(skiplistHeight);


		//Insert & Delete min simultaneously
		//		testBench(pq);

		//Insert & Delete min simultaneously 
		//		testBench2(pq);

		//Insert all and then delete all
		//				testBench3(pq);

		//Insert in an interleaving manner, DeleteMin simultaneously, using boolean flag to stop
		//		testBench4(pq);


		//		testBench5(pq, 1, 10, 1000);
		//		testBench6(pq, 10, 10, 1000);
		//		testBench7(pq, 1, 10, 1000);
		//		testBench8(pq, 5, 5, 1000, 1000000);
		//		testBench9(pq, 5, 5, 100000);
		//		testBench10(pq, 5, 5, 100000);
		//		testBench11(pq, 5, 5, 700);
		//		testBench12(pq, 5, 5, 100000);
		//		testBench13(pq, 5, 5, 100000);
		//		testBench14(pq, 1, 10, 1000);
		//		testBench15(pq, 10, 10, 1000);
		//		testBench16(pq, 1, 10, 1000);
		//		testBench17(pq, 5, 5, 100000);



		TestBench tb = testBench13;
		tb.setQueue(pq);
		tb.setNumDeleteWorkers(deleteWorkerCount);
		tb.setNumInsertWorkers(insertWorkerCount);
		tb.setHighestOnQueue(100000);
		tb.run();
		for(int i = 0;i<deleteWorkerCount;i++){
			System.out.println(tb.getResult().grade[i]);
		}

		//		testBench17.setQueue(pq);
		//		testBench17.setNumDeleteWorkers(5);
		//		testBench17.setNumInsertWorkers(5);
		//		testBench17.setHighestOnQueue(100000);
		//		testBench17.run();
		//		for(int i = 0;i<5;i++){
		//			System.out.println(testBench17.getResult().grade[i]);
		//		}



		//		simpleTest(pq);






		//		//Run TB4 x times
		//		int x=100;
		//		while(x!=0){
		//			testBenchNew.setQueue(pq);
		//			testBenchNew.run();
		//			x--;
		//		}
		//		//Get the result of all runs
		//		testBenchNew.getResult();
		//		//create CSV file with results
		//		
		//		

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

	public static TestBench testBench = new TestBench() {

		@Override
		public void run() {

			StopWatch timer = new StopWatch();

			int numWorkers = 5;

			SimpleInsertWorker[] insertWorkers = new  SimpleInsertWorker[numWorkers]; 
			Thread[] insertWorkerThreads = new Thread[numWorkers];

			for(int i=0;i<numWorkers; i++)
			{
				insertWorkers[i] = new SimpleInsertWorker(_queue, 100*i ,100);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			SimpleDeleteWorker[] deleteWorkers = new  SimpleDeleteWorker[numWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[numWorkers];

			for(int i=0;i<numWorkers; i++)
			{
				deleteWorkers[i] = new SimpleDeleteWorker(_queue);
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


			//collect results from each worker
			//each iteretion will call saveResult
		}
	};

	public static TestBench testBench2 = new TestBench() {

		final int _itemsPerThread = 100;
		
		@Override
		public void run() {
			StopWatch timer = new StopWatch();

			SimpleInsertWorker[] insertWorkers = new SimpleInsertWorker[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];
			for(int i=0; i < _numInsertWorkers; i++)
			{
				insertWorkers[i] = new SimpleInsertWorker(_queue, _itemsPerThread*i ,_itemsPerThread);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			SimpleDeleteWorker[] deleteWorkers = new  SimpleDeleteWorker[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new SimpleDeleteWorker(_queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}

			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}

			timer.startTimer();

			//	    try {
			//	      Thread.sleep(numMilliseconds);
			//	    } catch (InterruptedException ignore) {;}

			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			timer.stopTimer();

			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			// Output the statistics
			System.out.println("time: " + timer.getElapsedTime());
			
			saveResult(timer.getElapsedTime(), _itemsPerThread*_numInsertWorkers, _itemsPerThread*_numDeleteWorkers, grade);
		}
	};

	public static TestBench testBench3 = new TestBench() {
		final int _itemsPerThread = 100;

		@Override
		public void run() {
			StopWatch insertTimer = new StopWatch();
			StopWatch deleteTimer = new StopWatch();

			SimpleInsertWorker[] insertWorkers = new  SimpleInsertWorker[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				insertWorkers[i] = new SimpleInsertWorker(_queue, 100*i ,100);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			SimpleDeleteWorker[] deleteWorkers = new  SimpleDeleteWorker[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new SimpleDeleteWorker(_queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}



			insertTimer.startTimer();

			//    try {
			//         Thread.sleep(numMilliseconds);
			//    } catch (InterruptedException ignore) {;}


			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			insertTimer.stopTimer();
			
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}

			deleteTimer.startTimer();
			
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}
			deleteTimer.stopTimer();
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			// Output the statistics

//			System.out.println("time: " + timer.getElapsedTime());
			
			saveResult(insertTimer.getElapsedTime(), deleteTimer.getElapsedTime(), _itemsPerThread*_numInsertWorkers, _itemsPerThread*_numDeleteWorkers, grade);
		}
	};

	/**
	 * All workers run for a constant amount of time, interleaved values inserted
	 */
	public static TestBench testBench4 = new TestBench() {
		@Override
		public void run() {
			StopWatch timer = new StopWatch();

			int numMilliseconds = 200;

			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new InterleavingStepGenerator(i,  _numInsertWorkers), _queue);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}

			timer.startTimer();

			for(int i=0;i<_numInsertWorkers;i++)
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

			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			timer.stopTimer();
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			// Output the statistics

			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i]._totalPackets;
			}

			System.out.println("count: " + totalCount);
			System.out.println("time: " + timer.getElapsedTime());
			System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
			
			saveResult(timer.getElapsedTime(), totalCount, totalCount, grade);
		}
	};

	/**
	 * All workers run for a constant amount of time, interleaved values inserted
	 * 
	 */
	public static TestBench testBench5 = new TestBench() {
		@Override
		public void run() {
			StopWatch timer = new StopWatch();


			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new InterleavingStepGenerator(i,  _numInsertWorkers), _queue);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}

			timer.startTimer();

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}




			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}


			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers



			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			timer.stopTimer();
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			// Output the statistics

			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i]._totalPackets;
			}

			System.out.println("delete min count: " + totalCount);
			System.out.println("time: " + timer.getElapsedTime());
			System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
			
			saveResult(timer.getElapsedTime(), totalCount, totalCount, grade);
		}
	};

	public static TestBench testBench6 = new TestBench() {
		@Override
		public void run() {
			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new InterleavingStepGenerator(i,  _numInsertWorkers), _queue);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}

			/**********		 Insertion part	**********/

			// Start insert workers
			StopWatch timer = new StopWatch();
			timer.startTimer();

			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}


			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}


			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers



			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}


			timer.stopTimer();

			long totalInsertCount = 0;
			for(int i=0;i<_numInsertWorkers;i++)
			{
				totalInsertCount+= insertWorkers[i]._totalPackets;
				//			System.out.println("insert (thread "+i+") count: " + insertWorkers[i]._totalPackets);
				//			System.out.println(insertWorkers[i]._totalPackets/timer.getElapsedTime() + " pkts / ms");	
			}
			// Output the statistics for insert only
			long timeOfInsertion = timer.getElapsedTime();



			/**********		 Deletion part  	**********/

			// Start delete workers
			timer = new StopWatch();
			timer.startTimer();


			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}


			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			timer.stopTimer();
			// Output the statistics for delete only
			long timeOfDeletion = timer.getElapsedTime();


			long totalDeleteCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalDeleteCount+= deleteWorkers[i]._totalPackets;
			}


			System.out.println("");
			System.out.println("Num of insert workers: "+ _numInsertWorkers );
			System.out.println("insert count: " + totalInsertCount);
			System.out.println("time: " + timeOfInsertion);
			System.out.println(totalInsertCount/timeOfInsertion + " pkts / ms");	


			System.out.println("");
			System.out.println("Num of delete workers: "+ _numDeleteWorkers );
			System.out.println("delete min count: " + totalDeleteCount);
			System.out.println("time: " + timeOfDeletion);
			System.out.println(totalDeleteCount/timeOfDeletion + " pkts / ms");

			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(timeOfInsertion, timeOfDeletion, totalInsertCount, totalDeleteCount, grade);
		}
	};

	public static TestBench testBench7 = new TestBench() {
		@Override
		public void run() {
			StopWatch timer = new StopWatch();


			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new InterleavingStepGenerator(i,  _numInsertWorkers), _queue);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorkerWithoutEmptying[] deleteWorkers = new  AdvancedDeleteWorkerWithoutEmptying[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorkerWithoutEmptying(doneWorkers, _queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}

			timer.startTimer();


			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}


			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}




			//		boolean done = false;
			//		while(!done )
			//		{
			//			System.out.println("timer: "+timer.getElapsedTime());
			//			if(timer.getElapsedTime()>=timeOut){
			//				done=true;
			//			}
			//		}



			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers


			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			timer.stopTimer();

			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			// Output the statistics for the delete min

			long totalInsertCount = 0;
			for(int i=0;i<_numInsertWorkers;i++)
			{
				totalInsertCount+= insertWorkers[i]._totalPackets;
			}

			long totalDeleteCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalDeleteCount+= deleteWorkers[i]._totalPackets;
			}
			System.out.println("");
			System.out.println("delete min count: " + totalDeleteCount);
			System.out.println("time: " + timer.getElapsedTime());
			System.out.println(totalDeleteCount/timer.getElapsedTime() + " pkts / ms");

			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(timer.getElapsedTime(), totalInsertCount, totalDeleteCount, grade);
		}
	};
	public static TestBench testBench8 = new TestBench() {
		@Override
		public void run() {
			StopWatch timer = new StopWatch();


			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];
			DecreasingStepGenerator decreasingStepGenerator = new DecreasingStepGenerator(_highest);
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher,decreasingStepGenerator , _queue);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}

			//Start time for delete min
			timer.startTimer();


			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}

			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}




			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers



			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}




			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers


			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			timer.stopTimer();


			// Output the statistics for the delete min

			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i]._totalPackets;
			}
			System.out.println("");
			System.out.println("delete min count: " + totalCount);
			System.out.println("time: " + timer.getElapsedTime());
			System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");

			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(timer.getElapsedTime(), totalCount, totalCount, grade);
		}
	};


	public static TestBench testBench9 = new TestBench() {
		@Override
		public void run() {
			StopWatch timer = new StopWatch();

			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorkerUntilValue[] insertWorkers = new  AdvancedInsertWorkerUntilValue[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];
			DecreasingStepGenerator decreasingStepGenerator = new DecreasingStepGenerator(_highest);
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorkerUntilValue(decreasingStepGenerator , _queue, 0);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}


			//Start time for delete min
			timer.startTimer();

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}


			// no need to stop insert workers - will finish when reaching to value=0
			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			timer.stopTimer();

			// Output the statistics for the delete min
			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i]._totalPackets;
			}
			System.out.println("");
			System.out.println("delete min count: " + totalCount);
			System.out.println("time: " + timer.getElapsedTime());
			System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(timer.getElapsedTime(), _highest, totalCount, grade);
		}
	};


	public static TestBench testBench10 = new TestBench() {
		@Override
		public void run() {

			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorkerUntilValue[] insertWorkers = new  AdvancedInsertWorkerUntilValue[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];
			DecreasingStepGenerator decreasingStepGenerator = new DecreasingStepGenerator(_highest);
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorkerUntilValue(decreasingStepGenerator , _queue, 0);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers];
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			/**********		 Deal with insert only		**********/

			// Start insert workers
			StopWatch insertTimer = new StopWatch();
			insertTimer.startTimer();

			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}


			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}


			insertTimer.stopTimer();
			// Output the statistics for insert only

			long totalInsertCount = 0;
			for(int i=0;i<_numInsertWorkers;i++)
			{
				totalInsertCount+= insertWorkers[i]._totalPackets;
				//			System.out.println("insert (thread "+i+") count: " + insertWorkers[i]._totalPackets);
				//			System.out.println(insertWorkers[i]._totalPackets/timer.getElapsedTime() + " pkts / ms");	
			}


			/**********		 Deal with delete only		**********/

			// Start delete workers
			StopWatch deleteTimer = new StopWatch();
			deleteTimer.startTimer();


			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}


			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			deleteTimer.stopTimer();
			// Output the statistics for delete only


			long totalDeleteCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalDeleteCount+= deleteWorkers[i]._totalPackets;
			}


			System.out.println("");
			System.out.println("Num of insert workers: "+ _numInsertWorkers );
			System.out.println("insert count: " + totalInsertCount);
			System.out.println("time: " + insertTimer.getElapsedTime());
			System.out.println(totalInsertCount/insertTimer.getElapsedTime() + " pkts / ms");	


			System.out.println("");
			System.out.println("Num of delete workers: "+ _numDeleteWorkers );
			System.out.println("delete min count: " + totalDeleteCount);
			System.out.println("time: " + deleteTimer.getElapsedTime());
			System.out.println(totalDeleteCount/deleteTimer.getElapsedTime() + " pkts / ms");
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(insertTimer.getElapsedTime(), deleteTimer.getElapsedTime(), totalInsertCount, totalDeleteCount, grade);
		}
	};

	public static TestBench testBench11 = new TestBench() {
		@Override
		public void run() {
			StopWatch timer = new StopWatch();

			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];
			IncreasingStepGenerator increasingStepGenerator = new IncreasingStepGenerator();
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher,increasingStepGenerator , _queue);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}

			//Start time for delete min
			timer.startTimer();


			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}

			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}




			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers



			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}




			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers


			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			timer.stopTimer();


			// Output the statistics for the delete min

			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i]._totalPackets;
			}
			System.out.println("");
			System.out.println("delete min count: " + totalCount);
			System.out.println("time: " + timer.getElapsedTime());
			System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");

			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(timer.getElapsedTime(), totalCount, totalCount, grade);
		}
	};

	public static TestBench testBench12 = new TestBench() {
		@Override
		public void run() {
			StopWatch timer = new StopWatch();

			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorkerUntilValue[] insertWorkers = new  AdvancedInsertWorkerUntilValue[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];
			IncreasingStepGenerator increasingStepGenerator = new IncreasingStepGenerator(_highest);
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorkerUntilValue(increasingStepGenerator , _queue, _highest);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}


			//Start time for delete min
			timer.startTimer();

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}


			// no need to stop insert workers - will finish when reaching to value=0
			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			timer.stopTimer();

			// Output the statistics for the delete min
			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i]._totalPackets;
			}
			System.out.println("");
			System.out.println("delete min count: " + totalCount);
			System.out.println("time: " + timer.getElapsedTime());
			System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(timer.getElapsedTime(), _highest, totalCount, grade);
		}
	};

	public static TestBench testBench13 = new TestBench(){

		@Override
		public void run() {

			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorkerUntilValue[] insertWorkers = new  AdvancedInsertWorkerUntilValue[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];
			IncreasingStepGenerator increasingStepGenerator = new IncreasingStepGenerator(_highest);
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorkerUntilValue(increasingStepGenerator , _queue, _highest);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			/**********		 Deal with insert only		**********/

			// Start insert workers
			StopWatch insertTimer = new StopWatch();
			System.out.println("Inserting...");
			insertTimer.startTimer();

			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}


			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}


			insertTimer.stopTimer();
			// Output the statistics for insert only

			long totalInsertCount = 0;
			for(int i=0;i<_numInsertWorkers;i++)
			{
				totalInsertCount+= insertWorkers[i]._totalPackets;
				//			System.out.println("insert (thread "+i+") count: " + insertWorkers[i]._totalPackets);
				//			System.out.println(insertWorkers[i]._totalPackets/timer.getElapsedTime() + " pkts / ms");	
			}


			/**********		 Deal with delete only		**********/

			// Start delete workers
			StopWatch deleteTimer = new StopWatch();
			deleteTimer.startTimer();


			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}


			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			deleteTimer.stopTimer();
			// Output the statistics for delete only


			long totalDeleteCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalDeleteCount+= deleteWorkers[i]._totalPackets;
			}
			
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}

			System.out.println("");
			System.out.println("insert count: " + totalInsertCount);
			System.out.println("time: " + insertTimer.getElapsedTime());
			System.out.println(totalInsertCount/insertTimer.getElapsedTime() + " pkts / ms");	


			System.out.println("");
			System.out.println("Num of delete workers: "+ _numDeleteWorkers );
			System.out.println("delete min count: " + totalDeleteCount);
			System.out.println("time: " + deleteTimer.getElapsedTime());
			System.out.println(totalDeleteCount/deleteTimer.getElapsedTime() + " pkts / ms");
			
			saveResult(insertTimer.getElapsedTime(), deleteTimer.getElapsedTime(), totalInsertCount, totalDeleteCount, grade);
		}
	};



	public static TestBench testBench14 = new TestBench() {
		@Override
		public void run() {
			StopWatch timer = new StopWatch();


			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new RandomStepGenerator(i), _queue);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}

			timer.startTimer();

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}




			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}


			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers



			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			timer.stopTimer();
			// Output the statistics

			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i]._totalPackets;
			}
			System.out.println("");
			System.out.println("delete min count: " + totalCount);
			System.out.println("time: " + timer.getElapsedTime());
			System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(timer.getElapsedTime(), totalCount, totalCount, grade);
		}
	};

	public static TestBench testBench15 = new TestBench() {
		@Override
		public void run() {
			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new RandomStepGenerator(i), _queue);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}

			/**********		 Insertion part	**********/

			System.out.println("Starting insert...");
			// Start insert workers
			StopWatch timer = new StopWatch();
			timer.startTimer();

			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}


			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}


			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers



			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}


			timer.stopTimer();

			long totalInsertCount = 0;
			for(int i=0;i<_numInsertWorkers;i++)
			{
				totalInsertCount+= insertWorkers[i]._totalPackets;
				//			System.out.println("insert (thread "+i+") count: " + insertWorkers[i]._totalPackets);
				//			System.out.println(insertWorkers[i]._totalPackets/timer.getElapsedTime() + " pkts / ms");	
			}
			// Output the statistics for insert only
			long timeOfInsertion = timer.getElapsedTime();



			/**********		 Deletion part  	**********/

			// Start delete workers
			timer = new StopWatch();
			timer.startTimer();


			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}


			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}

			timer.stopTimer();
			// Output the statistics for delete only
			long timeOfDeletion = timer.getElapsedTime();


			long totalDeleteCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalDeleteCount+= deleteWorkers[i]._totalPackets;
			}


			System.out.println("");
			System.out.println("Num of insert workers: "+ _numInsertWorkers );
			System.out.println("insert count: " + totalInsertCount);
			System.out.println("time: " + timeOfInsertion);
			System.out.println(totalInsertCount/timeOfInsertion + " pkts / ms");	


			System.out.println("");
			System.out.println("Num of delete workers: "+ _numDeleteWorkers );
			System.out.println("delete min count: " + totalDeleteCount);
			System.out.println("time: " + timeOfDeletion);
			System.out.println(totalDeleteCount/timeOfDeletion + " pkts / ms");
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(timeOfInsertion, timeOfDeletion, totalInsertCount, totalDeleteCount, grade);
		}
	};


	public static TestBench testBench16 = new TestBench(){

		@Override
		public void run() {
			{
				StopWatch timer = new StopWatch();


				// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
				PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
				PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
				PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

				AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
				Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];

				for(int i=0;i<_numInsertWorkers; i++)
				{
					// Initialize insert workers with interleaving number generators
					insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new RandomStepGenerator(i), _queue);
					insertWorkerThreads[i] = new Thread(insertWorkers[i]);
				}

				AdvancedDeleteWorkerWithoutEmptying[] deleteWorkers = new  AdvancedDeleteWorkerWithoutEmptying[_numDeleteWorkers]; 
				Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];

				for(int i=0;i<_numDeleteWorkers; i++)
				{
					deleteWorkers[i] = new AdvancedDeleteWorkerWithoutEmptying(doneWorkers, _queue);
					deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
				}


				// Start all the workers
				for(int i=0;i<_numInsertWorkers;i++)
				{
					insertWorkerThreads[i].start();
				}

				timer.startTimer();


				for(int i=0;i<_numDeleteWorkers;i++)
				{
					deleteWorkerThreads[i].start();
				}


				try {
					Thread.sleep(_timeOutMilliseconds);
				} catch (InterruptedException ignore) {;}




				//		boolean done = false;
				//		while(!done )
				//		{
				//			System.out.println("timer: "+timer.getElapsedTime());
				//			if(timer.getElapsedTime()>=timeOut){
				//				done=true;
				//			}
				//		}



				// Stop delete Workers - they are responsible for leaving the queue empty
				doneWorkers.value = true;
				memFence.value = true; // memFence is a 'volatile' forcing a memory fence
				// which means that done.value is visible to the workers


				for(int i=0;i<_numDeleteWorkers;i++)
				{
					try {
						deleteWorkerThreads[i].join();
					} catch (InterruptedException ignore) {;}
				}

				timer.stopTimer();

				// stop insert workers
				doneDispatcher.value = true;
				memFence.value = true; // memFence is a 'volatile' forcing a memory fence
				// which means that done.value is visible to the workers

				for(int i=0;i<_numInsertWorkers;i++)
				{
					try {
						insertWorkerThreads[i].join();
					} catch (InterruptedException ignore) {;}
				}

				// Output the statistics for the delete min

				long totalCount = 0;
				for(int i=0;i<_numDeleteWorkers;i++)
				{
					totalCount+= deleteWorkers[i]._totalPackets;
				}
				System.out.println("");
				System.out.println("delete min count: " + totalCount);
				System.out.println("time: " + timer.getElapsedTime());
				System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");

				//grade is not relevant because generating random values
				saveResult(0, timer.getElapsedTime(), 0, totalCount, null);

			}
		}
	};


	public static TestBench testBench17 = new TestBench(){
	
		@Override
		public void run() {
	
			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
	
			AdvancedInsertWorkerUntilValue[] insertWorkers = new  AdvancedInsertWorkerUntilValue[_numInsertWorkers]; 
			Thread[] insertWorkerThreads = new Thread[_numInsertWorkers];
			IncreasingStepGenerator increasingStepGenerator = new IncreasingStepGenerator(_highest);
			int runs = _highest/_numDeleteWorkers;
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorkerUntilValue(increasingStepGenerator , _queue, _highest);
				insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}
	
			AdvancedInsertAndDelete[] deleteWorkers = new AdvancedInsertAndDelete[_numDeleteWorkers]; 
			Thread[] deleteWorkerThreads = new Thread[_numDeleteWorkers];
	
			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedInsertAndDelete(_queue, runs, _highest);
				deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}
	
	
			/**********		 Deal with insert only		**********/
	
			// Start insert workers
			StopWatch insertTimer = new StopWatch();
			System.out.println("Inserting...");
			insertTimer.startTimer();
	
			for(int i=0;i<_numInsertWorkers;i++)
			{
				insertWorkerThreads[i].start();
			}
	
	
			for(int i=0;i<_numInsertWorkers;i++)
			{
				try {
					insertWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}
	
	
			insertTimer.stopTimer();
			// Output the statistics for insert only
	
			long totalInsertCount = 0;
			for(int i=0;i<_numInsertWorkers;i++)
			{
				totalInsertCount+= insertWorkers[i]._totalPackets;
				//			System.out.println("insert (thread "+i+") count: " + insertWorkers[i]._totalPackets);
				//			System.out.println(insertWorkers[i]._totalPackets/timer.getElapsedTime() + " pkts / ms");	
			}
	
	
			/**********		 Deal with delete only		**********/
	
			// Start delete workers
			StopWatch deleteTimer = new StopWatch();
			deleteTimer.startTimer();
	
	
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				deleteWorkerThreads[i].start();
			}
	
	
	
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				try {
					deleteWorkerThreads[i].join();
				} catch (InterruptedException ignore) {;}
			}
	
			deleteTimer.stopTimer();
	
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
	
	
	
	
			// Output the statistics for delete only
	
	
			long totalDeleteCount = _highest;
	
	
			System.out.println("");
			System.out.println("insert count: " + totalInsertCount);
			System.out.println("time: " + insertTimer.getElapsedTime());
			System.out.println(totalInsertCount/insertTimer.getElapsedTime() + " pkts / ms");	
	
	
			System.out.println("");
			System.out.println("Num of delete workers: "+ _numDeleteWorkers );
			System.out.println("delete min count: " + totalDeleteCount);
			System.out.println("time: " + deleteTimer.getElapsedTime());
			System.out.println(totalDeleteCount/deleteTimer.getElapsedTime() + " pkts / ms");
	
	
			saveResult(insertTimer.getElapsedTime(), deleteTimer.getElapsedTime(), totalInsertCount, totalDeleteCount, grade);
		}
	};


	private static class InterleavingStepGenerator implements INumberGenerator
	{
		final int _step;
		int _seed;
	
		public InterleavingStepGenerator(int seed, int step)
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

	private static class DecreasingStepGenerator implements INumberGenerator
	{
		int _value;
	
		public DecreasingStepGenerator(int highest)
		{
			_value = highest;
		}
	
		@Override
		public int getNext() {
	
			if(_value==0) return 0;
	
			return _value--;
		}
	}

	private static class IncreasingStepGenerator implements INumberGenerator
	{
		int _value=0;
		int _highest=-1;
	
		public IncreasingStepGenerator(int highest){ 
			_highest = highest;
		}
	
		public IncreasingStepGenerator() {
		}
	
		@Override
		public int getNext() {
			//if value reached the highest number, return the highest
			if(_value==_highest) return _highest;
			return _value++;
		}
	}

	private static class RandomStepGenerator implements INumberGenerator
	{
	
		LockFreeRandom _random;
	
		public RandomStepGenerator(int seed){ 
			_random = new LockFreeRandom(seed);
		}
	
	
		@Override
		public int getNext() {
			return	_random.nextInt();
		}
	}


}
