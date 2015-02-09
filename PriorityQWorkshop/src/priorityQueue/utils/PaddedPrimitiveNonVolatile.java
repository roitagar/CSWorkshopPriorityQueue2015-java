package priorityQueue.utils;

public class PaddedPrimitiveNonVolatile<T> {
  private long[] pad1;
  public T value;
  private long[] pad2;
  
  public PaddedPrimitiveNonVolatile(T value) {
    pad1 = new long[8];
    this.value = value;
    pad2 = new long[8];
  }
}

