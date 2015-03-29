package priorityQueue.news;

import java.io.ObjectInputStream.GetField;
import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.Atomic;

public class TMSprayListPriorityQueue extends SeqSprayListPriorityQueue{

	AtomicInteger _threadCount;
	public TMSprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
		_threadCount = new AtomicInteger(0);
	}
	
	@Override
	public boolean insert(int value) {
		try
		{
			_threadCount.getAndIncrement();
			return atomicInsert(value);
		}
		finally
		{
			_threadCount.getAndDecrement();
		}
	}
	
	@Atomic
	private boolean atomicInsert(int value) {
		return super.insert(value);
	}
	
	@Override
	public int deleteMin() {
		try
		{
			_threadCount.getAndIncrement();
			return atomicDeleteMin();
		}
		finally
		{
			_threadCount.getAndDecrement();
		}
	}

	@Atomic
	private int atomicDeleteMin() {
		return super.deleteMin();	
	}
	
	@Override
	protected int getThreadCount() {
		return _threadCount.get();
	}

}
