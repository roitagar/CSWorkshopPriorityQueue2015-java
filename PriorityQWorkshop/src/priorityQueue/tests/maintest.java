package priorityQueue.tests;

import priorityQueue.news.*;
import priorityQueue.utils.*;


public class maintest {




	public static void main(String[] args) {

		final int skiplistHeight = 10;
		int insertWorkerCount = 5;
		int deleteWorkerCount = 5;
		IPriorityQueue pq = null;

		PriorityQueueFactory[] factories = {
				new NaiveLockNativePriorityQueueFactory(),
				new GlobalLockSprayListPriorityQueueFactory(),
				new TMSprayListPriorityQueueWithCounterFactory(),
				new TMSprayListPriorityQueueWithoutCounterFactory(),
				new LockFreeSprayListPriorityQueueFactory(),
				new CoolSprayListPriorityQueueFactory(),
				new SeqSprayListPriorityQueueFactory(),
		};
		
		TestBench[] simultaneousTests = {
				testBench2,
				testBench5,
				testBench8,
				testBench11,
				testBench14,
		};
		
		TestBench[] serialTests = {
				testBench3,
				testBench6,
				testBench10,
				testBench13,
				testBench15,
		};
		
		TestBench[][] tests = {simultaneousTests, serialTests};
		
		for(PriorityQueueFactory factory:factories)
		{
			System.out.println("Start test with " + factory.getQueueType());

			int[][] inserters	= {{1, 1, 7, 4}, {1, 8}};
			int[][] deleters	= {{1, 7, 1, 4}, {1, 8}};
			for(int i=0;i<inserters.length;i++)
			{
				for(int j=0;j<inserters[i].length;j++)
				{
					insertWorkerCount = inserters[i][j];
					deleteWorkerCount = deleters[i][j];

					for(TestBench tb:tests[i])
					{
						System.out.println("Start " + i + ", " + j);
						pq = factory.Create(skiplistHeight);
						tb.setQueue(pq);
						tb.setNumDeleteWorkers(deleteWorkerCount);
						tb.setNumInsertWorkers(insertWorkerCount);
						tb.setHighestOnQueue(32);
						tb.setTimeOutMillisecond(10);
						try{
							tb.runTest();
							for(int k = 0;k<deleteWorkerCount;k++){
								System.out.println(tb.getResult().grade[k]);
							}
						}
						catch(Exception e) {
							// TODO: Mostly divide by zero due to short timings
							System.out.println(e.getMessage());
						} 

					}
				}
			}
		}
		
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



//		TestBench tb = testBench13;
//		tb.setQueue(pq);
//		tb.setNumDeleteWorkers(deleteWorkerCount);
//		tb.setNumInsertWorkers(insertWorkerCount);
//		tb.setHighestOnQueue(100000);
//		tb.run();
//		for(int i = 0;i<deleteWorkerCount;i++){
//			System.out.println(tb.getResult().grade[i]);
//		}

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

	public static TestBench testBench2 = new TestBench() {
		@Override
		public void run() {
			InsertWorker[] insertWorkers = new SimpleInsertWorker[_numInsertWorkers]; 
			_insertWorkerThreads = new Thread[_numInsertWorkers];
			for(int i=0; i < _numInsertWorkers; i++)
			{
				insertWorkers[i] = new SimpleInsertWorker(_queue, getItemsPerThread()*i ,getItemsPerThread());
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			SimpleDeleteWorker[] deleteWorkers = new  SimpleDeleteWorker[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new SimpleDeleteWorker(_queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}

			startAllWorkers();

			joinInsertWorkers();

			joinDeleteWorkers();

			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			// Output the statistics
			System.out.println("time: " + _deleteTimer.getElapsedTime());
			
			saveResult(_deleteTimer.getElapsedTime(), getItemsPerThread()*_numInsertWorkers, getItemsPerThread()*_numDeleteWorkers, grade);
		}
	};

	public static TestBench testBench3 = new TestBench() {
		@Override
		public void run() {
			InsertWorker[] insertWorkers = new  SimpleInsertWorker[_numInsertWorkers]; 
			_insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				insertWorkers[i] = new SimpleInsertWorker(_queue, getItemsPerThread()*i ,getItemsPerThread());
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			SimpleDeleteWorker[] deleteWorkers = new  SimpleDeleteWorker[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new SimpleDeleteWorker(_queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			startInsertWorkers();

			joinInsertWorkers();

			startDeleteWorkers();

			joinDeleteWorkers();
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			// Output the statistics

			saveResult(_insertTimer.getElapsedTime(), _deleteTimer.getElapsedTime(), getItemsPerThread()*_numInsertWorkers, getItemsPerThread()*_numDeleteWorkers, grade);
		}
	};

	/**
	 * All workers run for a constant amount of time, interleaved values inserted
	 */
	public static TestBench testBench5 = new TestBench() {
		@Override
		public void run() {

			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			_insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new InterleavingStepGenerator(i,  _numInsertWorkers), _queue);
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			startAllWorkers();


			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}


			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers


			joinInsertWorkers();
			
			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			joinDeleteWorkers();
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			// Output the statistics

			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i].totalPackets();
			}

			System.out.println("delete min count: " + totalCount);
			System.out.println("time: " + _deleteTimer.getElapsedTime());
			System.out.println(totalCount/_deleteTimer.getElapsedTime() + " pkts / ms");
			
			saveResult(_deleteTimer.getElapsedTime(), totalCount, totalCount, grade);
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
			_insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new InterleavingStepGenerator(i,  _numInsertWorkers), _queue);
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}

			/**********		 Insertion part	**********/

			// Start insert workers
			startInsertWorkers();


			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}


			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			joinInsertWorkers();

			long totalInsertCount = 0;
			for(int i=0;i<_numInsertWorkers;i++)
			{
				totalInsertCount+= insertWorkers[i].totalPackets();
			}
			
			// Output the statistics for insert only
			long timeOfInsertion = _insertTimer.getElapsedTime();

			
			/**********		 Deletion part  	**********/

			// Start delete workers
			startDeleteWorkers();

			
			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			joinDeleteWorkers();
			
			// Output the statistics for delete only
			long timeOfDeletion = _deleteTimer.getElapsedTime();


			long totalDeleteCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalDeleteCount+= deleteWorkers[i].totalPackets();
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
			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			_insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new InterleavingStepGenerator(i,  _numInsertWorkers), _queue);
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorkerWithoutEmptying[] deleteWorkers = new  AdvancedDeleteWorkerWithoutEmptying[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorkerWithoutEmptying(doneWorkers, _queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			startAllWorkers();


			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}


			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			joinDeleteWorkers();

			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			joinInsertWorkers();
			
			// Output the statistics for the delete min

			long totalInsertCount = 0;
			for(int i=0;i<_numInsertWorkers;i++)
			{
				totalInsertCount+= insertWorkers[i].totalPackets();
			}

			long totalDeleteCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalDeleteCount+= deleteWorkers[i].totalPackets();
			}

			System.out.println();
			System.out.println("delete min count: " + totalDeleteCount);
			System.out.println("time: " + _deleteTimer.getElapsedTime());
			System.out.println(totalDeleteCount/_deleteTimer.getElapsedTime() + " pkts / ms");

			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(_deleteTimer.getElapsedTime(), totalInsertCount, totalDeleteCount, grade);
		}
	};

	public static TestBench testBench8 = new TestBench() {
		@Override
		public void run() {
			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			_insertWorkerThreads = new Thread[_numInsertWorkers];
			DecreasingStepGenerator decreasingStepGenerator = new DecreasingStepGenerator(_highest);
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher,decreasingStepGenerator , _queue);
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			startAllWorkers();
			
			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}


			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			
			joinInsertWorkers();

			
			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers


			joinDeleteWorkers();


			// Output the statistics for the delete min

			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i].totalPackets();
			}
			System.out.println("");
			System.out.println("delete min count: " + totalCount);
			System.out.println("time: " + _deleteTimer.getElapsedTime());
			System.out.println(totalCount/_deleteTimer.getElapsedTime() + " pkts / ms");

			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(_deleteTimer.getElapsedTime(), totalCount, totalCount, grade);
		}
	};


	public static TestBench testBench9 = new TestBench() {
		@Override
		public void run() {
			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			InsertWorker[] insertWorkers = new  AdvancedInsertWorkerUntilValue[_numInsertWorkers]; 
			_insertWorkerThreads = new Thread[_numInsertWorkers];
			DecreasingStepGenerator decreasingStepGenerator = new DecreasingStepGenerator(_highest);
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorkerUntilValue(decreasingStepGenerator , _queue, 0);
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			startAllWorkers();


			// no need to stop insert workers - will finish when reaching to value=0
			joinInsertWorkers();

			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			joinDeleteWorkers();

			// Output the statistics for the delete min
			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i].totalPackets();
			}
			System.out.println("");
			System.out.println("delete min count: " + totalCount);
			System.out.println("time: " + _deleteTimer.getElapsedTime());
			System.out.println(totalCount/_deleteTimer.getElapsedTime() + " pkts / ms");
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(_deleteTimer.getElapsedTime(), _highest, totalCount, grade);
		}
	};


	public static TestBench testBench10 = new TestBench() {
		@Override
		public void run() {

			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			InsertWorker[] insertWorkers = new  AdvancedInsertWorkerUntilValue[_numInsertWorkers]; 
			_insertWorkerThreads = new Thread[_numInsertWorkers];
			DecreasingStepGenerator decreasingStepGenerator = new DecreasingStepGenerator(_highest);
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorkerUntilValue(decreasingStepGenerator , _queue, 0);
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers];
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			/**********		 Deal with insert only		**********/

			// Start insert workers
			startInsertWorkers();


			joinInsertWorkers();
			
			// Output the statistics for insert only

			long totalInsertCount = 0;
			for(int i=0;i<_numInsertWorkers;i++)
			{
				totalInsertCount+= insertWorkers[i].totalPackets();
			}


			/**********		 Deal with delete only		**********/

			// Start delete workers
			startDeleteWorkers();


			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			joinDeleteWorkers();
			
			// Output the statistics for delete only

			long totalDeleteCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalDeleteCount+= deleteWorkers[i].totalPackets();
			}


			System.out.println("");
			System.out.println("Num of insert workers: "+ _numInsertWorkers );
			System.out.println("insert count: " + totalInsertCount);
			System.out.println("time: " + _insertTimer.getElapsedTime());
			System.out.println(totalInsertCount/_insertTimer.getElapsedTime() + " pkts / ms");	


			System.out.println("");
			System.out.println("Num of delete workers: "+ _numDeleteWorkers );
			System.out.println("delete min count: " + totalDeleteCount);
			System.out.println("time: " + _deleteTimer.getElapsedTime());
			System.out.println(totalDeleteCount/_deleteTimer.getElapsedTime() + " pkts / ms");
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(_insertTimer.getElapsedTime(), _deleteTimer.getElapsedTime(), totalInsertCount, totalDeleteCount, grade);
		}
	};

	public static TestBench testBench11 = new TestBench() {
		@Override
		public void run() {
			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			_insertWorkerThreads = new Thread[_numInsertWorkers];
			IncreasingStepGenerator increasingStepGenerator = new IncreasingStepGenerator();
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher,increasingStepGenerator , _queue);
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			startAllWorkers();

			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}


			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers


			joinInsertWorkers();


			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers


			joinDeleteWorkers();


			// Output the statistics for the delete min

			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i].totalPackets();
			}
			System.out.println("");
			System.out.println("delete min count: " + totalCount);
			System.out.println("time: " + _deleteTimer.getElapsedTime());
			System.out.println(totalCount/_deleteTimer.getElapsedTime() + " pkts / ms");

			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(_deleteTimer.getElapsedTime(), totalCount, totalCount, grade);
		}
	};

	public static TestBench testBench12 = new TestBench() {
		@Override
		public void run() {
			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			InsertWorker[] insertWorkers = new  AdvancedInsertWorkerUntilValue[_numInsertWorkers]; 
			_insertWorkerThreads = new Thread[_numInsertWorkers];
			IncreasingStepGenerator increasingStepGenerator = new IncreasingStepGenerator(_highest);
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorkerUntilValue(increasingStepGenerator , _queue, _highest);
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			startAllWorkers();


			// no need to stop insert workers - will finish when reaching to value=0
			joinInsertWorkers();

			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			joinDeleteWorkers();

			// Output the statistics for the delete min
			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i].totalPackets();
			}
			System.out.println("");
			System.out.println("delete min count: " + totalCount);
			System.out.println("time: " + _deleteTimer.getElapsedTime());
			System.out.println(totalCount/_deleteTimer.getElapsedTime() + " pkts / ms");
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(_deleteTimer.getElapsedTime(), _highest, totalCount, grade);
		}
	};

	public static TestBench testBench13 = new TestBench(){

		@Override
		public void run() {

			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			InsertWorker[] insertWorkers = new  AdvancedInsertWorkerUntilValue[_numInsertWorkers]; 
			_insertWorkerThreads = new Thread[_numInsertWorkers];
			IncreasingStepGenerator increasingStepGenerator = new IncreasingStepGenerator(_highest);
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorkerUntilValue(increasingStepGenerator , _queue, _highest);
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			/**********		 Deal with insert only		**********/

			// Start insert workers
			startInsertWorkers();


			joinInsertWorkers();
			
			// Output the statistics for insert only

			long totalInsertCount = 0;
			for(int i=0;i<_numInsertWorkers;i++)
			{
				totalInsertCount+= insertWorkers[i].totalPackets();
			}


			/**********		 Deal with delete only		**********/

			// Start delete workers
			startDeleteWorkers();


			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			joinDeleteWorkers();
			
			// Output the statistics for delete only

			long totalDeleteCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalDeleteCount+= deleteWorkers[i].totalPackets();
			}
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}

			System.out.println();
			System.out.println("insert count: " + totalInsertCount);
			System.out.println("time: " + _insertTimer.getElapsedTime());
			System.out.println(totalInsertCount/_insertTimer.getElapsedTime() + " pkts / ms");	


			System.out.println();
			System.out.println("Num of delete workers: "+ _numDeleteWorkers );
			System.out.println("delete min count: " + totalDeleteCount);
			System.out.println("time: " + _deleteTimer.getElapsedTime());
			System.out.println(totalDeleteCount/_deleteTimer.getElapsedTime() + " pkts / ms");
			
			saveResult(_insertTimer.getElapsedTime(), _deleteTimer.getElapsedTime(), totalInsertCount, totalDeleteCount, grade);
		}
	};

	public static TestBench testBench14 = new TestBench() {
		@Override
		public void run() {
			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
			PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
			PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

			AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
			_insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new RandomStepGenerator(i), _queue);
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}


			// Start all the workers
			startAllWorkers();


			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}


			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers


			joinInsertWorkers();

			
			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			
			joinDeleteWorkers();
			
			
			// Output the statistics

			long totalCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalCount+= deleteWorkers[i].totalPackets();
			}
			System.out.println("");
			System.out.println("delete min count: " + totalCount);
			System.out.println("time: " + _deleteTimer.getElapsedTime());
			System.out.println(totalCount/_deleteTimer.getElapsedTime() + " pkts / ms");
			
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
			
			saveResult(_deleteTimer.getElapsedTime(), totalCount, totalCount, grade);
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
			_insertWorkerThreads = new Thread[_numInsertWorkers];

			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new RandomStepGenerator(i), _queue);
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}

			AdvancedDeleteWorker[] deleteWorkers = new  AdvancedDeleteWorker[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];

			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedDeleteWorker(doneWorkers, _queue);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}

			/**********		 Insertion part	**********/

			System.out.println("Starting insert...");
			// Start insert workers
			startInsertWorkers();


			try {
				Thread.sleep(_timeOutMilliseconds);
			} catch (InterruptedException ignore) {;}


			// stop insert workers
			doneDispatcher.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers


			joinInsertWorkers();

			
			long totalInsertCount = 0;
			for(int i=0;i<_numInsertWorkers;i++)
			{
				totalInsertCount+= insertWorkers[i].totalPackets();
				//			System.out.println("insert (thread "+i+") count: " + insertWorkers[i].totalPackets());
				//			System.out.println(insertWorkers[i].totalPackets()/timer.getElapsedTime() + " pkts / ms");	
			}
			
			// Output the statistics for insert only
			long timeOfInsertion = _insertTimer.getElapsedTime();



			/**********		 Deletion part  	**********/

			// Start delete workers
			startDeleteWorkers();


			// Stop delete Workers - they are responsible for leaving the queue empty
			doneWorkers.value = true;
			memFence.value = true; // memFence is a 'volatile' forcing a memory fence
			// which means that done.value is visible to the workers

			joinDeleteWorkers();
			
			// Output the statistics for delete only
			long timeOfDeletion = _deleteTimer.getElapsedTime();


			long totalDeleteCount = 0;
			for(int i=0;i<_numDeleteWorkers;i++)
			{
				totalDeleteCount+= deleteWorkers[i].totalPackets();
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
				// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
				PaddedPrimitiveNonVolatile<Boolean> doneDispatcher = new PaddedPrimitiveNonVolatile<Boolean>(false);
				PaddedPrimitiveNonVolatile<Boolean> doneWorkers = new PaddedPrimitiveNonVolatile<Boolean>(false);
				PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

				AdvancedInsertWorker[] insertWorkers = new  AdvancedInsertWorker[_numInsertWorkers]; 
				_insertWorkerThreads = new Thread[_numInsertWorkers];

				for(int i=0;i<_numInsertWorkers; i++)
				{
					// Initialize insert workers with interleaving number generators
					insertWorkers[i] = new AdvancedInsertWorker(doneDispatcher, new RandomStepGenerator(i), _queue);
					_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
				}

				AdvancedDeleteWorkerWithoutEmptying[] deleteWorkers = new  AdvancedDeleteWorkerWithoutEmptying[_numDeleteWorkers]; 
				_deleteWorkerThreads = new Thread[_numDeleteWorkers];

				for(int i=0;i<_numDeleteWorkers; i++)
				{
					deleteWorkers[i] = new AdvancedDeleteWorkerWithoutEmptying(doneWorkers, _queue);
					_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
				}


				// Start all the workers
				startAllWorkers();


				try {
					Thread.sleep(_timeOutMilliseconds);
				} catch (InterruptedException ignore) {;}


				// Stop delete Workers - they are responsible for leaving the queue empty
				doneWorkers.value = true;
				memFence.value = true; // memFence is a 'volatile' forcing a memory fence
				// which means that done.value is visible to the workers


				joinDeleteWorkers();

				
				// stop insert workers
				doneDispatcher.value = true;
				memFence.value = true; // memFence is a 'volatile' forcing a memory fence
				// which means that done.value is visible to the workers

				
				joinInsertWorkers();

				
				// Output the statistics for the delete min

				long totalCount = 0;
				for(int i=0;i<_numDeleteWorkers;i++)
				{
					totalCount+= deleteWorkers[i].totalPackets();
				}
				
				System.out.println("");
				System.out.println("delete min count: " + totalCount);
				System.out.println("time: " + _deleteTimer.getElapsedTime());
				System.out.println(totalCount/_deleteTimer.getElapsedTime() + " pkts / ms");

				//grade is not relevant because generating random values
				saveResult(0, _deleteTimer.getElapsedTime(), 0, totalCount, null);
			}
		}
	};

	public static TestBench testBench17 = new TestBench(){
	
		@Override
		public void run() {
	
			// Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
	
			InsertWorker[] insertWorkers = new  AdvancedInsertWorkerUntilValue[_numInsertWorkers]; 
			_insertWorkerThreads = new Thread[_numInsertWorkers];
			IncreasingStepGenerator increasingStepGenerator = new IncreasingStepGenerator(_highest);
			int runs = _highest/_numDeleteWorkers;
			for(int i=0;i<_numInsertWorkers; i++)
			{
				// Initialize insert workers with interleaving number generators
				insertWorkers[i] = new AdvancedInsertWorkerUntilValue(increasingStepGenerator , _queue, _highest);
				_insertWorkerThreads[i] = new Thread(insertWorkers[i]);
			}
	
			AdvancedInsertAndDelete[] deleteWorkers = new AdvancedInsertAndDelete[_numDeleteWorkers]; 
			_deleteWorkerThreads = new Thread[_numDeleteWorkers];
	
			for(int i=0;i<_numDeleteWorkers; i++)
			{
				deleteWorkers[i] = new AdvancedInsertAndDelete(_queue, runs, _highest);
				_deleteWorkerThreads[i] = new Thread(deleteWorkers[i]);
			}
	
	
			/**********		 Deal with insert only		**********/
	
			// Start insert workers
			startInsertWorkers();
	
	
			joinInsertWorkers();
			
			// Output the statistics for insert only
	
			long totalInsertCount = 0;
			for(int i=0;i<_numInsertWorkers;i++)
			{
				totalInsertCount+= insertWorkers[i].totalPackets();
			}
	
	
			/**********		 Deal with delete only		**********/
	
			// Start delete workers
			startDeleteWorkers();
	
			joinDeleteWorkers();
	
			//get grade of each worker
			int[] grade = new int[_numDeleteWorkers];
			for(int i=0; i<_numDeleteWorkers;i++){
				grade[i]=deleteWorkers[i].getGrade();
			}
	
	
			// Output the statistics for delete only
			long totalDeleteCount = _highest;
	
			System.out.println();
			System.out.println("insert count: " + totalInsertCount);
			System.out.println("time: " + _insertTimer.getElapsedTime());
			System.out.println(totalInsertCount/_insertTimer.getElapsedTime() + " pkts / ms");	
	
			System.out.println();
			System.out.println("Num of delete workers: "+ _numDeleteWorkers );
			System.out.println("delete min count: " + totalDeleteCount);
			System.out.println("time: " + _deleteTimer.getElapsedTime());
			System.out.println(totalDeleteCount/_deleteTimer.getElapsedTime() + " pkts / ms");
	
			saveResult(_insertTimer.getElapsedTime(), _deleteTimer.getElapsedTime(), totalInsertCount, totalDeleteCount, grade);
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
