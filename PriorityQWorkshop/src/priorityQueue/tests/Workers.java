package priorityQueue.tests;

import priorityQueue.news.IPriorityQueue;

public class Workers {

}


class InsertWorker implements Runnable{

IPriorityQueue _queue;
int _amount;
int _from;


public InsertWorker(IPriorityQueue queue, int from, int amount){
	this._queue = queue;
	this._from = from;
	this._amount = amount;
}

public void run(){
	long tid = Thread.currentThread().getId();

	for(int i=_from;i<_from+_amount;i++){
		try
		{
			_queue.insert(i);
		}
		catch(IllegalMonitorStateException ex)
		{
			// TODO: Should we keep this?
			System.out.println("Deuce error at InsertWorker, thread " + tid);
		}
	}
	
	System.out.println("InsertWorker thread " + tid + " done");
}
}

class DeleteWorker implements Runnable{

IPriorityQueue _queue;


public DeleteWorker(IPriorityQueue queue){
	this._queue = queue;
}

public void run(){
	long tid = Thread.currentThread().getId();
	
	while(!_queue.isEmpty())	
	{
		int result;

		try
		{
			result = _queue.deleteMin();
			System.out.println(result); //TODO modify
		}
		catch(IllegalMonitorStateException ex)
		{
			// TODO: Should we keep this?
			System.err.println("Deuce error at DeleteWorker, thread " + tid);
		}
	}
	
	System.out.println("DeleteWorker thread " + tid + " done");
}
}

