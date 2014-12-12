package priorityQueue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GrainedLockSprayListPriorityQueue extends SprayListPriorityQueue {

	AtomicInteger _threads;
	Random _random; // TODO: Replace with a concurrent version
	
	public GrainedLockSprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
		_threads = new AtomicInteger(0);
		_random = new Random();
	}

	@Override
	protected void startInsert() {
		_threads.incrementAndGet();
		
	}

	@Override
	protected void endInsert() {
		_threads.decrementAndGet();
		
	}

	@Override
	protected int randomLevel() {
		return randomStep(_maxAllowedHeight);
	}

	@Override
	protected void startDeleteMin() {
		_threads.incrementAndGet();
	}

	@Override
	protected void endDeleteMin() {
		_threads.decrementAndGet();
	}

	@Override
	protected int getNumberOfThreads() {
		// TODO Auto-generated method stub
		return _threads.get();
	}

	@Override
	protected int randomStep(int max) {
		// TODO Auto-generated method stub
		return _random.nextInt(max+1);
	}

	@Override
	protected boolean canInsertBetween(SprayListNode pred, SprayListNode succ, int level)
	{
		return !pred.isMarked() && !succ.isMarked() && pred.next[level].getReference()==succ;
	}

	@Override
	protected void lockNode(SprayListNode node) {
		node.lock.lock();
		
	}

	@Override
	protected void unlockNode(SprayListNode node) {
		node.lock.unlock();
	}

	@Override
	protected boolean readyToBeDeleted(SprayListNode node) {
		return node.isFullyLinked() && !node.isMarked();
	}

}
