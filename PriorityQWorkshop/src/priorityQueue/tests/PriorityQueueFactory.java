package priorityQueue.tests;

import priorityQueue.news.*;

abstract class PriorityQueueFactory {
	abstract IPriorityQueue Create(int skiplistHeight);
	String getQueueType()
	{
		return this.getClass().getSimpleName().replaceAll("Factory", "");
	}
}

class JavaPriorityBlockingQueueFactory extends PriorityQueueFactory {
	IPriorityQueue Create(int skiplistHeight) {
		return new JavaPriorityBlockingQueue();
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

class TMSprayListPriorityQueueFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new TMSprayListPriorityQueue(skiplistHeight);
	}
}

class LockFreeSprayListPriorityQueueFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new LockFreeSprayListPriorityQueue(skiplistHeight);
	}
}

class CoolSprayListPriorityQueueFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new CoolSprayListPriorityQueue(skiplistHeight,false);
	}
}

class CoolSprayListPriorityQueueFairLockFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new CoolSprayListPriorityQueue(skiplistHeight,true);
	}
}

class OptimisticCoolSprayListPriorityQueueFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new OprimisticCoolSprayListPriorityQueue(skiplistHeight, false);
	}
}

class OptimisticCoolSprayListPriorityQueueFairLockFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new OprimisticCoolSprayListPriorityQueue(skiplistHeight, true);
	}
}

class LazyLockSparyListPriorityQueueFactory extends PriorityQueueFactory {
	@Override
	IPriorityQueue Create(int skiplistHeight) {
		return new LazyLockSparyListPriorityQueue(skiplistHeight);
	}
}
