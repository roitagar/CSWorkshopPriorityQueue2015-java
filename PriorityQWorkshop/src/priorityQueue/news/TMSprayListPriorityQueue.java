package priorityQueue.news;

import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.Atomic;

public class TMSprayListPriorityQueue extends SeqSprayListPriorityQueue{

	AtomicInteger _threadCount;
	boolean _useThreadCounter;
	public TMSprayListPriorityQueue(int maxAllowedHeight, boolean useThreadCounter) {
		super(maxAllowedHeight);
		_threadCount = new AtomicInteger(0);
		_useThreadCounter = useThreadCounter; 
	}
	
	@Override
	public boolean insert(int value) {
		try
		{
			if(_useThreadCounter) _threadCount.getAndIncrement();
			return atomicInsert(value);
		}
		finally
		{
			if(_useThreadCounter) _threadCount.getAndDecrement();
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
			if(_useThreadCounter) _threadCount.getAndIncrement();
			return atomicDeleteMin();
		}
		finally
		{
			if(_useThreadCounter) _threadCount.getAndDecrement();
		}
	}

	@Atomic
	private int atomicDeleteMin() {
		return super.deleteMin();	
	}
	
	@Override
	protected int getThreadCount() {
		return _useThreadCounter ? _threadCount.get() : 1;
	}
}
