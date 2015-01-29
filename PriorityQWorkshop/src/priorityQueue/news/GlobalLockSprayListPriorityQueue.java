package priorityQueue.news;

import java.util.concurrent.locks.ReentrantLock;

public class GlobalLockSprayListPriorityQueue extends SeqSprayListPriorityQueue{
	/*This is a global Lock that used in insert and deleteMin methods
	 * isEmpty() and getSize() are wait-free*/
	
	ReentrantLock _lock;
	
	public GlobalLockSprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
		_lock = new ReentrantLock();	
	}
	
	@Override
	public boolean insert(int value) {
		boolean ret = false;
		try{
			_lock.lock();
			ret = super.insert(value);
		}
		finally {
			_lock.unlock();
		}
		return ret;
	}
	@Override
	public int deleteMin() {
		int ret;
		try {
			_lock.lock();
			ret = super.deleteMin();
		}
		finally {
			_lock.unlock();
		}
	return ret;	
	}
	

}
