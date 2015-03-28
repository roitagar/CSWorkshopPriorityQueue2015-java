package priorityQueue.news;


public class OprimisticCoolSprayListPriorityQueue extends CoolSprayListPriorityQueue {



	public OprimisticCoolSprayListPriorityQueue(int maxAllowedHeight, boolean useItemsCounter, boolean fair) {
		super(maxAllowedHeight, useItemsCounter, fair);
	}

	@Override
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

			// Coherency check:
			if(_elimArray.hasNodes())
			{
				// Someone else performed cleanup and I missed it, go back to empty the elimination array
				return false;
			}

			/* Determine the max number of Healthy element you want to traverse */
			int p = _threads.get();
			p = p*(int)(Math.log(p)/Math.log(2)) + 1;
			int numOfHealtyNodes = p;
			/* Create an Elimination Array in this size */
			newElimArray = new NodesEliminationArray(numOfHealtyNodes);

			/* Traverse the list in the bottom level look for healthy element, and find an *Optimistic* optimal group */
			foundHealthyNodes = 0;
			maxLevelFound = 0;
			firstNode = _head.next[0].getReference();
			curr = firstNode;
			highest = curr;
			while (foundHealthyNodes < numOfHealtyNodes && curr != _tail) {
				if (!curr.isDeleted()) {
					foundHealthyNodes++;
				}
				/* find the last highest node in the ragne */
				if (maxLevelFound <= curr.topLevel()) {
					highest = curr;
					maxLevelFound = curr.topLevel();

					// TODO: Compare live-dead element ratios?
				}
				curr = curr.next[0].getReference();
			}
			if(firstNode == _tail)
			{
				// No nodes to remove
				highestNodeKey = null;
				return false;
			}

			//Now we have and optimistic list
			// Block inserters for a while and update higestNodeKey
			_lock2.writeLock().lock(); //promise no inserters in the list at all - for a short time
			_lock3.writeLock().lock();
			try {
				try{
					highestNodeKey = highest.value; //now we are sure all the inserters has read this value and won't disturb us.	
				}
				finally {
					_lock2.writeLock().unlock(); //allow only high inserters - high-valued inserts can go on
				}
				
				//now search again for the list
				len = 0;
				actualLen = 0;
				foundHealthyNodes = 0;
				maxLevelFound = 0;
				firstNode = _head.next[0].getReference();
				curr = firstNode;
				highest = curr;
				while (foundHealthyNodes < numOfHealtyNodes && curr != _tail && curr.value <= highestNodeKey) {
					len++;
					if (!curr.isDeleted()) {
						foundHealthyNodes++;
					}
					/* find the last highest node in the ragne */
					if (maxLevelFound <= curr.topLevel()) {
						highest = curr;
						maxLevelFound = curr.topLevel();
						actualLen = len;
					}
					curr = curr.next[0].getReference();
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

}
