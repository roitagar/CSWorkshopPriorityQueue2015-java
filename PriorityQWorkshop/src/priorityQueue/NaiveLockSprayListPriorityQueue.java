package priorityQueue;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class NaiveLockSprayListPriorityQueue extends SprayListPriorityQueue {

	Random _random;
	private final ReentrantLock _lock;
	
	public NaiveLockSprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
		_random = new Random();
		_lock = new ReentrantLock();
	}
	
	@Override
	public void insert(int value) {
		_lock.lock();
		super.insert(value);
		_lock.unlock();
	}

	
	@Override
	public int deleteMin() {
		int ret;
		_lock.lock();
		ret = super.deleteMin();
		_lock.unlock();
		return ret;
	}



	@Override
	protected boolean canInsertBetween(SprayListNode pred, SprayListNode succ,
			int level) {
		return true;
	}

	@Override
	protected void lockNode(SprayListNode node) {
	}

	@Override
	protected void unlockNode(SprayListNode node) {
	}

	@Override
	protected boolean readyToBeDeleted(SprayListNode victim) {
		return true;
	}

}
