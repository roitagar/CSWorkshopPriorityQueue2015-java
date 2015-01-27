package priorityQueue.news;

public class SeqSprayListPriorityQueue implements IPriorityQueue {
	SeqSprayListNode _head;
	SeqSprayListNode _tail;
	int _maxAllowedHeight;
	int _size;
	
	public SeqSprayListPriorityQueue(int maxAllowedHeight)
	{
		_maxAllowedHeight = maxAllowedHeight;
		_head = new SeqSprayListNode(Integer.MIN_VALUE, maxAllowedHeight);
		_tail = new SeqSprayListNode(Integer.MAX_VALUE, maxAllowedHeight);
		_size = 0;
		/* connect the head sentinel to the tail sentinel */
		for(int i=0;i<=_maxAllowedHeight;i++)
		{
			_head.next[i] = _tail;
		}
	}
	
	protected int find(int value, SeqSprayListNode[] preds, SeqSprayListNode[] succs)
	{
		int lFound = -1;
		SeqSprayListNode pred = _head;
		
		/* traverse each level and update preds and succss,
		 * if node was found - set lFound with the level it was initially found
		 * Note: if a node was found - the succs array cells,
		 * from lFound down to 0, should all point to it. 
		 * !
		 */
		for(int level = _maxAllowedHeight;level>=0;level--)
		{
			SeqSprayListNode curr = pred.next[level];
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
	public void insert(int value) {
		int topLevel = serviceClass.randomLevel(_maxAllowedHeight); //TODO: Check if static method is ok

		SeqSprayListNode[] preds = new SeqSprayListNode[_maxAllowedHeight+1];
		SeqSprayListNode[] succs = new SeqSprayListNode[_maxAllowedHeight+1];

		/* find the location for the new item with preds and succss arrays
		 * if find returns an integer other then -1, it means that
		 * the item is already exist - and we don't want to insert it again
		 */
		int lFound = find(value, preds, succs);

		if(lFound != -1)
		{
			return;
		}

		/*Item is not on the list - so insert it */

		/*Create an new element and connect it, bottom up to its successors */
		SeqSprayListNode newNode = new SeqSprayListNode(value, topLevel);

		for(int level=0; level<=topLevel; level++)
		{
			newNode.next[level] = succs[level];
		}

		/*Connect the new item's predecessors to it */
		for(int level=0; level<=topLevel; level++)
		{
			preds[level].next[level] = newNode;
		}
		_size++;
	}
	
	
	protected boolean remove(int value)
	{
		SeqSprayListNode victim = null;
		int topLevel = -1;
		SeqSprayListNode[] preds = new SeqSprayListNode[_maxAllowedHeight+1];
		SeqSprayListNode[] succs = new SeqSprayListNode[_maxAllowedHeight+1];

		int lFound = find(value, preds, succs);
		
		/* if node were found  - assign succs[lFound]  
		 * (which contains the node we want to remove) to victim and remove it. */
		if(lFound !=-1)
		{
			victim = succs[lFound];
			topLevel = victim.topLevel();

			for (int level = topLevel; level >= 0; level--)
			{
				preds[level].next[level] = victim.next[level];
			}
			_size--;
			return true;
		}

		/*Item was not found*/
		else
		{
			return false;
		}
	}

	// Finds a candidate for deleteMin
	protected int spray(int H, int L, int D)
	{
		SeqSprayListNode x = _head;
		int level = H;
		while(level>=0)
		{
			int j = serviceClass.randomStep(L);
			for(;j>0 || x==_head;j--)
			{
				x = x.next[level];
			}
			level-=D;
		}
		
		return x.value;
	}
	@Override
	public int deleteMin() {
		boolean retry = false;
		int result;
		do
		{
			int p =1;
			int H = (int) Math.log(p)/*+K*/;
			int L = (int) (/*M * */ Math.pow(Math.log(p),3));
			int D = 1; /* Math.max(1, log(log(p))) */
			result = spray(H,L,D);
			retry = !remove(result);
		} while(retry);
		return result;
	}

	@Override
	public boolean isEmpty() {
		return _size==0;
	}

	@Override
	public int size() {
		return _size;
	}
	
	protected class SeqSprayListNode{
		int value;
		SeqSprayListNode[] next;
		
		public SeqSprayListNode(int value, int height)
		{
			this.value = value;
			next =  new SeqSprayListNode[height+1]; // TODO: Verify +/-1
		}
		
		public int topLevel()
		{
			return next.length-1; // TODO: Verify +/-1
		}
	}

}
