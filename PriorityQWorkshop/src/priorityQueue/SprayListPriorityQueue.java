package priorityQueue;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.ReentrantLock;

public abstract class SprayListPriorityQueue extends AbstractSprayListPriorityQueue {
	SprayListNode _head;
	SprayListNode _tail;

	
	protected class SprayListNode{
		int value;
		private volatile boolean _fullyLinked;
		private volatile boolean _marked;
		SprayListNode[] next;
		final ReentrantLock lock = new ReentrantLock();
		
		public SprayListNode(int value, int height)
		{
			this.value = value;
			this._fullyLinked = false;
			this._marked = false;
			next =  new SprayListNode[height+1]; // TODO: Verify +/-1
		}
		
		public int topLevel()
		{
			return next.length-1; // TODO: Verify +/-1
		}
		
		/**
		 * Atomically marks this node as deleted
		 * returns the old mark value
		 * *** ONLY USE THIS as a single node mark, for implementations using locks ***
		 * @return
		 */
		public void mark() {
			_marked = true;
		}
		public boolean isMarked()
		{
			return _marked;
		}

		public boolean isFullyLinked() {
			return _fullyLinked;
		}

		public void setFullyLinked() {
			_fullyLinked = true;
		}
	}
	
	public SprayListPriorityQueue(int maxAllowedHeight)
	{
		super(maxAllowedHeight);
		_head = new SprayListNode(Integer.MIN_VALUE, maxAllowedHeight);
		_tail = new SprayListNode(Integer.MAX_VALUE, maxAllowedHeight);
		for(int i=0;i<=_maxAllowedHeight;i++)
		{
			_head.next[i] = _tail;
		}
	}
	
	protected abstract boolean canInsertBetween(SprayListNode pred, SprayListNode succ, int level);

	// Locks a node for edit, if required
	protected abstract void lockNode(SprayListNode node);
	
	// Unlocks a locked node, if required
	protected abstract void unlockNode(SprayListNode node);

	protected abstract boolean readyToBeDeleted(SprayListNode victim);

	@Override
	public void insert(int value) {
		// TODO Auto-generated method stub
		// long insert logic
		_threads.incrementAndGet();
		int topLevel = randomLevel();
		SprayListNode[] preds = new SprayListNode[_maxAllowedHeight+1];
		SprayListNode[] succs = new SprayListNode[_maxAllowedHeight+1];
		boolean success = false;

		while(!success)
		{
			int lFound = find(value, preds, succs);

			//if (lfound!=-1) it means that the item is already exist
			//then return; TODO: decide whether to change to boolean/execption

			if(lFound != -1)
			{
				SprayListNode nodeFound = succs[lFound];
				//if the item is marked due to deletion
				if(!nodeFound.isMarked())
				{
					//wait for prev insertion to finish?
					//						while(!nodeFound.isFullyLinked()){}
					return;// false;
				}
				continue;
			}
			int highestLocked = -1;
			try
			{
				SprayListNode pred, succ;
				boolean valid = true;
				// Try to connect the new element, bottom up
				for(int level = 0; valid && level<=topLevel; level++)
				{
					pred = preds[level];
					succ = succs[level];
					lockNode(pred); // note: not necessarily locks anything, implementation dependant
					highestLocked = level;
					// valid == false means a different item was inserted after the same pred, or pred/succ were removed
					valid = canInsertBetween(pred, succ, level);
				}

				// TODO: convert the lock & unlock loops to a method of this kind:
				// valid = aquireAllLevels(preds, succs, level, out highestLocked);

				if(!valid) continue;
				// At this point, all preds in all levels are exclusively locked for this insertion
				SprayListNode newNode = new SprayListNode(value, topLevel);
				for(int level=0; level<=topLevel; level++)
				{
					newNode.next[level] = succs[level];
				}

				for(int level=0; level<=topLevel; level++)
				{
					preds[level].next[level] = newNode;
				}

				newNode.setFullyLinked(); // Successful add linearization point
				success = true;
			}
			finally
			{
				for(int level = 0; level<=highestLocked; level++)
				{
					unlockNode(preds[level]);
				}
			}
		}
		_threads.decrementAndGet();
	}

	protected int find(int value, SprayListNode[] preds, SprayListNode[] succs)
	{
		int lFound = -1;
		SprayListNode pred = _head;
		for(int level = _maxAllowedHeight;level>=0;level--)
		{
			SprayListNode curr = pred.next[level];
			while(value > curr.value)
			{
				pred = curr;
				curr = pred.next[level];
			}
			if(lFound == -1 && value == curr.value) {
				lFound = level;
			}
			preds[level] = pred;
			succs[level] = curr;
		}
		return lFound;
	}
	


	protected boolean remove(int value)
	{
		SprayListNode victim = null;
		boolean isMarked = false;
		int topLevel = -1;
		SprayListNode[] preds = new SprayListNode[_maxAllowedHeight+1];
		SprayListNode[] succs = new SprayListNode[_maxAllowedHeight+1];
		
		while(true)
		{
			int lFound = find(value, preds, succs);
			if(lFound !=-1)
			{
				victim = succs[lFound];
			}
			
			// testing topLevel==lFound is not necessarily required, due to the otehr two checks
			if (isMarked ||  (lFound != -1 && readyToBeDeleted(victim)))// (victim.isFullyLinked()  && victim.topLevel() == lFound  && !victim.isMarked())))
			{
				if(!isMarked)
				{
					topLevel = victim.topLevel();
					lockNode(victim);
					if(victim.isMarked())
					{
						// Item was marked by another thread, and is being removed.
						unlockNode(victim);
						return false;
					}
				victim.mark();
				isMarked = true;
				}

				int highestLocked = -1;
				try
				{
					SprayListNode pred, succ;
					boolean valid = true;
					for (int level = 0; valid && (level <= topLevel); level++)
					{
						pred = preds[level];
				        lockNode(pred);
						highestLocked = level;
						valid = !pred.isMarked() && pred.next[level]==victim;
					}
					
					// valid == false if pred is pending deletion, or a new item was inserted after it
					if (!valid) continue;
					
					for (int level = topLevel; level >= 0; level--)
					{
						preds[level].next[level] = victim.next[level];
					}
					unlockNode(victim);
					return true;
				}
				finally
				{
					for (int i = 0; i <= highestLocked; i++)
					{
						unlockNode(preds[i]);
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
	
	// Finds a candidate for deleteMin
	protected int spray(int H, int L, int D)
	{
		SprayListNode x = _head;
		int level = H;
		while(level>=0)
		{
			int j = randomStep(L);
			for(;(j>0 || x==_head) && x!=_tail;j--)
			{
				x = x.next[level];
			}
			level-=D;
		}
		
		return x.value;
	}
	
	@Override
	public boolean isEmpty() {
		return _head.next[0] == _tail;
	}
	
}
