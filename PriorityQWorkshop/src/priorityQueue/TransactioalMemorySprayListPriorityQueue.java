package priorityQueue;

public class TransactioalMemorySprayListPriorityQueue extends SprayListPriorityQueue {

	public TransactioalMemorySprayListPriorityQueue(int maxAllowedHeight) {
		super(maxAllowedHeight);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void startInsert() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endInsert() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int randomLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void startDeleteMin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endDeleteMin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getNumberOfThreads() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int randomStep(int max) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected boolean canInsertBetween(SprayListNode pred, SprayListNode succ,
			int level) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void lockNode(SprayListNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void unlockNode(SprayListNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean readyToBeDeleted(SprayListNode victim) {
		// TODO Auto-generated method stub
		return false;
	}

}
