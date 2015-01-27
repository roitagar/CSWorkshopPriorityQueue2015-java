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
	for(int i=_from;i<_from+_amount;i++){
		_queue.insert(i);
	}
}
}

class DeleteWorker implements Runnable{

IPriorityQueue _queue;


public DeleteWorker(IPriorityQueue queue){
	this._queue = queue;
}

public void run(){
	while(!_queue.isEmpty())	
	{
		int result;

		result = _queue.deleteMin();
		System.out.println(result); //TODO modify
	}
}
}

