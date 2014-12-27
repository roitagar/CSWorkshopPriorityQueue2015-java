package priorityQueue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.Atomic;

import priorityQueue.SprayListPriorityQueue.SprayListNode;

public class TransactioalMemorySprayListPriorityQueue extends SprayListPriorityQueue {

	
	public TransactioalMemorySprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
	}

	@Override
	@Atomic(retries=64)
	public void insert(int value) { 
		super.insert(value);
	}

	@Override
	protected int randomLevel() {
		return randomStep(_maxAllowedHeight);
	}
	
	@Override
	@Atomic(retries=64)
	public int deleteMin() {
		return super.deleteMin();	
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
