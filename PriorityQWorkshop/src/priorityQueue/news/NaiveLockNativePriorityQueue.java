package priorityQueue.news;

import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

public class NaiveLockNativePriorityQueue implements IPriorityQueue {
	private final ReentrantLock _lock = new ReentrantLock();
	
	private PriorityQueue<Integer> _queue;
	
	public NaiveLockNativePriorityQueue()
	{
		_queue = new PriorityQueue<Integer>();
	}
	
	@Override
	public boolean insert(int value) {
		boolean ret = false;
		_lock.lock();
		try {
			ret = _queue.add(value);
		}
		finally {
			_lock.unlock();
		}
		return ret;	
	}

	@Override
	public int deleteMin() {
		int ret = 0;
		_lock.lock();
		try {
			ret = _queue.poll();
		}
		finally {
			_lock.unlock();
		}
		return ret;
	}

	@Override
	public boolean isEmpty() {
		return _queue.isEmpty();
	}

}
