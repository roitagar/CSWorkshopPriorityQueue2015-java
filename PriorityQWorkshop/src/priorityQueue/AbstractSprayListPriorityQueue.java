package priorityQueue;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import priorityQueue.SprayListPriorityQueue.SprayListNode;

public abstract class AbstractSprayListPriorityQueue implements IPriorityQueueOld {

	protected  int _maxAllowedHeight;
	protected AtomicInteger _threads;
	protected AtomicInteger _size;
	
	public AbstractSprayListPriorityQueue(int maxAllowedHeight) {
		_maxAllowedHeight = maxAllowedHeight;
		_threads = new AtomicInteger(0);
	}
	
	/* Abstract Methods */
	
	/**
	 * Threads issues on insert:
	 * 1. Trying to insert a value that is being insert by another thread
	 * 2. Trying to insert a value that is being delete by another thread
	 * 3. 
	 */
	public abstract void insert(int value);
	
	
	protected abstract boolean remove(int value);
	
	
	protected abstract int spray(int H, int L, int D); //TODO: Make it actual with polymorphism
	
	
	public abstract boolean isEmpty();

	
	@Override
	public int deleteMin() {
		_threads.incrementAndGet();
		boolean retry = false;
		int result;
		long tid = Thread.currentThread().getId();
		do
		{
			int p = getNumberOfThreads();
			int H = (int) Math.log(p)/*+K*/;
			int L = (int) (/*M * */ Math.pow(Math.log(p),3));
			int D = 1; /* Math.max(1, log(log(p))) */
			result = spray(H,L,D);
			System.out.println("Thread " + tid + ": After spray got "+ result);
			if(result == Integer.MAX_VALUE)
			{
				if(isEmpty())
					return result;
				else
					retry = true;
			}
			else
			{
				retry = !remove(result);
				//(retry = true) means that another thread performed an action that affect the remove
				System.out.println("Thread " + tid + ": After remove " + result + " got retry="+ retry);
			}
		} while(retry);
		_threads.decrementAndGet();
		return result;
	}
	

	// Number of threads currently calling deleteMin
	protected int getNumberOfThreads() {
		return _threads.get();
	}
	
	protected int randomStep(int max) {
		return ThreadLocalRandom.current().nextInt(max+1);
	}
	
	protected int randomLevel() {
		return randomStep(_maxAllowedHeight);
	}

	@Override
	public int size() {
		return _size.get();
	}

}
