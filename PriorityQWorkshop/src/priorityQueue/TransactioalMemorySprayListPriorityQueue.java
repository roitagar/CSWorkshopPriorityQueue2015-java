package priorityQueue;

import java.util.Random;

import org.deuce.Atomic;

public class TransactioalMemorySprayListPriorityQueue extends SprayListPriorityQueue {

	Random _random;
	int _threads;
	
	public TransactioalMemorySprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
		_random = new Random();
	}

	@Override
	@Atomic
	protected void startInsert() {
		_threads++;
	}

	@Override
	protected void endInsert() {
		_threads--;	
		
	}

	@Override
	protected int randomLevel() {
		return randomStep(_maxAllowedHeight);
	}

	@Override
	@Atomic
	protected void startDeleteMin() {
		_threads++;
	}

	@Override
	protected void endDeleteMin() {
		_threads--;
	}

	@Override
	protected int getNumberOfThreads() {
		return _threads;
	}

	@Override
	protected int randomStep(int max) {
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
