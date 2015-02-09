package priorityQueue.utils;


public class PaddedPrimitive<T> {
  private long[] pad1;
  public volatile T value;
  private long[] pad2;
  
  public PaddedPrimitive(T value) {
    pad1 = new long[8]; // one cache line worth of data
    this.value = value;
    pad2 = new long[8]; // on either side of the precious variable...
  }
}