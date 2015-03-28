package priorityQueue.news;

import java.util.concurrent.PriorityBlockingQueue;

public class JavaConcurrentPriorityQueue implements IPriorityQueue {

	private final PriorityBlockingQueue<Integer> _queue;
	
	public JavaConcurrentPriorityQueue() {
		_queue = new PriorityBlockingQueue<Integer>();
	}
	@Override
	public boolean insert(int value) {
		return _queue.add(value);
	}

	@Override
	public int deleteMin() {
		Integer ret = _queue.poll();
		if (ret == null) {
			ret = Integer.MAX_VALUE;
		}
		return ret;
	}

	@Override
	public boolean isEmpty() {
		return _queue.isEmpty();
	}


}
