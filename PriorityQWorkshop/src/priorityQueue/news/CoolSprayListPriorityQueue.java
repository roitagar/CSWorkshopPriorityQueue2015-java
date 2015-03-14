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
	protected volatile NodesEliminationArray _elimArray;
	private ReadWriteLock _lock1; // during the entire cleanup - allows only one cleaner
	private ReadWriteLock _lock2; // during delete-group selection - blocks all inserters
	private ReadWriteLock _lock3; // during delete-group disconnection - blocks low inserters
	volatile Integer highetNodeKey;
	
	public CoolSprayListPriorityQueue(int maxAllowedHeight) {
		_maxAllowedHeight = maxAllowedHeight;
		_threads = new AtomicInteger(0);
		_head = new CoolSprayListNode(Integer.MIN_VALUE, maxAllowedHeight);
		_tail = new CoolSprayListNode(Integer.MAX_VALUE, maxAllowedHeight);
		_elimArray = new NodesEliminationArray(0);
		_lock1 = new ReentrantReadWriteLock(true);
		_lock2 = new ReentrantReadWriteLock(true);
		_lock3 = new ReentrantReadWriteLock(true);
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
		boolean crap = false;
		CoolSprayListNode[] preds = new CoolSprayListNode[_maxAllowedHeight+1];
		CoolSprayListNode[] succs = new CoolSprayListNode[_maxAllowedHeight+1];

		// Don't interfere with deciding a delete-group
		_lock2.readLock().lock();
		/*in this case we have to wait */
		if (highetNodeKey != null && value <highetNodeKey){
			crap = true;
			// Don't interfere with disconnecting a delete-group
			_lock3.readLock().lock();

			// TODO: instead of waiting, join the elimination?
		}

		// TODO: check if there are elimination items smaller than my item? maybe after inserting?

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
			if(crap)
			{
				_lock3.readLock().unlock();
			}
			_lock2.readLock().unlock();
		}
	}
	
	protected boolean clean() {
		// Allow only a single cleaner, but don't block
		if (_lock1.writeLock().tryLock()) {
			try {
				
				// Coherency check:
				if(_elimArray.hasNodes())
				{
					// Someone else performed cleanup and I missed it, go back to empty the elimination array
					return false;
				}
				
				// Spin until ongoing eliminations are done
				while(!_elimArray.completed()) { } // TODO: Move this to the end?
				
				// Block inserters
				_lock2.writeLock().lock();
				_lock3.writeLock().lock();
				
				/*Determine the max number of Healthy element you want to traverse */
				int numOfHealtyNodes = 5; //TODO: Determine it for a variable
				
				
				
				// TODO: Just hold the same one instance?
				/*Create an Elimination Array in this size */
				NodesEliminationArray newElimArray = new NodesEliminationArray(numOfHealtyNodes);
				
				/*Traverse the list in the bottom level look for healthy element, and find an optimal group */
				int foundHealthyNodes = 0;
				int maxLevelFound = 0;
				int len = 0; // TODO: Remove
				int actualLen = 0; // TODO: Remove
				CoolSprayListNode firstNode = _head.next[0].getReference();
				CoolSprayListNode curr = firstNode;
				CoolSprayListNode highest = curr;
				while (foundHealthyNodes < numOfHealtyNodes && curr != _tail) {
					len++;
					if (!curr.isMarked()) {
						foundHealthyNodes +=1;
					}
					/*find the last highest node in the ragne */
					if (maxLevelFound <= curr.topLevel()) {
						highest = curr;
						maxLevelFound = curr.topLevel();
						actualLen = len;
						
						// TODO: Compare live-dead element ratios?
					}
					curr = curr.next[0].getReference();
				}
				
				_lock2.writeLock().unlock(); // high-valued inserts can go on
				
				if(firstNode == _tail)
				{
					System.out.println("Clean none"); // TODO: Remove print
					// No nodes to remove
					_lock3.writeLock().unlock();
					return false;
				}
				
				System.out.println("Clean " + actualLen); // TODO: Remove print
				
				highetNodeKey = highest.value; // TODO: lock guard (finally)?
				
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
				for (int level= 0; level <= highest.topLevel(); level++) {
					_head.next[level].set(highest.next[level].getReference(), false);
				}
				
				_lock2.writeLock().lock(); // shortly block all inserters to change highetNodeKey, avoids race / NullPointerException
				highetNodeKey = null; // TODO: lock guard (finally)? is this stage good?

				_lock2.writeLock().unlock();
				_lock3.writeLock().unlock(); // no more messing around with the skiplist, all inserts can go on
				
				/*Now  - logically delete each alive node in the group deleted and add it to the elimination array */
				curr = firstNode;
				while (curr != highest){
					if (!curr.isMarked()) {
						/*If I marked it - add it to the elimination array*/
						if (curr.mark()) {
							newElimArray.addNode(curr);
						}
					}
					curr = curr.next[0].getReference();
				}
				
				// publish the ready elimination array
				_elimArray = newElimArray;

			}

			finally{
				_lock1.writeLock().unlock();
				// TODO: safely-unlock the other locks?
			}
			
			return true;
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

	// Finds a candidate for deleteMin
	protected int spray(int H, int L, int D)
	{
		CoolSprayListNode x = _head;
		int level = H;
//		int saturation = 0;
		while(level>=0)
		{
			int j = serviceClass.randomStep(L);
			/* 
			 * Don't stay on head
			 * Don't advance beyond tail
			 * Usually don't advance to tail
			 * Advance to tail when list is empty
			 */
			for(;(j>0 || x==_head) && x!=_tail && (x.next[level].getReference() != _tail || isEmpty());j--)
			{
				x = x.next[level].getReference();
				
				// TODO: skip longer over deleted nodes?
//				if(x.isMarked())
//				{
//					saturation++;
//				}
//				
//				if(saturation >=3)
//				{
//					saturation = 0;
//					j++;
//				}
				
				// TODO: inform deleted nodes encountered?
			}
			level-=D;
		}
		
		return x.value;
	}
	
	@Override
	public int deleteMin() {
		_threads.getAndIncrement();
		
		int result;
		
		while(true)
		{
			// First try to eliminate
			if(_elimArray.hasNodes())
			{
				CoolSprayListNode node = _elimArray.getNode();
				if(node != null)
				{
					System.out.println("Eliminated!"); // TODO: Remove print
					
					// Successful elimination
					result = node.value;
					break;
				}
			}
		
			// Attempt cleanup
			int tmp = serviceClass.randomStep(100);
			if(tmp < 3 /* TODO: cleanup conditions? */)
			{
				if(clean())
				{
					// Successful cleanup - now try to eliminate an item
					continue;
				}
			}
			
			// Normal spray
			int p = _threads.get();
			int H = Math.min((int) Math.log(p)/*+K*/, _maxAllowedHeight);
			int L = (int) (/*M * */ Math.pow(Math.log(p),3));
			int D = 1; /* Math.max(1, log(log(p))) */
			result = spray(H,L,D);
			
			if(result == Integer.MAX_VALUE)
			{
				// if we got tail's value, the list might be empty
				if(isEmpty())
					break;
			}
			else if(remove(result))
			{
				// Successful spray+remove
				break;
			}
		}
		
		_threads.getAndDecrement();
		
		return result;
	}

	@Override
	public boolean isEmpty() {
		if(_head.next[0].getReference() == _tail && !_elimArray.hasNodes() /* TODO: && !_cleanerrunning */)
		{
			return true;
		}
		
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
			
			_marked = new AtomicBoolean(false);
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
		private CoolSprayListNode[] arr; // TODO: int array?
		private AtomicInteger nextNode; // token allocator
		private AtomicInteger pendingCompletion; // prevents overriding before array access is done
		
		public NodesEliminationArray(int size) {
			arr = new CoolSprayListNode[size];
			nextNode = new AtomicInteger(0);
			pendingCompletion = new AtomicInteger(0);
		}
		public void addNode(CoolSprayListNode node) {
			int i = nextNode.getAndIncrement();
			arr[i] = node;
			pendingCompletion.getAndIncrement();
		}
		
		public CoolSprayListNode getNode() {
			int i = nextNode.getAndDecrement() - 1;
			if (i<0) {
				return null;
			}
			CoolSprayListNode result = arr[i];
			pendingCompletion.getAndDecrement();
			return result;
		}
		
		public boolean hasNodes(){
			return nextNode.get() > 0;
		}
		
		public boolean completed()
		{
			return pendingCompletion.get() == 0;
		}
	}
	
	private enum NodeStatus {
		FOUND,
		MARKED,
		NOT_FOUND
	}

}
