package priorityQueue;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class GrainedLockSprayListPriorityQueue extends SprayListPriorityQueue {

	
	public GrainedLockSprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
	}


	@Override
	protected boolean canInsertBetween(SprayListNode pred, SprayListNode succ, int level)
	{
		return !pred.isMarked() && !succ.isMarked() && (pred.next[level] == succ);
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
