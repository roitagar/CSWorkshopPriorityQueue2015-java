package priorityQueue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.Atomic;

public class TransactioalMemorySprayListPriorityQueue extends SprayListPriorityQueue {

	Random _random;
	AtomicInteger _threads;
	
	public TransactioalMemorySprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
		_threads = new AtomicInteger(0);
		_random = new Random();
	}

	@Override
	@Atomic
	public void insert(int value) { 
		_threads.incrementAndGet();
		super.insert(value);
	}
	
	@Override
	protected void startInsert() {
	}

	@Override
	protected void endInsert() {
		_threads.decrementAndGet();
		
	}

	@Override
	protected int randomLevel() {
		return randomStep(_maxAllowedHeight);
	}
	
	@Atomic
	@Override
	public int deleteMin() {
		_threads.incrementAndGet();
		return super.deleteMin();
		
	}
	
	@Override
	protected void startDeleteMin() {
		
	}

	@Override
	protected void endDeleteMin() {
		_threads.decrementAndGet();
	}

	@Override
	protected int getNumberOfThreads() {
		return _threads.get();
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
