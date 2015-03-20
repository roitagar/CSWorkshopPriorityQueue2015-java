package priorityQueue.tests;

import priorityQueue.news.*;

abstract class PriorityQueueFactory {
	abstract IPriorityQueue Create(int skiplistHeight);
	String getQueueType()
	{
		return this.getClass().getSimpleName().replaceAll("Factory", "");
	}
}

class NaiveLockNativePriorityQueueFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new NaiveLockNativePriorityQueue();
	}
}

class GlobalLockSprayListPriorityQueueFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new GlobalLockSprayListPriorityQueue(skiplistHeight);
	}
}

class TMSprayListPriorityQueueWithCounterFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new TMSprayListPriorityQueue(skiplistHeight, true);
	}
}

class TMSprayListPriorityQueueWithoutCounterFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new TMSprayListPriorityQueue(skiplistHeight, false);
	}
}

class LockFreeSprayListPriorityQueueFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new LockFreeSprayListPriorityQueue(skiplistHeight);
	}
}

class CoolSprayListPriorityQueueWithItemsCounterFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new CoolSprayListPriorityQueue(skiplistHeight, true);
	}
}

class CoolSprayListPriorityQueueWithImpreciseIsEmptyFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new CoolSprayListPriorityQueue(skiplistHeight, false);
	}
}

class LazyLockSparyListPriorityQueueFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new LazyLockSparyListPriorityQueue(skiplistHeight);
	}
}

// TODO: is this required?
class SeqSprayListPriorityQueueFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new SeqSprayListPriorityQueue(skiplistHeight);
	}
}
