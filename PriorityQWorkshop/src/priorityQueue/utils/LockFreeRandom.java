package priorityQueue.utils;

public class LockFreeRandom {
	
	private int _seed;
	public LockFreeRandom(int seed) {
		_seed = seed;
	}
	
	public int nextInt()
	{
		int x = MarsagliaXORV(_seed);
		_seed = x;
		return x & 0x7fffffff;
	}

	private int MarsagliaXORV(int x) {
		if(x==0) x=1;
		x^= x<<6;
		x^=x>>>21;
		x^=x<<7;
		return x;
	}
	
	
}
