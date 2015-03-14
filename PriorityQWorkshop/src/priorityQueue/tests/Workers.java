package priorityQueue.tests;

import java.util.ArrayList;

import priorityQueue.utils.*;
import priorityQueue.news.IPriorityQueue;

abstract class InsertWorker implements Runnable{

	protected int _totalPackets;
	int totalPackets()
	{
		return _totalPackets;
	}
	
}

/**
 * 
 * @author adamelimelech
 *
 */
abstract class GradedWorkerBase implements Runnable{
	IPriorityQueue _queue;
	protected ArrayList<Integer> _values;

	public GradedWorkerBase(IPriorityQueue queue) {
		_queue = queue;
		_values = new ArrayList<Integer>();
	}
	
	public int deleteMin(){
		int result = _queue.deleteMin();
		if(result!=Integer.MAX_VALUE){
			_values.add(result);
		}
		return result;
	}

	/**
	 * Counting #inversions of the _value array 
	 * 
	 * Each delete worker creates an array, grade = 0 -> optimum 
	 * @return
	 */
	public int getGrade(){
		int grade=0;
		
		//check with bubble sort the goodness of the del. min
		for(int i=0;i<_values.size()-1;i++){
			for(int j=i;j<_values.size();j++){
				if(_values.get(i)>_values.get(j)){
					grade++;
				}
			}
		}

		return grade;
	}

	public int totalPackets()
	{
		return _values.size();
	}
}

class SimpleInsertWorker extends InsertWorker {

	IPriorityQueue _queue;
	int _amount;
	int _from;

	public SimpleInsertWorker(IPriorityQueue queue, int from, int amount) {
		this._queue = queue;
		this._from = from;
		this._amount = amount;
		_totalPackets = 0;
	}

	@Override
	public void run() {
		for (int i = _from; i < _from + _amount; i++) {
			_queue.insert(i);
			_totalPackets++;
		}
	}
}

class SimpleDeleteWorker extends GradedWorkerBase implements Runnable {

	IPriorityQueue _queue;

	public SimpleDeleteWorker(IPriorityQueue queue) {
		super(queue);
		this._queue = queue;
	}

	public void run() {
		while (!_queue.isEmpty()) {
			int result;

			result = deleteMin();
		}
	}
}

interface INumberGenerator 
{
	int getNext();
}



class AdvancedInsertAndDelete extends GradedWorkerBase implements Runnable {
	final IPriorityQueue _queue;
	final int _runs;
	final int _highest;

	public AdvancedInsertAndDelete(
			IPriorityQueue queue, int runs, int highest)
	{
		super(queue);
		_queue = queue;
		_runs=runs;
		_highest = highest;
	}

	@Override
	public void run()
	{
		int counter=0;
		int result;
		int value;
		while( _runs > counter)
		{
			result = deleteMin();
			value = result + _highest;
			_queue.insert(value);
			counter++;
		}
	}
	/**
	 * Ranking the worker - grade = 0 -> optimum
	 * Each delete min with result which is higher then the _highest increasing grade by 1 (bad point)
	 * @return
	 */
	@Override
	public int getGrade(){
		int grade=0;
		for(int value : _values){
			if(value > _highest){
				grade++;
			}
		}
		return grade;

	}

}


class AdvancedInsertWorker extends InsertWorker {
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
		_totalPackets = 0;
	}

	@Override
	public void run()
	{
		while( !_done.value )
		{
			int value = _generator.getNext();
			if(value>0){
				_queue.insert(value);
				_totalPackets++;
			}
		}
	}
}


class AdvancedInsertWorkerUntilValue extends InsertWorker {
	final INumberGenerator _generator;
	final IPriorityQueue _queue;
	final int _finishWithValue;

	public AdvancedInsertWorkerUntilValue(
			INumberGenerator generator,
			IPriorityQueue queue,
			int finishWithValue)
	{
		_generator = generator;
		_queue = queue;
		_totalPackets = 0;
		_finishWithValue = finishWithValue;
	}

	@Override
	public void run()
	{

		boolean reallyDone = false;
		while( !reallyDone ) {
			reallyDone = true; // if we got the last value we need to finish
			int value = _generator.getNext();

			if(value!=_finishWithValue)
			{
				// if the value is not as expected, need to keep going
				reallyDone = false;
				_queue.insert(value);
				_totalPackets++;
			}

		}

	}
}



class AdvancedDeleteWorker extends GradedWorkerBase implements Runnable
{
	final PaddedPrimitiveNonVolatile<Boolean> _done;
	final IPriorityQueue _queue;

	public AdvancedDeleteWorker(
			PaddedPrimitiveNonVolatile<Boolean> done,
			IPriorityQueue queue)
	{
		super(queue);
		_done = done;
		_queue = queue;
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

			result = deleteMin();

			if(!_queue.isEmpty())
			{
				// if the queue is not empty, need to keep going
				reallyDone = false;
			}
		}
	}
}
class AdvancedDeleteWorkerWithoutEmptying extends GradedWorkerBase implements Runnable
{
	final PaddedPrimitiveNonVolatile<Boolean> _done;
	final IPriorityQueue _queue;

	public AdvancedDeleteWorkerWithoutEmptying(
			PaddedPrimitiveNonVolatile<Boolean> done,
			IPriorityQueue queue)
	{
		super(queue);
		_done = done;
		_queue = queue;
	}

	@Override
	public void run()
	{
		// if done is triggered and queue is not empty, reallyDone would keep the loop running.
		// if done is triggered and queue is empty, the loop will not set reallyDone back to false, and exit.
		boolean done = false;
		while( !done ) {
			done = _done.value; // if done is marked, we might need to finish
			int result;

			result = deleteMin();
		}
	}
}