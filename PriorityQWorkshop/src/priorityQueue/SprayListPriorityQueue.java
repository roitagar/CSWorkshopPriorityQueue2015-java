package priorityQueue;

import java.util.concurrent.atomic.AtomicMarkableReference;

public abstract class SprayListPriorityQueue implements IPriorityQueue {

	SprayListNode _head;
	SprayListNode _tail;
	int _maxAllowedHeight;
	boolean isFullyLinked;
	boolean isMarked;
	
	protected class SprayListNode{
		int value;
		AtomicMarkableReference<SprayListNode>[] next;
		
		public SprayListNode(int value, int height)
		{
			this.value = value;
			next = (AtomicMarkableReference<SprayListNode>[]) new AtomicMarkableReference[height]; // TODO: Verify +/-1
		}
		
		public int topLevel()
		{
			return next.length-1; // TODO: Verify +/-1
		}
	}
	
	public SprayListPriorityQueue(int maxAllowedHeight)
	{
		_maxAllowedHeight = maxAllowedHeight;
		_head = new SprayListNode(Integer.MIN_VALUE, maxAllowedHeight);
		_tail = new SprayListNode(Integer.MAX_VALUE, maxAllowedHeight);
		for(int i=0;i<_maxAllowedHeight;i++)
		{
			_head.next[i] =
					new AtomicMarkableReference<SprayListNode>(
							_tail,
							false);
		}
	}
	
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
			// The next commented code handles duplicates in a thread-safe way
//			if(lFound != -1){
//				// value was found in level lFound
//				SprayListNode nodeFound = succs[lFound];
//				if(!nodeFound.marked){
//					while(!nodeFound.fullyLinked){}
//					return false;
//				}
//				continue;
//			}
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
//					pred.lock.lock();
					highestLocked = level;
					// valid == false means a different item was inserted after the same pred, or pred/succ were removed
					valid = /*!pred.marked && !succ.marked && */ pred.next[level].getReference()==succ;
				}
				
				if(!valid) continue;
				// At this point, all preds in all levels are exclusively locked for this insertion
				SprayListNode newNode = new SprayListNode(value, topLevel);
				for(int level=0; level<=topLevel; level++)
				{
					newNode.next[level] = new AtomicMarkableReference<SprayListNode>(succs[level],false);
				}
				for(int level=0; level<=topLevel; level++)
				{
					preds[level].next[level] = new AtomicMarkableReference<SprayListNode>(newNode,false);
				}
//				newNode.fullyLinked = true; // Successful add linearization point
				success = true;
			}
			finally
			{
//				for(int level = 0; level<=highestLocked; level++)
//				{
//					preds[level].unlock();
//				}
			}
		}
		
		
		endInsert();
	}
	
	protected abstract void startInsert();
	protected abstract void endInsert();
	protected abstract int randomLevel();
	
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
		int p = getNumberOfThreads();
		int H = (int) Math.log(p)/*+K*/;
		int L = (int) (/*M * */ Math.pow(Math.log(p),3));
		int D = 1; /* Math.max(1, log(log(p))) */
		int result = spray(H,L,D);
		remove(result);
		endDeleteMin();
		return result;
	}
	
	protected abstract void startDeleteMin();
	protected abstract void endDeleteMin();
	// Number of threads currently calling deleteMin
	protected abstract int getNumberOfThreads();
	
	protected abstract int randomStep(int max);
	
	// Finds a candidate for deleteMin
	private int spray(int H, int L, int D)
	{
		SprayListNode x = _head;
		int level = H;
		while(level>=0)
		{
			int j = randomStep(L);
			for(;j>0;j--)
			{
				x = x.next[level].getReference();
			}
			level-=D;
		}
		
		return x.value;
	}
	
	private void remove(int value)
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
			if (isMarked |  (lFound != -1 &&   (victim.isFullyLinked   && victim.topLevel == lFound  && !victim.marked)))
			{
				if(!isMarked)
				{
					topLevel = victim.topLevel();
					victim.lock.lock();
					if(victim.marked)
					{
						victim.lock.unlock();
						return false;
					}
				victim.marked = true;
				isMarked = true;
				}
			}
			int highestLocked = -1;
			try
			{
				Node<T> pred, succ;
				boolean valid = true;
				for (int level = 0; valid && (level <= topLevel); level++)
				{
					pred = preds[level];
			        pred.lock.lock();
		         	highestLocked = level;
		         	valid = !pred.marked && pred.next[level]==victim;
				}
				if (!valid) continue;
				for (int level = topLevel; level >= 0; level--)
				{
					preds[level].next[level] = victim.next[level];	
				}
				victim.lock.unlock();
				return true;
			}
			finally
			{
				for (int i = 0; i <= highestLocked; i++)
				{
					preds[i].unlock();
				}
			}else return false;
		}
	}

		
	
	
//	private 

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
