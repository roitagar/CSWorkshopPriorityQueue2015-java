package priorityQueue.news;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class CoolSprayListPriorityQueue implements IPriorityQueue {

	protected final int _maxAllowedHeight;
	protected AtomicInteger _threads;
	protected AtomicInteger _liveItems;
	protected AtomicInteger _itemsInSkipList;
	protected CoolSprayListNode _head;
	protected CoolSprayListNode _tail;
	protected volatile NodesEliminationArray _elimArray;
	protected ReentrantLock _lock1; // during the entire cleanup - allows only one cleaner
	protected ReadWriteLock _lock2; // during delete-group selection - blocks all inserters
	protected ReadWriteLock _lock3; // during delete-group disconnection and construction - blocks low inserters
	volatile Integer highestNodeKey;

	public CoolSprayListPriorityQueue(int maxAllowedHeight, boolean fair) {
		_maxAllowedHeight = maxAllowedHeight;
		_threads = new AtomicInteger(0);
		_liveItems = new AtomicInteger(0);
		_itemsInSkipList = new AtomicInteger(0);
		_head = new CoolSprayListNode(Integer.MIN_VALUE, maxAllowedHeight);
		_tail = new CoolSprayListNode(Integer.MAX_VALUE, maxAllowedHeight);
		_elimArray = new NodesEliminationArray(0);
		_lock1 = new ReentrantLock();
		_lock2 = new ReentrantReadWriteLock(fair);
		_lock3 = new ReentrantReadWriteLock(fair);
		highestNodeKey = null;
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
			if (curr.isDeleted()){
				return NodeStatus.DELETED;
			}
			return NodeStatus.FOUND;
		}
		return NodeStatus.NOT_FOUND;
	}

	@Override
	public boolean insert(int value) {
		_threads.getAndIncrement();
		int topLevel = serviceClass.randomLevel(_maxAllowedHeight);
		boolean shouldReleaseLock3 = false;
		boolean reinsert = false; // determine insertion phase
		CoolSprayListNode[] preds = new CoolSprayListNode[_maxAllowedHeight+1];
		CoolSprayListNode[] succs = new CoolSprayListNode[_maxAllowedHeight+1];
		Integer temp = null;

		// Don't interfere with deciding a delete-group
		_lock2.readLock().lock();
		try {
			/*in this case we have to wait */
			temp = highestNodeKey; // local copy to avoid a race condition with assignment of null value
			if (temp != null && value <temp){
				shouldReleaseLock3 = true;
				// Don't interfere with disconnecting and building a delete-group
				_lock3.readLock().lock();

				// TODO: maybe instead of waiting, join the elimination array being built right now?
				//		 not sure it's a good idea, since it requires complex synchronization with the cleaner thread
			}

			/* create a new node */
			CoolSprayListNode newNode = new CoolSprayListNode(value, topLevel);

			// Insertion might have two phases:
			// 		1. insert the requested value
			//		2. reinsert an item from the elimination array back to the skiplist
			//
			// if there are elimination items smaller than my value, 
			// I need to eliminate them back to the list, to preserve linearizability
			//
			// every insert thread reinserts at most one item. the elimination array is depleted only if enough insert operations
			// are performed before deleteMin operations, otherwise deleteMin operations will deplete the elimination array before
			// linearizability is harmed

			while(true)
			{
				if (!reinsert && _elimArray.contains(value))
				{
					// Node exists, and is pending deletion in the elimination array
					return false;
				}

				NodeStatus status = find(value, preds, succs);
				if(!reinsert && status == NodeStatus.FOUND) {
					/* linearization point of unsuccessful insertion */
					return false;
				}

				if (!reinsert && status == NodeStatus.DELETED) {
					/* Node physically exists, and only logically deleted - unmark it */
					boolean IRevivedIt = succs[0].revive();

					if(IRevivedIt)
					{
						logInsertion(true);
					}

					return IRevivedIt;
				}

				if (reinsert && newNode.isDeleted())
				{
					// some deleteMin successfully eliminated this node, no need to reinsert it
					// original insertion was successful
					return true;
				}

				/* The item is not in the set (both physically and logically - so add it */

				/* connect level 0 of newNode to its successor */
				newNode.next[0].set(succs[0], false);
				CoolSprayListNode pred = preds[0];
				CoolSprayListNode succ = succs[0];

				/* try to connect the bottom level - this is a linearization point of successful insertion*/
				if(!pred.next[0].compareAndSet(succ, newNode, false, false)) {
					continue;
				}

				if(reinsert)
				{
					logReinsert();
				}
				else
				{
					logInsertion(false);
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

						// connect the new node to the next successor
						// Note: This action is repeated until connecting pred to newNode succeeds, unlike in the lock-free implementation
						//		 Although it seems to increase accesses due to retries, this implementation heavily reduces retries,
						//		 due to grouped removals, thus allowing us to maintain better correctness of the skiplist structure without
						//		 harming performance.
						newNode.next[level].set(succ, false);

						if (pred.next[level].compareAndSet(succ, newNode, false, false)){
							break; 
						}	
						find(value, preds, succs);
					}
				}
				if (reinsert) {
					// change node status from ALIVE_IN_ELIMINATION to ALIVE
					//linearization point for reinsert
					newNode.reinsert();
				}
				else
				{
					// successful insertion completed, now check if we need to help fix linearization by
					// reinserting a high-valued node from the elimination array to the skiplist 
					newNode = _elimArray.getNodeForReinsert(value);
					reinsert = (newNode != null);
					if(reinsert)
					{
						value = newNode.value;
						topLevel = newNode.topLevel();
						// repeat insertion with the reinsert node
						continue;
					}
				}

				// success
				return true;
			}
		}
		finally {
			if(shouldReleaseLock3)
			{
				_lock3.readLock().unlock();
			}
			_lock2.readLock().unlock();

			_threads.getAndDecrement();
		}
	}

	protected boolean clean() {

		// Allow only a single cleaner, but don't block
		if (!_lock1.tryLock())
		{
			return false;
		}
		int foundHealthyNodes;
		int maxLevelFound;
		int len;
		int actualLen;
		CoolSprayListNode firstNode;
		CoolSprayListNode curr;
		CoolSprayListNode highest;
		NodesEliminationArray newElimArray;
		try {

			// Coherence test:
			if(_elimArray.hasNodes())
			{
				// Someone else performed cleanup and I missed it, go back to empty the elimination array
				return false;
			}

			// Block inserters
			_lock2.writeLock().lock();
			_lock3.writeLock().lock();
			try { //for _lock3
				try{ //for _lock2
					/* Determine the max number of Healthy element you want to traverse */
					int p = _threads.get();
					p = p*(int)(Math.log(p)/Math.log(2)) + 1;
					int numOfHealtyNodes = p;
					/* Create an Elimination Array in this size */
					newElimArray = new NodesEliminationArray(numOfHealtyNodes);

					/* Traverse the list in the bottom level look for healthy element, and find an optimal group */
					foundHealthyNodes = 0;
					maxLevelFound = 0;
					len = 0;
					actualLen = 0;
					firstNode = _head.next[0].getReference();
					curr = firstNode;
					highest = curr;
					while (foundHealthyNodes < numOfHealtyNodes && curr != _tail) {
						len++;
						if (!curr.isDeleted()) {
							foundHealthyNodes++;
						}
						/* find the last highest node in the ragne */
						if (maxLevelFound <= curr.topLevel()) {
							highest = curr;
							maxLevelFound = curr.topLevel();
							actualLen = len;

							// TODO: Compare live-dead element ratios?
						}
						curr = curr.next[0].getReference();
					}

					highestNodeKey = highest.value;

					if(firstNode == _tail)
					{
						// No nodes to remove
						highestNodeKey = null;
						return false;
					}
				}
				finally {
					_lock2.writeLock().unlock(); // high-valued inserts can go on	
				}


				// Now you have a range that you want to delete. mark the highest node's markable reference in all levels,
				// so other threads cannot add a node after it.
				// Starting the marking process from the bottom, blocks new inserts from interrupting.
				for (int level=0; level <= highest.topLevel(); level++) {
					while (true) {
						CoolSprayListNode succ = highest.next[level].getReference();
						if (highest.next[level].attemptMark(succ, true)) {
							break;
						}
					}
				}

				/* Now - nobody can connect a node after the highest node - in the deletion list - connect the head*/
				for (int level=0; level <= highest.topLevel(); level++) {
					_head.next[level].set(highest.next[level].getReference(), false);
				}
				/* Now  - mark each alive node in the group as belong to elimination array and add it to the elimination array */
				curr = firstNode;
				boolean done = false;
				while (!done){
					if(curr == highest) // last node to process
						done = true;

					if (!curr.isDeleted()) {
						// Try to mark it as node of the eliminataion array.
						if (curr.markAsEliminationNode()) {
							newElimArray.addNode(curr);
						}
					}
					curr = curr.next[0].getReference();
				}

				logCleanup(actualLen);

				// Spin until ongoing eliminations are done
				while(!_elimArray.completed()) { }

				// publish the ready elimination array
				_elimArray = newElimArray;

				highestNodeKey = null;	
			}
			finally {
				// now after the elimination array is ready, also the lower inserters can go.
				_lock3.writeLock().unlock(); 
			}
		}
		finally{

			_lock1.unlock();
		}

		return true;
	}

	/* This remove is wait-free and only logically removes the item */
	protected boolean remove(int value) {
		CoolSprayListNode[] preds = new CoolSprayListNode[_maxAllowedHeight+1];
		CoolSprayListNode[] succs = new CoolSprayListNode[_maxAllowedHeight+1];
		NodeStatus status = find(value, preds, succs);
		if (status == NodeStatus.NOT_FOUND || status == NodeStatus.DELETED) {
			return false;
		} 

		/*The node exists in the set */

		else {
			/*remove it logically and check if it was marked before*/
			CoolSprayListNode nodeToRemove = succs[0];	
			boolean iMarkedIt = nodeToRemove.logicallyDeleteFromList();
			return iMarkedIt;
		}
	}

	// Finds a candidate for deleteMin
	protected int spray(int H, int L, int D)
	{
		CoolSprayListNode x = _head;
		int level = H;
		while(level>=0)
		{
			int j = serviceClass.randomStep(L);
			/* 
			 * Don't stay on head
			 * Don't advance beyond tail
			 * Usually don't advance to tail
			 * Advance to tail when list is empty
			 */
			for(;(j>0 || x==_head) && x!=_tail && (x.next[level].getReference() != _tail || isSkipListEmpty());j--)
			{
				x = x.next[level].getReference();

				// TODO: skip longer over deleted nodes?
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
					// Successful elimination
					result = node.value;
					break;
				}
			}

			// Attempt cleanup
			int tmp = serviceClass.randomStep(100);
			if(tmp < 3)
			{
				if(clean())
				{
					// Successful cleanup - now try to eliminate an item
					continue;
				}
			}

			// Normal spray
			int p = _threads.get();
			int K = 0;
			int H =  Math.min((int) (Math.log(p)/Math.log(2))+K, _maxAllowedHeight);
			int L = (int) (Math.pow((Math.log(p)/Math.log(2)),3));
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

		if(result != Integer.MAX_VALUE)
		{
			logRemoval();
		}

		return result;
	}

	@Override
	public boolean isEmpty() {
			return _liveItems.get() == 0;
			}

	/**
	 * for internal use - due to items in elimination array, the list might be empty
	 * even when the queue is not empty
	 */
	protected boolean isSkipListEmpty()
	{
		return _itemsInSkipList.get() == 0;
	}

	/**
	 * inform a successful insertion
	 */
	protected void logInsertion(boolean revive)
	{

		_liveItems.getAndIncrement();
		if(!revive)
		{
			_itemsInSkipList.getAndIncrement();
		}
	}

	protected void logReinsert()
	{
		_itemsInSkipList.getAndIncrement();
	}

	/**
	 * inform a successful logical removal
	 */
	protected void logRemoval()
	{
		_liveItems.getAndDecrement();
	}

	/**
	 * inform a successful batch cleanup
	 */
	protected void logCleanup(int amount)
	{
		_itemsInSkipList.getAndAdd(-amount);
	}

	protected class CoolSprayListNode {
		public int value;
		/*The mark in the AtomicMarkableReference is used to the phyiscal deleteion of the last node in the deletion group */ 
		public AtomicMarkableReference<CoolSprayListNode>[] next;
		private AtomicInteger _status;

		// Status codes:
		private static final int ALIVE = 0; // alive in the skiplist
		private static final int DELETED = 1; // logically deleted in the skiplist
		private static final int ALIVE_IN_ELIMINATION = 2; // alive in the elimination array (but not alive in the skiplist)
		private static final int ELIMINATED = 3; // logically deleted in the elimination array

		public CoolSprayListNode(int value, int height) {
			this.value = value;
			this.next =  (AtomicMarkableReference<CoolSprayListNode>[]) new AtomicMarkableReference[height+1];
			for (int i = 0; i < next.length; i++) {
				next[i] = new AtomicMarkableReference<CoolSprayListNode>(null,false);
			}

			_status = new AtomicInteger(ALIVE);
		}

		public int topLevel()
		{
			return next.length-1;
		}

		/* try to mark and return if succeed */
		public boolean markAsEliminationNode() {
			return _status.compareAndSet(ALIVE, ALIVE_IN_ELIMINATION);
		}

		public boolean logicallyDeleteFromList() {
			return _status.compareAndSet(ALIVE, DELETED);
		}

		/* try to revive the node and return true if succeed */
		public boolean revive() { 
			return (_status.compareAndSet(DELETED, ALIVE) || _status.compareAndSet(ELIMINATED, ALIVE));
		}

		public boolean reinsert() {
			return _status.compareAndSet(ALIVE_IN_ELIMINATION, ALIVE);
		}

		public boolean eliminate() {
			return _status.compareAndSet(ALIVE_IN_ELIMINATION, ELIMINATED);
		}

		public boolean isDeleted() {
			int status = _status.get();
			return status == DELETED || status == ELIMINATED;
		}
	}

	protected class NodesEliminationArray {
		private CoolSprayListNode[] arr;
		private AtomicInteger deleteMinCounter; // token allocator
		private AtomicInteger reInsertCounter; // token allocator
		private AtomicInteger pendingCompletion; // prevents overriding before array access is done
		private int numOfNodes = 0; //number of initial total nodes after all insertions
		public NodesEliminationArray(int size) {
			arr = new CoolSprayListNode[size];
			deleteMinCounter = new AtomicInteger(0);
			reInsertCounter = new AtomicInteger(0);
			pendingCompletion = new AtomicInteger(0);

		}
		public void addNode(CoolSprayListNode node) {
			int i = deleteMinCounter.getAndIncrement();
			arr[i] = node;
			pendingCompletion.getAndIncrement();
			reInsertCounter.getAndIncrement();
			numOfNodes++;
		}

		//Traverse the array from lowest to highest
		public CoolSprayListNode getNode() {
			// get token
			int i = numOfNodes -  deleteMinCounter.getAndDecrement();

			if (i >= numOfNodes) {
				// nothing left to do here
				return null;
			}

			// get value
			CoolSprayListNode result = arr[i];

			// inform completion, "release" the array
			pendingCompletion.getAndDecrement();

			//Try to mark the item as deleted - means that no re-insertion was done (or got the linearization point)
			if (result.eliminate()) {
				// successful elimination
				return result;
			}
			else {
				//otherwise - someone else (re-insertion) succeeded to insert and mark it as ready so try the next node.
				return getNode();
			}
		}

		//Traverse the array from highest to lowest
		public CoolSprayListNode getNodeForReinsert(int insertedValue) {
			int i = reInsertCounter.get() - 1; // speculated reinsert token for value test
			int nextEliminatedIndex = numOfNodes - deleteMinCounter.get(); 
			if(i < nextEliminatedIndex || arr[i].value < insertedValue)
			{
				// no relevant reinsert node exists
				return null;
			}

			// get token
			i = reInsertCounter.decrementAndGet();
			if(i < 0)
			{
				return null;
			}

			// Now get value and check if node is still valid for reinsert
			CoolSprayListNode result = arr[i];
			if (result.isDeleted()) {
				// the node was already deleted by some deleteMin
				// no need to retry - lower nodes were also already eliminated
				return null;
			}

			//return result
			return result;
		}

		public boolean hasNodes(){
			return deleteMinCounter.get() > 0;
		}

		public boolean completed()
		{
			return pendingCompletion.get() == 0;
		}

		public boolean contains(int value)
		{
			// go over values that were not removed yet
			for(int i=1; i<= deleteMinCounter.get(); i++)
			{
				// if value found and not deleted (linearization point) return true
				if(arr[numOfNodes - i].value == value && !arr[numOfNodes - i].isDeleted())
				{
					return true;
				}
			}

			return false;
		}
	}

	private enum NodeStatus {
		FOUND,
		DELETED,
		NOT_FOUND
	}

}
