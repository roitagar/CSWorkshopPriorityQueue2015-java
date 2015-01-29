package priorityQueue.news;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/*This is a lazy fine-grained locked-base version */

public class LazyLockSparyListPriorityQueue implements IPriorityQueue {
	protected final int _maxAllowedHeight;
	protected AtomicInteger _threads;
	protected AtomicInteger _size;
	protected LazyLockSprayListNode _head;
	protected LazyLockSprayListNode _tail;
	
	
	public LazyLockSparyListPriorityQueue(int maxAllowedHeight)
	{
		_maxAllowedHeight = maxAllowedHeight;
		_threads = new AtomicInteger(0);
		_head = new LazyLockSprayListNode(Integer.MIN_VALUE, maxAllowedHeight);
		_tail = new LazyLockSprayListNode(Integer.MAX_VALUE, maxAllowedHeight);
		for(int i=0;i<=_maxAllowedHeight;i++)
		{
			_head.next[i] = _tail;
		}
	}
	
	/*This implementation of find is identical to the serial one, and is wait-free */
	protected int find(int value, LazyLockSprayListNode[] preds, LazyLockSprayListNode[] succs)
	{
		int lFound = -1;
		LazyLockSprayListNode pred = _head;
		
		/* traverse each level and update preds and succss,
		 * if node was found - set lFound with the level it was initially found
		 * Note: if a node was found - the succs array cells,
		 * from lFound down to 0, should all point to it. 
		 * !
		 */
		for(int level = _maxAllowedHeight;level>=0;level--)
		{
			LazyLockSprayListNode curr = pred.next[level];
			while(value > curr.value)
			{
				pred = curr;
				curr = pred.next[level];
			}
			if(lFound == -1 && value == curr.value) {
				lFound = level;
			}
			
			/*update the preds and succss arrays for the current level */
			preds[level] = pred;
			succs[level] = curr;
		}
		return lFound;
	}
	
	
	@Override
	public boolean insert(int value) {
		/* Increment the number of threads that are writing to the SprayList by 1,
		 * in order to use this number in DeleteMin
		 */
		_threads.incrementAndGet();
		
		int topLevel = serviceClass.randomLevel(_maxAllowedHeight);	
		LazyLockSprayListNode[] preds = new LazyLockSprayListNode[_maxAllowedHeight+1];
		LazyLockSprayListNode[] succs = new LazyLockSprayListNode[_maxAllowedHeight+1];

		while(true)
		{
			/* Find the location for the new item with predecessors and successors arrays
			 * if find returns an integer other then -1, it means that the key is already exist (physically!).
			 * In this implementation we also have to check if the item is logically exists(by checking node.isMark = false).
			 * If it also logically exist - wait until it will be fully linked (in case other thread inserting the same key)
			 * and then return false - because we don't want to add it.
			 * otherwise - it is logically deleted - and it is about to be physically deleted, so try again.
			 * Note that if the node is not fully linked - it cannot be marked (see: Remove method),
			 * and therefore we will just wait until it will be fully linked and just after that we will return,
			 * in order to be consistent with the linearization point.
			 */
			int lFound = find(value, preds, succs);
			if(lFound != -1)
			{
				LazyLockSprayListNode nodeFound = succs[lFound];
				
				/*Check if the node is not deleted */
				if(!nodeFound.isMarked())
				{
					/* If node is in an insertion process - it is not fully linked
					 * wait until it will be fully linked
					 */
					while(!nodeFound.isFullyLinked()) {
						//do nothing
					} 
					/*Linearization Point of unsuccessful insertion */
					_threads.decrementAndGet();
					return false;
				}
				/*If you arrived here, it means that node is logically deleted, but not yet physically,
				 * so continue with the loop and try again - maybe next time it will be deleted 
				 */
				continue;
			}

			int highestLocked = -1;
			try
			{
				LazyLockSprayListNode pred, succ;
				boolean valid = true;
				
				/* Try to acquire the locks of all the predecessor bottom up - in order to avoid deadlock
				 * (Remove behaves the same way when acquiring locks).
				 * Note that from the moment we found the appropriate location for insertion,
				 * and until we finished lock all the predecessors, 
				 * the structure can changed in a way we have to start over:
				 * - Either one or more of the predecessors or the successors can be deleted,
				 * - Or one or more nodes were inserted between one or more of the predecessors and the successors.
				 * Therefore we must validate it was not occurred, and if it was - start over.
				 */
				for(int level = 0; valid && level<=topLevel; level++)
				{
					pred = preds[level];
					succ = succs[level];
					pred.lock();
					highestLocked = level;
					valid = !pred.isMarked() && !succ.isMarked() && (pred.next[level] == succ);
				}
				
				/* If one or more of the validation test failed - start over. */
				if(!valid) {
					continue;
				}
				
				/* At this point, all predecessor in all levels are exclusively locked for the insertion,
				 * and we are ready to insert the new node:
				 * Create an new element and connect it, bottom up to its successors
				*/
				
				LazyLockSprayListNode newNode = new LazyLockSprayListNode(value, topLevel);
				
				for(int level=0; level<=topLevel; level++)
				{
					newNode.next[level] = succs[level];
				}

				for(int level=0; level<=topLevel; level++)
				{
					preds[level].next[level] = newNode;
				}

				/* Now the node is fully linked - mark it as fully linked, and exit from the loop. 
				 * Note: This is a linearization point - where the node was successfully added*/
				newNode.setFullyLinked();
				break;
			}
			finally
			{
				/* Release all the predecessors locks */
				for(int level = 0; level<=highestLocked; level++)
				{
					preds[level].unlock();
				}
			}
		}
		
		_threads.decrementAndGet();
		return true;
	}
	
	protected boolean remove(int value)
	{
		LazyLockSprayListNode victim = null;
		int topLevel = -1;
		LazyLockSprayListNode[] preds = new LazyLockSprayListNode[_maxAllowedHeight+1];
		LazyLockSprayListNode[] succs = new LazyLockSprayListNode[_maxAllowedHeight+1];
		
		/* This local variable isMarked is set to true
		 * when a thread succeed to lock a node and mark it as marked(i.e logically deleted).
		 * After isMarked to true - the node will eventually deleted by *this* thread
		 */
		boolean isMarked = false;
		
		while(true)
		{
			/* if node were found -assign succs[lFound]  
			 * (which contains the node we want to remove) to victim */
			int lFound = find(value, preds, succs);
			if(lFound !=-1)
			{
				victim = succs[lFound];
			}
			
			//TODO: WRITE a COMMENT!!!
			// testing topLevel==lFound is not necessarily required, due to the otehr two checks
			if (isMarked ||  (lFound != -1 &&
					victim.isFullyLinked()  && 
					victim.topLevel() == lFound  && 
					!victim.isMarked())) {
				
				/*Check if the node was already marked by me.
				 * If it wasn't, try to lock it and mark it, and then set the local isMarked to true.
				 */
				if(!isMarked)
				{
					topLevel = victim.topLevel();
					/* Grab a Lock on the victim, and check if it is already marked.
					 * If it is, release the lock and return, because victim was marked by another thread, and is being removed.
					 * If victim is not marked - mark it, and continue.
					 * In this case, victim will be release only after the current thread will delete it.
					 */
						victim.lock();
						if(victim.isMarked())
						{
							 /*	This is a linearization point of unsuccessful remove.*/
							victim.unlock();
							return false;
						}
						
						/*This is a linearization point of successful remove */
						victim.mark();
						isMarked = true;
				}

				/*Now the victim is mine - locked and marked, and I have to remove it */
				
				int highestLocked = -1;
				try
				{
					LazyLockSprayListNode pred, succ;
					boolean valid = true;
					
					/* Try to acquire the locks of all the predecessor bottom up - in order to avoid deadlock
					 * (Insert behaves the same way when acquiring locks).
					 * Note that from the moment we found the victim and its predecessors,
					 * and until we finished lock all the predecessors, 
					 * the structure can changed in a way we have to start over:
					 * - Either one or more of the predecessors can be deleted,
					 * - Or one or more nodes were inserted between one or more of the predecessors and the victim.
					 * Therefore we must validate it was not occurred, and if it was - start over.
					 */
					for (int level = 0; valid && (level <= topLevel); level++)
					{
						pred = preds[level];
				        pred.lock();
						highestLocked = level;
						valid = !pred.isMarked() && pred.next[level]==victim;
					}
					
					/* If one or more of the validation test failed - start over,
					 * but this time - isMarked = true*/
					if (!valid) continue;
					
					/* At this point, all predecessor in all levels are exclusively locked,
					 * and we are ready to physically remove the victim.
					*/
					for (int level = topLevel; level >= 0; level--)
					{
						preds[level].next[level] = victim.next[level];
					}
					/* Release the lock of the victim - and return true*/
					victim.unlock();
					return true;
				}
				finally
				{
					/* Release all the predecessors locks */
					for (int i = 0; i <= highestLocked; i++)
					{
						preds[i].unlock();
					}
				}
			}
			else
			{
				// Item was either not found, or not ready for deletion (in the middle of insert/remove)
				return false;
			}
		}
	}

	@Override
	public int deleteMin() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	protected class LazyLockSprayListNode{
		public int value;
		public LazyLockSprayListNode[] next;
		
		/*Each Node has a lock and also has to flags:
		 * fullyLinked - true if the node is pointed by all its predecessors, and points to all it successors
		 * marked - true if the node was logically deleted
		 */
		private final ReentrantLock _lock = new ReentrantLock();
		private volatile boolean _fullyLinked;
		private volatile boolean _marked;
		
		
		public LazyLockSprayListNode(int value, int height)
		{
			this.value = value;
			this._fullyLinked = false;
			this._marked = false;
			next =  new LazyLockSprayListNode[height+1]; // TODO: Verify +/-1
		}
		
		public int topLevel()
		{
			return next.length-1; // TODO: Verify +/-1
		}
		
		public void mark() {
			_marked = true;
		}
		
		public boolean isMarked() {
			return _marked;
		}

		public boolean isFullyLinked() {
			return _fullyLinked;
		}

		public void setFullyLinked() {
			_fullyLinked = true;
		}
		
		public void lock() {
			_lock.lock();
		}
		
		public void unlock() {
			_lock.unlock();
		}
	}

}
