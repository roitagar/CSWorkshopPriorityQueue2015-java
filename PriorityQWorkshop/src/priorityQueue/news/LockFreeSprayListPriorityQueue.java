package priorityQueue.news;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeSprayListPriorityQueue implements IPriorityQueue {
	protected  int _maxAllowedHeight;
	protected AtomicInteger _threads;
	protected AtomicInteger _size;
	protected LockFreeSprayListNode _head;
	protected LockFreeSprayListNode _tail;


	public LockFreeSprayListPriorityQueue(int maxAllowedHeight) {
		_maxAllowedHeight = maxAllowedHeight;
		_threads = new AtomicInteger(0);
		_head = new LockFreeSprayListNode(Integer.MIN_VALUE, maxAllowedHeight);
		_tail = new LockFreeSprayListNode(Integer.MAX_VALUE, maxAllowedHeight);
		
		for(int i=0;i<=_maxAllowedHeight;i++)
		{
			_head.next[i] = new AtomicMarkableReference<LockFreeSprayListNode>(_tail, false);
			_tail.next[i] = new AtomicMarkableReference<LockFreeSprayListNode>(null, false);
		}
	}

	public boolean insert(int value) {
		_threads.getAndIncrement();
		int topLevel = serviceClass.randomLevel(_maxAllowedHeight);
		LockFreeSprayListNode[] preds = new LockFreeSprayListNode[_maxAllowedHeight+1];
		LockFreeSprayListNode[] succs = new LockFreeSprayListNode[_maxAllowedHeight+1];
		while(true)
		{
			/* Find the location for the new item with predecessors and successors arrays.
			 * If find returns true, it means that the key is already exist
			 * both physically and logically, because find always returns exist keys and remove marked items,
			 * so we don't want to add it.
			 */
			boolean found = find(value, preds, succs);

			if(found)
			{
				/*linearization point of unsuccessful insertion */
				_threads.getAndDecrement();
				return false;
			}

			/*The item is not in the set - so add it */
			else 
			{
				/* create a new node and connect it the the successors */
				LockFreeSprayListNode newNode = new LockFreeSprayListNode(value, topLevel);
				for (int level = 0; level <= topLevel; level++) {
					LockFreeSprayListNode succ = succs[level];
					newNode.next[level].set(succ, false);
				}
				
				
				LockFreeSprayListNode pred = preds[0];
				LockFreeSprayListNode succ = succs[0];
				
				/* Check if the predecessor at level 0 (the closet pred)
				 * is connected to the successor at level 0 (means no new nodes were inserted between them),
				 * and also if it is not marked (because a node is logically deleted if its next[0] is marked).
				 * If everything is OK - connect (atomically with the test) the predecessor at level 0 to the new node at level 0.
				 * Otherwise - something was changed - start over.
				 * When the node is connected at level 0 is considered as a linearization point.
				 */
				if(!pred.next[0].compareAndSet(succ, newNode, false, false)) {
					continue;
				}
				/* now when level 0 is connected - connect the other predecessors from the other levels to the new node.
				 * If you fail to connect a specific level - find again - means - prepare new arrays of preds and succs,
				 * and continue from the level you failed.
				 */
				for (int level= 1; level <= topLevel; level++) {
					while (true) {
						pred = preds[level];
						succ = succs[level];
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

	protected boolean remove(int value) {
		LockFreeSprayListNode[] preds = new LockFreeSprayListNode[_maxAllowedHeight+1];
		LockFreeSprayListNode[] succs = new LockFreeSprayListNode[_maxAllowedHeight+1];
		LockFreeSprayListNode succ;
		
		
		while (true) {
			/* Find the location for the new item with predecessors and successors arrays.
			 * If find returns false, it means that the key is not exist
			 * both physically and logically, because if it was only logically deleted - find removed it physically,
			 * and we don't have to remove it.
			 */
			boolean found = find(value, preds, succs);
			if (!found) {
				return false;
			} 
			
			/*The node exists in the set */
			
			else {
				/*Traverse all the levels - except the bottom level (level 0) of the victim, top to level 1,
				 * and check if they already marked. If a level is marked - go on.
				 * Otherwise - try to mark it using CAS - it is being marked (by you or by some other thread).
				 * Note - the victim is not yet logically deleted.
				 */
				LockFreeSprayListNode nodeToRemove = succs[0];
				for (int level = nodeToRemove.topLevel(); level >= 1; level--) {
					boolean[] marked = {false};
					succ = nodeToRemove.next[level].get(marked);
					while (!marked[0]) {
						nodeToRemove.next[level].attemptMark(succ, true);
						succ = nodeToRemove.next[level].get(marked);
					}
				}
				/*Now when all the levels except level 0 is marked - try to mark level 0 - and to logically delete it */
				boolean[] marked = {false};
				
				/* get the direct successor (at level 0). (Don't care about where it is marked or not) */
				succ = nodeToRemove.next[0].get(marked);
				while (true) {
					/* Try to only mark the victim. If you marked it - iMarkedIt = true.
					 * Otherwise - one of the two happened:
					 * - Either another thread changed or marked the successor.
					 * - Or another thread marked the victim.
					 */
					boolean iMarkedIt = nodeToRemove.next[0].compareAndSet(succ, succ, false, true); //linearization point
					/* Check if the victim is marked. (Don't care about its successor) */
					succ = succs[0].next[0].get(marked);
					
					if (iMarkedIt) {
						/*I marked it - so run find in order to physically remove the item (this is an optimization) and return true.
						 * Don't care about find's return value - because most probably it will return false (becuase I logically deleted it),
						 * and if it return true - it's becauase another thread has added the key after my deletion. 
						 */
						find(value, preds, succs); //find also removes this item!
						return true;
					}
					/* Now I know didn't mark it, but maybe the node is not marked at all and the CAS failed because another thread 
					 * marked the victim's successor.So, if it is marked - return because someone else remove it.
					 * Otherwise - start over.
					 */
					else if (marked[0]) {
						return false;
					}
					
					/*start over */
				}
			}
		}
	}
	
	

	@Override
	public boolean isEmpty() {
		return _head.next[0].getReference() == _tail;
	}
	

	/* The find function is lock-free in contrast to the lazy implementation (where it is wait-free).
	 * When it notices a marked node, it tries to physically remove it.
	 * Pay attention that the level a node were found is meaningless because it might not fully linked!
	 */
	
	protected boolean find(int value, LockFreeSprayListNode[] preds, LockFreeSprayListNode[] succs)
	{
		boolean[] marked = {false};
		boolean snip;
		LockFreeSprayListNode pred = null, curr = null, succ = null;
		retry:
			while (true) {
				pred = _head;
				
				/* Traverse each level up do bottom */
				for (int level = _maxAllowedHeight; level >= 0; level--) {
					curr = pred.next[level].getReference();
					
					while (true) {
						/* Check if curr is marked in this level. If it is, try to connect it to it successor.
						 * If you succeed - update curr and succ at level, and check again if the new curr is marked.
						 * If the new curr is also marked - do the same, otherwise - check the value and progress.
						 * If you didn't succeed to physically delete it (CAS failed) - start over.
						 */
						succ = curr.next[level].get(marked);
						while (marked[0]) {
							snip = pred.next[level].compareAndSet(curr, succ, false, false);
							if (!snip){
								continue retry;
							}
							curr = pred.next[level].getReference();
							succ = curr.next[level].get(marked);
						}
						
						if (curr.value  < value){
							pred = curr;
							curr = succ;
						} else {
							break;
						}
					}
					/* update preds and succss */
					preds[level] = pred;
					succs[level] = curr;
				}
				return (curr.value == value);
			}
	}


	// Finds a candidate for deleteMin
	protected int spray(int H, int L, int D)
	{
		LockFreeSprayListNode x = _head;
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
			for(;(j>0 || x==_head) && x!=_tail && (x.next[level].getReference() != _tail || isEmpty());j--)
			{
				x = x.next[level].getReference();
			}
			level-=D;
		}
		
		return x.value;
	}
	
	//TODO: Do we really need the test here?
	@Override
	public int deleteMin() {
		_threads.incrementAndGet();
		boolean retry = false;
		int result;
		//long tid = Thread.currentThread().getId();
		do
		{
			int p = _threads.get();
			int H = (int) Math.log(p)/*+K*/;
			int L = (int) (/*M * */ Math.pow(Math.log(p),3));
			int D = 1; /* Math.max(1, log(log(p))) */
			result = spray(H,L,D);
			//System.out.println("Thread " + tid + ": After spray got "+ result);
			if(result == Integer.MAX_VALUE)
			{
				// if we got tail's value, the list might be empty
				retry = !isEmpty();
			}
			else
			{
				retry = !remove(result);
				//(retry = true) means that another thread performed an action that affect the remove
				//System.out.println("Thread " + tid + ": After remove " + result + " got retry="+ retry);
			}
		} while(retry);
		_threads.decrementAndGet();
		return result;
	}
	
	
	public static final class LockFreeSprayListNode {
		int value;
		AtomicMarkableReference<LockFreeSprayListNode>[] next;
		
		public LockFreeSprayListNode(int value, int height) {
			this.value = value;
			next = (AtomicMarkableReference<LockFreeSprayListNode>[]) new AtomicMarkableReference[height+1];
			
			for (int i = 0; i < next.length; i++) {
				next[i] = new AtomicMarkableReference<LockFreeSprayListNode>(null,false);
			}
		}
		
		public int topLevel()
		{
			return next.length-1;
		}
		
	}
}
