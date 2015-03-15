package priorityQueue.tests;

import priorityQueue.news.IPriorityQueue;

public abstract class TestBench {

	private TestBenchResult _result;
	protected IPriorityQueue _queue;
	protected int _numInsertWorkers;
	protected int _numDeleteWorkers;
	protected int _highest;
	protected int _timeOutMilliseconds;

	protected Thread[] _insertWorkerThreads;
	protected Thread[] _deleteWorkerThreads;
	protected StopWatch _insertTimer = new StopWatch();
	protected StopWatch _deleteTimer = new StopWatch();



	public void setNumInsertWorkers(int numInsertWorkers){ 
		_numInsertWorkers = numInsertWorkers;
	}
	public void setNumDeleteWorkers(int numDeleteWorkers){ 
		_numDeleteWorkers= numDeleteWorkers;
	}

	public void setHighestOnQueue(int highest){ 
		_highest = highest;
	}
	
	public int getItemsPerThread()
	{
		return _highest/_numInsertWorkers;
	}

	public void setTimeOutMillisecond(int timeOutMillisecond){ 
		_timeOutMilliseconds = timeOutMillisecond;
	}
	
	protected void startAllWorkers()
	{
		startInsertWorkers();
		startDeleteWorkers();
	}
	
	protected void startInsertWorkers()
	{
		_insertTimer.startTimer();
		
		for(int i=0;i<_numInsertWorkers;i++)
		{
			_insertWorkerThreads[i].start();
		}
	}
	
	protected void startDeleteWorkers()
	{
		_deleteTimer.startTimer();
		
		for(int i=0;i<_numDeleteWorkers;i++)
		{
			_deleteWorkerThreads[i].start();
		}
	}
	
	protected void joinInsertWorkers()
	{
		for(int i=0;i<_numInsertWorkers;i++)
		{
			try {
				_insertWorkerThreads[i].join();
			} catch (InterruptedException ignore) {;}
		}
		
		_insertTimer.stopTimer();
	}
	
	protected void joinDeleteWorkers()
	{
		for(int i=0;i<_numDeleteWorkers;i++)
		{
			try {
				_deleteWorkerThreads[i].join();
			} catch (InterruptedException ignore) {;}
		}
		
		_deleteTimer.stopTimer();
	}

	public void runTest() {
		// TODO: Initialize signals?
		
		// TODO: Initialize arrays
		
		// TODO: Initialize workers/threads
		
		run();
		
		// TODO: Calculate statistics/results
		
		// TODO: get grades
		
		// TODO: save result
	}


	protected abstract void run();

	public void setQueue(IPriorityQueue queue){

		_queue=queue;
	}

	public TestBenchResult getResult(){
		return _result;
	}

	protected void saveResult(long insertCount, long deleteCount,	int[] grade){

		TestBenchResult result= new TestBenchResult(); 

		result.deleteCount=deleteCount;
		result.deleteTime=_deleteTimer.getElapsedTime();
		result.insertCount=insertCount;
		result.insertTime=_insertTimer.getElapsedTime();
		result.grade=grade;

		_result=result;
	}

	class TestBenchResult {

		long insertTime=0;
		long deleteTime=0;
		long insertCount=0;
		long deleteCount=0;
		int[] grade;

	}
}
