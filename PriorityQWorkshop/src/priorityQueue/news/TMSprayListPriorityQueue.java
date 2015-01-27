package priorityQueue.news;

import org.deuce.Atomic;

public class TMSprayListPriorityQueue extends SeqSprayListPriorityQueue{

	public TMSprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
	}
	
	@Override
	@Atomic
	public void insert(int value) { 
		super.insert(value);
	}
	
	@Atomic
	@Override
	public int deleteMin() {
		return super.deleteMin();	
	}

}
