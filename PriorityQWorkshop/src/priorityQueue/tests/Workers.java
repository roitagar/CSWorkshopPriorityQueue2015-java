package priorityQueue.tests;

import priorityQueue.utils.*;
import priorityQueue.news.IPriorityQueue;

class SimpleInsertWorker implements Runnable {

	IPriorityQueue _queue;
	int _amount;
	int _from;

	public SimpleInsertWorker(IPriorityQueue queue, int from, int amount) {
		this._queue = queue;
		this._from = from;
		this._amount = amount;
	}

	public void run() {
		for (int i = _from; i < _from + _amount; i++) {
			_queue.insert(i);
		}
	}
}

class SimpleDeleteWorker implements Runnable {

	IPriorityQueue _queue;

	public SimpleDeleteWorker(IPriorityQueue queue) {
		this._queue = queue;
	}

	public void run() {
		while (!_queue.isEmpty()) {
			int result;

			result = _queue.deleteMin();
			System.out.println(result); // TODO modify
		}
	}
}

interface INumberGenerator 
{
	int getNext();
}

class AdvancedInsertWorker implements Runnable {
	final PaddedPrimitiveNonVolatile<Boolean> _done;
	final INumberGenerator _generator;
	final IPriorityQueue _queue;

	public AdvancedInsertWorker(
			PaddedPrimitiveNonVolatile<Boolean> done,
			INumberGenerator generator,
			IPriorityQueue queue)
	{
		_done = done;
		_generator = generator;
		_queue = queue;
	}
	
	@Override
	public void run()
	{
		while( !_done.value )
		{
			int value = _generator.getNext();
			_queue.insert(value);
		}
	}
}

class AdvancedDeleteWorker implements Runnable
{
	final PaddedPrimitiveNonVolatile<Boolean> _done;
	final IPriorityQueue _queue;
	public long _totalPackets;

	public AdvancedDeleteWorker(
			PaddedPrimitiveNonVolatile<Boolean> done,
			IPriorityQueue queue)
	{
		_done = done;
		_queue = queue;
		_totalPackets = 0;
	}
	
	@Override
	public void run()
	{
		// if done is triggered and queue is not empty, reallyDone would keep the loop running.
		// if done is triggered and queue is empty, the loop will not set reallyDone back to false, and exit.
		boolean reallyDone = false;
		while( !reallyDone ) {
			reallyDone = _done.value; // if done is marked, we might need to finish
			int result;

			result = _queue.deleteMin();
			
			if(result != Integer.MAX_VALUE)
			{
				System.out.println(result); // TODO modify

				// Count valid dequeued elements
				_totalPackets++;
			}
			
			if(!_queue.isEmpty())
			{
				// if the queue is not empty, need to keep going
				reallyDone = false;
			}
		}
	}
}