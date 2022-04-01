import java.util.concurrent.atomic.AtomicLong;

/**
 * JDK在concurrent包里提供了一些线程安全的基本数据类型的实现，
 * 比如 Long型对应的concurrent包的类是AtomicLong。
 */
public class AtomicLongTest {

  private static final int threadNum = 50;
  private static final int timeMs = 50;

  public static void main(String[] args) {
    for (int i = 0; i < threadNum; i++) {
      Thread thread = new Thread(() -> {
        try {
          Thread.sleep(timeMs);
          if (ConCurrentCounter.addOne() == threadNum) {
            System.out.println("counter = " + threadNum);
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });
      thread.start();
    }
    System.out.println("线程启动完毕");
  }
}

/**
 * 线程不安全。
 * Counter 类在 addOne()方法被调用时，并不能保证线程的安全，即它不是原子级别的运行性。
 * 结果就是大多数情况下都不会输出"counter = 100"。
 */
class Counter {
  private static long counter = 0;
  public static long addOne(){
    return ++ counter;
  }
}

/**
 * 线程安全的计数器。
 * 每次运行都会输出"counter = 100"。
 */
class ConCurrentCounter {
  private static AtomicLong counter = new AtomicLong(0);
  public static long addOne(){
    return counter.incrementAndGet();
  }
}
