package priorityQueue.news;

import org.deuce.Atomic;

public class TMSprayListPriorityQueue extends SeqSprayListPriorityQueue{

	public TMSprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
	}
	
	@Override
	@Atomic
	public boolean insert(int value) { 
		return super.insert(value);
	}
	
	@Atomic
	@Override
	public int deleteMin() {
		return super.deleteMin();	
	}

}
