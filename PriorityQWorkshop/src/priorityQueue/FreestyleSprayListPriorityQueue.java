package priorityQueue;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

import priorityQueue.SprayListPriorityQueue.SprayListNode;
import priorityQueue.utils.LockFreeRandom;

public class FreestyleSprayListPriorityQueue extends  AbstractSprayListPriorityQueue {

	SprayListNode _head;
	SprayListNode _tail;
	public static final class SprayListNode {
		int value;
		AtomicMarkableReference<SprayListNode>[] next;
		
		public SprayListNode(int value, int height) {
			this.value = value;
			next = (AtomicMarkableReference<SprayListNode>[]) new AtomicMarkableReference[height+1]; // TODO: Verify +/-1
			
			for (int i = 0; i < next.length; i++) {
				next[i] = new AtomicMarkableReference<SprayListNode>(null,false);
			}
		}
		
		public int topLevel()
		{
			return next.length-1; // TODO: Verify +/-1
		}
		
	}

	public FreestyleSprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
		
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

	public void insert(int value) {
		_threads.getAndIncrement();
		// long insert logic
		int topLevel = randomLevel();
		SprayListNode[] preds = new SprayListNode[_maxAllowedHeight+1];
		SprayListNode[] succs = new SprayListNode[_maxAllowedHeight+1];
		boolean success = false;
		while(!success)
		{
			boolean found = find(value, preds, succs);

			if(found)
			{
				success = true;
				break; //TODO: Check if has to be boolean. In that case - it should return false
			}
			else 
			{
				SprayListNode newNode = new SprayListNode(value, topLevel);
				 for (int level = 0; level <= topLevel; level++) {
					 SprayListNode succ = succs[level];
					 newNode.next[level].set(succ, false);
					}
				 SprayListNode pred = preds[0];
				 SprayListNode succ = succs[0];
				 newNode.next[0].set(succ, false);
				 if(!pred.next[0].compareAndSet(succ, newNode, false, false)) {
					 continue;
				 }
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
				 success = true; //TODO :True;
			}
		}
		_threads.getAndDecrement();
	}
	
	protected boolean remove(int value) {
		SprayListNode[] preds = new SprayListNode[_maxAllowedHeight+1];
		SprayListNode[] succs = new SprayListNode[_maxAllowedHeight+1];
		SprayListNode succ;
		while (true) {
			boolean found = find(value, preds, succs);
			if (!found) {
				return false;
			} 
			else {
				SprayListNode nodeToRemove = succs[0];
				for (int level = nodeToRemove.topLevel(); level >= 1; level--) {
					boolean[] marked = {false};
					succ = nodeToRemove.next[level].get(marked);
					while (!marked[0]) {
						nodeToRemove.next[level].attemptMark(succ, true);
						succ = nodeToRemove.next[level].get(marked);
					}
				}
				boolean[] marked = {false};
				succ = nodeToRemove.next[0].get(marked);
				while (true) {
					boolean iMarkedIt = nodeToRemove.next[0].compareAndSet(succ, succ, false, true); //linearization point
					succ = succs[0].next[0].get(marked);
					if (iMarkedIt) {
						find(value, preds, succs); //find also removes this item!
						return true;
					}
					// I didn't marked it - but it's marked
					else if (marked[0]) {
						return false;
						}
					/*else - I didn't marked it, and nobody else did - keep trying to mark
					 * This case can happened when ComperAndSet fails because succ != succ - cause someone else 
					 * changed the succ reference but we already have the victim - so we try again without find.
					*/
				}
			}
		}
	}
	
	

	@Override
	public boolean isEmpty() {
		return _head.next[0].getReference() == _tail;
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
				x = x.next[level].getReference();
			}
			level-=D;
		}
		
		return x.value;
	}

	protected boolean find(int value, SprayListNode[] preds, SprayListNode[] succs)
	{
		boolean[] marked = {false};
		boolean snip;
		SprayListNode pred = null, curr = null, succ = null;
		retry:
			while (true) {
				pred = _head;
				for (int level = _maxAllowedHeight; level >= 0; level--) {
					curr = pred.next[level].getReference();
					while (true) {
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
					preds[level] = pred;
					succs[level] = curr;
				}
				return (curr.value == value);
			}
	}

	
	protected int randomStep(int max) {
		return ThreadLocalRandom.current().nextInt(max+1);
	}
}
