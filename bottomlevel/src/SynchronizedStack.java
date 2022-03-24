/**
 * 同步栈.
 * Synchronized修饰非静态方法，实际上是对调用该方法的对象加锁，俗称“对象锁”。
 *
 */
public class SynchronizedStack<T> {

  private static final int DEFAULT_SIZE = 128;
  private static final int DEFAULT_LIMIT = -1;

  private int size;
  private final int limit;
  private Object[] stacks;

  private int index = -1;

  public SynchronizedStack() {
    this(DEFAULT_SIZE, DEFAULT_LIMIT);
  }

  public SynchronizedStack(int size, int limit) {
    if (limit > -1 && size > limit) {
      this.size = limit;
    } else {
      this.size = size;
    }
    this.limit = limit;
    this.stacks = new Object[size];
  }

  public synchronized boolean push(T object) {
    index ++;
    if (index == stacks.length) {
      if (limit == -1 || size < limit) {
        expand();
      } else {
        index --;
        return false;
      }
    }
    stacks[index] = object;
    return true;
  }

  private void expand() {
    int newSize = size * 2;
    if (limit != -1 && newSize > limit) {
      newSize = limit;
    }
    Object[] newStacks = new Object[newSize];
    System.arraycopy(stacks, 0, newStacks, 0, size);
    stacks = newStacks;
    size = newSize;
  }

  public synchronized T pop() {
    if (index == -1) {
      return null;
    }
    T result = (T) stacks[index];
    stacks[index--] = null;
    return result;
  }

  public synchronized void clear() {
    if (index > -1) {
      for (int i = 0; i < index + 1; i++) {
        stacks[i] = null;
      }
    }
    index = -1;
  }

  public static void main(String[] args) {
    SynchronizedStack<Buffer> stack = new SynchronizedStack<>(1, 2);
    System.out.println(stack.push(new Buffer()));
    System.out.println(stack.push(new Buffer()));
    System.out.println(stack.push(new Buffer()));
  }

}
