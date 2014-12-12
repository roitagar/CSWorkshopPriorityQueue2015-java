package priorityQueue;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class NaiveLockSprayListPriorityQueue extends SprayListPriorityQueue {

	int _threads;
	Random _random;
	private final ReentrantLock _lock;
	
	public NaiveLockSprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
		_threads = 0;
		_random = new Random();
		_lock = new ReentrantLock();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void startInsert() {
		// TODO Auto-generated method stub
		_lock.lock();
		_threads++;
		
	}

	@Override
	protected void endInsert() {
		// TODO Auto-generated method stub
		_threads--;
		_lock.unlock();
	}

	@Override
	protected int randomLevel() {
		return randomStep(_maxAllowedHeight);
		// TODO Auto-generated method stub
//		return 0;
	}

	@Override
	protected void startDeleteMin() {
		// TODO Auto-generated method stub
		_lock.lock();
		_threads++;
	}

	@Override
	protected void endDeleteMin() {
		// TODO Auto-generated method stub
		_threads--;
		_lock.unlock();
	}

	@Override
	protected int getNumberOfThreads() {
		// TODO Auto-generated method stub
		return _threads;
	}

	@Override
	protected int randomStep(int max) {
		// TODO Auto-generated method stub
		return _random.nextInt(max+1);
	}

	@Override
	protected boolean canInsertBetween(SprayListNode pred, SprayListNode succ,
			int level) {
		return true;
	}

	@Override
	protected void lockNode(SprayListNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void unlockNode(SprayListNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean readyToBeDeleted(SprayListNode victim) {
		return true;
	}

}
