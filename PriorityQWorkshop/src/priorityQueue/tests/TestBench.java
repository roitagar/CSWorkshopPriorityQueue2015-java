package priorityQueue.tests;

import priorityQueue.news.IPriorityQueue;

public abstract class TestBench {

	private TestBenchResult _result;
	protected IPriorityQueue _queue;
	protected int _numInsertWorkers;
	protected int _numDeleteWorkers;
	protected int _highest;
	protected int _timeOutMilliseconds;




	public void setNumInsertWorkers(int numInsertWorkers){ 
		_numInsertWorkers = numInsertWorkers;
	}
	public void setNumDeleteWorkers(int numDeleteWorkers){ 
		_numDeleteWorkers= numDeleteWorkers;
	}

	public void setHighestOnQueue(int highest){ 
		_highest = highest;
	}

	public void setTimeOutMillisecond(int timeOutMillisecond){ 
		_timeOutMilliseconds = timeOutMillisecond;
	}

	public abstract void run();

	public void setQueue(IPriorityQueue queue){

		_queue=queue;
	}

	public TestBenchResult getResult(){
		return _result;
	}

	/**
	 * two timers - insert and delete times are separate
	 */
	protected void saveResult(long insertTime, long deleteTime,	
			long insertCount, long deleteCount,	int[] grade){

		TestBenchResult result= new TestBenchResult(); 

		result.deleteCount=deleteCount;
		result.deleteTime=deleteTime;
		result.insertCount=insertCount;
		result.insertTime=insertTime;
		result.grade=grade;

		_result=result;
	}

	/**
	 * single timer - insert and delete are simultaneous
	 */
	protected void saveResult(long totalTime,	
			long insertCount, long deleteCount,	int[] grade){
		// TODO: add unified printing, consider the "0"
		saveResult(0, totalTime, insertCount, deleteCount, grade);
	}
	class TestBenchResult {

		long insertTime=0;
		long deleteTime=0;
		long insertCount=0;
		long deleteCount=0;
		int[] grade;

	}


}
