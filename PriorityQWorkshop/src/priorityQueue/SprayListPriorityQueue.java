package priorityQueue;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.ReentrantLock;

public abstract class SprayListPriorityQueue implements IPriorityQueue {

	SprayListNode _head;
	SprayListNode _tail;
	int _maxAllowedHeight;
	
	protected class SprayListNode{
		int value;
		private boolean _fullyLinked;
		AtomicMarkableReference<SprayListNode>[] next;
		final ReentrantLock lock = new ReentrantLock();
		
		public SprayListNode(int value, int height)
		{
			this.value = value;
			_fullyLinked = false;
			next = (AtomicMarkableReference<SprayListNode>[]) new AtomicMarkableReference[height+1]; // TODO: Verify +/-1
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
		public boolean tryMark()
		{
			SprayListNode succ = next[0].getReference();
			return next[0].attemptMark(succ, true);
		}
		
		public boolean isMarked()
		{
			return next[0].isMarked();
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
		_maxAllowedHeight = maxAllowedHeight;
		_head = new SprayListNode(Integer.MIN_VALUE, maxAllowedHeight);
		_tail = new SprayListNode(Integer.MAX_VALUE, maxAllowedHeight);
		for(int i=0;i<=_maxAllowedHeight;i++)
		{
			_head.next[i] =
					new AtomicMarkableReference<SprayListNode>(
							_tail,
							false);
			_tail.next[i] = 
					new AtomicMarkableReference<SprayListNode>(
							null,
							false);
		}
	}
	
	protected abstract boolean canInsertBetween(SprayListNode pred, SprayListNode succ, int level);

	// Locks a node for edit, if required
	protected abstract void lockNode(SprayListNode node);
	
	// Unlocks a locked node, if required
	protected abstract void unlockNode(SprayListNode node);
	
	protected abstract void startInsert();
	protected abstract void endInsert();
	protected abstract int randomLevel();
	
	protected abstract void startDeleteMin();

	protected abstract void endDeleteMin();

	// Number of threads currently calling deleteMin
	protected abstract int getNumberOfThreads();

	protected abstract int randomStep(int max);

	protected abstract boolean readyToBeDeleted(SprayListNode victim);

	@Override
	public void insert(int value) {
		// TODO Auto-generated method stub
		startInsert();
		// long insert logic
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
					newNode.next[level] = new AtomicMarkableReference<SprayListNode>(succs[level],false);
				}

				for(int level=0; level<=topLevel; level++)
				{
					preds[level].next[level].set(newNode,false); // TODO: encapsulate this, as it is illegal for a lockless implementation
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

		endInsert();
	}

	protected int find(int value, SprayListNode[] preds, SprayListNode[] succs)
	{
		int lFound = -1;
		SprayListNode pred = _head;
		for(int level = _maxAllowedHeight;level>=0;level--)
		{
			SprayListNode curr = pred.next[level].getReference();
			while(value > curr.value)
			{
				pred = curr;
				curr = pred.next[level].getReference();
			}
			if(lFound == -1 && value == curr.value) {
				lFound = level;
			}
			preds[level] = pred;
			succs[level] = curr;
		}
		return lFound;
	}
	

	@Override
	public int deleteMin() {
		startDeleteMin();
		boolean retry = false;
		int result;
		do
		{
			int p = getNumberOfThreads();
			int H = (int) Math.log(p)/*+K*/;
			int L = (int) (/*M * */ Math.pow(Math.log(p),3));
			int D = 1; /* Math.max(1, log(log(p))) */
			result = spray(H,L,D);
			retry = !remove(result);
		} while(retry);
		endDeleteMin();
		return result;
	}
	
	// Finds a candidate for deleteMin
	private int spray(int H, int L, int D)
	{
		SprayListNode x = _head;
		int level = H;
		while(level>=0)
		{
			int j = randomStep(L);
			for(;j>0 || x==_head;j--)
			{
				x = x.next[level].getReference();
			}
			level-=D;
		}
		
		return x.value;
	}

	private boolean remove(int value)
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
				victim.tryMark();
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
						valid = !pred.isMarked() && pred.next[level].getReference()==victim;
					}
					
					// valid == false if pred is pending deletion, or a new item was inserted after it
					if (!valid) continue;
					
					for (int level = topLevel; level >= 0; level--)
					{
						preds[level].next[level].set(victim.next[level].getReference(), false); // TODO: encapsulate this, as it is illegal for a lockless implementation
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

		
	
	
//	private 

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return _head.next[0].getReference() == _tail;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
