/**
 * 这是Tomcat源码中实现的线程安全的队列。
 * 是一个替代java.util.concurrent.ConcurrentLinkedQueue的GC-free方案，
 * 满足基本功能的基础上，尽量减少garbage的产生。
 *
 * 有个问题就是如果创建size为0的queue，那么offer添加就会报错，
 * 自定义的SynchronizedQueue中对这种情况是自行扩容至size=1。
 * @param <T>
 */
public class SynchronizedQueueTomcat<T> {
  public static final int DEFAULT_SIZE = 128;

  private Object[] queue;
  private int size;
  private int insert = 0;
  private int remove = 0;

  public SynchronizedQueueTomcat() {
    this(DEFAULT_SIZE);
  }

  public SynchronizedQueueTomcat(int initialSize) {
    queue = new Object[initialSize];
    size = initialSize;
  }

  public synchronized boolean offer(T t) {
    queue[insert++] = t;

    // Wrap
    if (insert == size) {
      insert = 0;
    }

    if (insert == remove) {
      expand();
    }
    return true;
  }

  public synchronized T poll() {
    if (insert == remove) {
      // empty
      return null;
    }

    @SuppressWarnings("unchecked")
    T result = (T) queue[remove];
    queue[remove] = null;
    remove++;

    // Wrap
    if (remove == size) {
      remove = 0;
    }

    return result;
  }

  private void expand() {
    int newSize = size * 2;
    Object[] newQueue = new Object[newSize];

    System.arraycopy(queue, insert, newQueue, 0, size - insert);
    System.arraycopy(queue, 0, newQueue, size - insert, insert);

    insert = size;
    remove = 0;
    queue = newQueue;
    size = newSize;
  }

  public synchronized int size() {
    int result = insert - remove;
    if (result < 0) {
      result += size;
    }
    return result;
  }

  public synchronized void clear() {
    queue = new Object[size];
    insert = 0;
    remove = 0;
  }

  public static void main(String[] args) {
    SynchronizedQueueTomcat<Integer> sqt = new SynchronizedQueueTomcat<>(0);
    sqt.offer(4);
  }
}
