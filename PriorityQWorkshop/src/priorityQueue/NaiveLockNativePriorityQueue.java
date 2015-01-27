package priorityQueue;

import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

public class NaiveLockNativePriorityQueue implements IPriorityQueueOld {
	private final ReentrantLock _lock = new ReentrantLock();
	
	private PriorityQueue<Integer> _queue;
	
	public NaiveLockNativePriorityQueue()
	{
		_queue = new PriorityQueue<Integer>();
	}
	
	@Override
	public void insert(int value) {
		// TODO Auto-generated method stub
		_lock.lock();
		_queue.add(value);
		_lock.unlock();
	}

	@Override
	public int deleteMin() {
		_lock.lock();
		int value = _queue.poll();
		_lock.unlock();
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return _queue.isEmpty();
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return _queue.size();
	}

}
