package priorityQueue.news;

public interface IPriorityQueue {

	void insert(int value);
	int deleteMin();
	boolean isEmpty();
	int size();
	
}
