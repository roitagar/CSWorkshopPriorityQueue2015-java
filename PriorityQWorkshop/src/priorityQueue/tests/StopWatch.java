package priorityQueue.tests;

class StopWatch {
  long startTime = 0;
  long stopTime;
  void startTimer() {
    startTime = System.currentTimeMillis();
  }
  void stopTimer() {
    stopTime = System.currentTimeMillis();    
  }
  long getElapsedTime() {
    return stopTime - startTime;
  }  
}