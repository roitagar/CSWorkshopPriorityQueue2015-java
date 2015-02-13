package priorityQueue.news;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class CoolSprayListPriorityQueue implements IPriorityQueue {
	
	protected final int _maxAllowedHeight;
	protected AtomicInteger _threads;
	protected AtomicInteger _size;
	protected CoolSprayListNode _head;
	protected CoolSprayListNode _tail;
	protected NodesEliminationArray _elimArray;
	private ReadWriteLock _lock1;
	private ReadWriteLock _lock2;
	volatile Integer highetNodeKey;
	
	public CoolSprayListPriorityQueue(int maxAllowedHeight) {
		_maxAllowedHeight = maxAllowedHeight;
		_threads = new AtomicInteger(0);
		_head = new CoolSprayListNode(Integer.MIN_VALUE, maxAllowedHeight);
		_tail = new CoolSprayListNode(Integer.MAX_VALUE, maxAllowedHeight);
		_lock1 = new ReentrantReadWriteLock(true);
		highetNodeKey = null;
		
		for(int i=0;i<=_maxAllowedHeight;i++)
		{
			_head.next[i] = new AtomicMarkableReference<CoolSprayListNode>(_tail, false);
			_tail.next[i] = new AtomicMarkableReference<CoolSprayListNode>(null, false);
		}
	}
	
	/* find can find also marked nodes */
	protected NodeStatus find(int value, CoolSprayListNode[] preds, CoolSprayListNode[] succs)
	{
		CoolSprayListNode pred =_head; 
		CoolSprayListNode curr = null;
		/* Traverse each level up do bottom */
		for (int level = _maxAllowedHeight; level >= 0; level--) {
			curr = pred.next[level].getReference();
			
			while (curr.value  < value){
				pred = curr;
				curr = pred.next[level].getReference();

			}
			/* update preds and succss */
			preds[level] = pred;
			succs[level] = curr;
		}
		if (curr.value == value) {
			if (curr.isMarked()){
				return NodeStatus.MARKED;
			}
			return NodeStatus.FOUND;
		}
		return NodeStatus.NOT_FOUND;
	}
	
	@Override
	public boolean insert(int value) {
		_threads.getAndIncrement();
		int topLevel = serviceClass.randomLevel(_maxAllowedHeight);
		CoolSprayListNode[] preds = new CoolSprayListNode[_maxAllowedHeight+1];
		CoolSprayListNode[] succs = new CoolSprayListNode[_maxAllowedHeight+1];
		_lock1.readLock().lock();
		/*in this case we have to wait */
		if (highetNodeKey != null && value <highetNodeKey){
			_lock2.readLock().lock();
			_lock2.readLock().unlock();
		}
		try {
		while(true)
		{
			
			
			NodeStatus status = find(value, preds, succs);

			if(status == NodeStatus.FOUND) {
				/*linearization point of unsuccessful insertion */
				_threads.getAndDecrement();
				return false;
			}
			
			else if (status == NodeStatus.MARKED) {
			/*Node is physically exist and only logically deleted - unmarked it */
				boolean IRevivedIt = succs[0].unmark();
				_threads.getAndDecrement();
				return IRevivedIt;
			}

			/*The item is not in the set (both physically and logically - so add it */
			else 
			{

				/* create a new node */
				CoolSprayListNode newNode = new CoolSprayListNode(value, topLevel);
				
				/* connect level 0 of newNode to it successor */
				newNode.next[0].set(succs[0], false);
				CoolSprayListNode pred = preds[0];
				CoolSprayListNode succ = succs[0];
				
				/*try to connect the bottom level - this is a linearization point of successful insertion*/
				if(!pred.next[0].compareAndSet(succ, newNode, false, false)) {
					continue;
				}
				/* now when level 0 is connected - connect the other predecessors from the other levels to the new node.
				 * If you fail to connect a specific level - find again - means - prepare new arrays of preds and succs,
				 * and continue from the level you failed.
				 * If you failed because of the mark - that means you try to insert the node next to the last node in the
				 * deletion list - and it will work just after this node will be removed.
				 */
				for (int level= 1; level <= topLevel; level++) {
					while (true) {
						pred = preds[level];
						succ = succs[level];
						//TODO: Check if it OK
						newNode.next[level].set(succ, false); //connect the new node to the next succ if it was changed
						
						if (pred.next[level].compareAndSet(succ, newNode, false, false)){
							break; 
						}	
						find(value, preds, succs);	 
					}
				}
				_threads.getAndDecrement();
				return true;
			}

			}

		}
		finally {
			_lock1.readLock().unlock();
		}
	}
	
	protected boolean clean() {
		if (_lock1.writeLock().tryLock()) {
			try {
				/*Determine the max number of Healthy element you want to traverse */
				int numOfHealtyNodes = 5; //TODO: Determine it for a variable
				
				
				

				/*Create an Elimination Array in this size */
				_elimArray = new NodesEliminationArray(numOfHealtyNodes);
				
				/*Traverse the list in the bottom level look for healthy element, and find an optimal group */
				int foundHealthyNodes = 0;
				int maxLevelFound = 0;
				CoolSprayListNode firstNode = _head.next[0].getReference();
				CoolSprayListNode curr = firstNode;
				CoolSprayListNode highest = null;
				while (foundHealthyNodes < numOfHealtyNodes) {
					if (!curr.isMarked()) {
						foundHealthyNodes +=1;
					}
					/*find the last highest node in the ragne */
					if (maxLevelFound <= curr.topLevel()) {
						highest = curr;
						maxLevelFound = curr.topLevel();
					}
					curr = curr.next[0].getReference();
				}

				/*Now you have a range that you want to delete  mark the highest node's markable reference,
				 * so other threads cannot add a node after it */
				//TODO: to to top or vice versa?
				for (int level= 0; level <= highest.topLevel(); level++) {
					while (true) {
						CoolSprayListNode succ = highest.next[level].getReference();
						if (highest.next[level].attemptMark(succ, true)) {
							break;
						}
					}
				}
				/*Now - nobody can connect a node after the highest node - in the deletion list - connect the head*/
				for (int level= 0; level <= _maxAllowedHeight; level++) {
					_head.next[level].set(highest.next[level].getReference(), false);
				}

				/*Now  - logically delete each alive node in the group deleted and add it to the elimination array */
				curr = firstNode;
				while (curr != highest){
					if (!curr.isMarked()) {
						/*If I marked it - add it to the elimination array*/
						if (curr.mark()) {
							_elimArray.addNode(curr);
						}
					}
					curr = curr.next[0].getReference();
				}
			}

			finally{
				_lock1.writeLock().unlock();
			}
		}

		return false;
	}
	
	/*This remove is wait-free and only logically remove the item */
	protected boolean remove(int value) {
		CoolSprayListNode[] preds = new CoolSprayListNode[_maxAllowedHeight+1];
		CoolSprayListNode[] succs = new CoolSprayListNode[_maxAllowedHeight+1];
		NodeStatus status = find(value, preds, succs);
		if (status == NodeStatus.NOT_FOUND || status == NodeStatus.MARKED) {
			return false;
		} 

		/*The node exists in the set */

		else {
			/*remove it logically and check if it was marked before*/
			CoolSprayListNode nodeToRemove = succs[0];	
			boolean iMarkedIt = nodeToRemove.mark();
			return iMarkedIt;
		}
	}

	@Override
	public int deleteMin() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
	
	protected class CoolSprayListNode {
		public int value;
		/*The mark in the AtomicMarkableReference is used to the phyiscal deleteion of the last node in the deletion group */ 
		public AtomicMarkableReference<CoolSprayListNode>[] next;
		private AtomicBoolean _marked;
		
		public CoolSprayListNode(int value, int height) {
			this.value = value;
			this.next =  (AtomicMarkableReference<CoolSprayListNode>[]) new AtomicMarkableReference[height+1];
			for (int i = 0; i < next.length; i++) {
				next[i] = new AtomicMarkableReference<CoolSprayListNode>(null,false);
			}
			_marked.set(false);
		}
		
		public int topLevel()
		{
			return next.length-1;
		}
		
		/* try to mark and return if succeed */
		public boolean mark() {
			return _marked.compareAndSet(false, true);
		}
		
		/* try to unmark and return if succeed */
		public boolean unmark() {
			return _marked.compareAndSet(true, false);
		}
		
		public boolean isMarked() {
			return _marked.get();
		}
	}
	
	private class NodesEliminationArray {
		private CoolSprayListNode[] arr;
		private AtomicInteger nextNode;
		
		public NodesEliminationArray(int size) {
			arr = new CoolSprayListNode[size];
			nextNode.set(0);
		}
		public void addNode(CoolSprayListNode node) {
			int i = nextNode.getAndIncrement();
			arr[i] = node;
		}
		
		public CoolSprayListNode getNode() {
			int i = nextNode.getAndDecrement();
			if (i<0) {
				return null;
			}
			return arr[i];
		}
		
		public boolean hasNodes(){
			return nextNode.get() >= 0;
		}
	}
	
	private enum NodeStatus {
		FOUND,
		MARKED,
		NOT_FOUND
	}

}
