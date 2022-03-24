public class SynchronizedQueue<T> {

  private static final int DEFAULT_SIZE = 128;

  private int size;
  private Object[] queue;
  private int addFlag = -1;
  private int removeFlag = -1;

  public SynchronizedQueue() {
    this(DEFAULT_SIZE);
  }

  public SynchronizedQueue(int size) {
    queue = new Object[size];
    this.size = size;
  }

  /**
   * 从末尾添加元素，FIFO。
   * @param t T.
   * @return boolean.
   */
  public boolean add(T t) {
    addFlag ++;
    if (addFlag == size && removeFlag > -1) {
      move();
    }
    if (addFlag == size && removeFlag == -1) {
      expand();
    }
    queue[addFlag] = t;
    return true;
  }

  /**
   * 弹出最早进来的顶元素。
   * @return
   */
  public T poll() {
    if (addFlag == removeFlag) {
      return null;
    }
    removeFlag ++;
    T result = (T) queue[removeFlag];
    queue[removeFlag] = null;

    return result;
  }

  /**
   * 当队列满时，扩容。
   */
  private void expand() {
    int newSize = size * 2;
    Object[] newQueue = new Object[newSize];

    System.arraycopy(queue, 0, newQueue, 0, size);

    addFlag = size;
    removeFlag = -1;
    queue = newQueue;
    size = newSize;
  }

  /**
   * 调整数据分布，数据整体往前移动，填补删除后所产生的空间；
   * 增加成员始终从后面增加。
   * 得排除队列满的情况。
   */
  private void move() {
    if (removeFlag == size - 1) {
      removeFlag = -1;
      addFlag = 0;
      return;
    }
    for (int i = removeFlag; i < size - 1; i++) {
      queue[i - removeFlag] = queue[i + 1];
      queue[i + 1] = null;
      addFlag = i - removeFlag + 1;
    }
    removeFlag = -1;
  }

  public String getAll() {
    String res = "";
    for (Object t: queue) {
      res += t;
    }
    return res;
  }

  public static void main(String[] args) {
    SynchronizedQueue<Integer> queue = new SynchronizedQueue<>(3);
    queue.testAdd(queue);
    queue.testExpand(queue);
    queue.testPollPart(queue);
    queue.testPollPartAndAddFull(queue);
    queue.testPollAll(queue);
    queue.testAdd(queue);
    queue.testAddFullAndExpand(queue);
    queue.testPollPart(queue);
//    queue.testAddFullAndMove(queue);
    queue.testAddFullAndMoveAndExpand(queue);
  }

  private void testAdd(SynchronizedQueue<Integer> queue) {
    queue.add(5);
    queue.add(6);
    queue.add(7);
    System.out.println("test add result:" + queue.getAll());
  }

  private void testExpand(SynchronizedQueue<Integer> queue) {
    queue.add(9);
    System.out.println("test expand result:" + queue.getAll());
  }

  private void testPollPart(SynchronizedQueue<Integer> queue) {
    queue.poll();
    queue.poll();
    System.out.println("test pollPart result:" + queue.getAll());
  }

  private void testPollPartAndAddFull(SynchronizedQueue<Integer> queue) {
    queue.add(5);
    queue.add(6);
    System.out.println("test pollPartAndAddFull result:" + queue.getAll());
  }

  private void testPollAll(SynchronizedQueue<Integer> queue) {
    queue.poll();
    queue.poll();
    queue.poll();
    queue.poll();
    System.out.println("test pollAll result:" + queue.getAll());
  }

  private void testAddFullAndExpand(SynchronizedQueue<Integer> queue) {
    queue.add(5);
    queue.add(6);
    queue.add(7);
    queue.add(8);
    System.out.println("testAddFullAndExpand result:" + queue.getAll());
  }

  private void testAddFullAndMove(SynchronizedQueue<Integer> queue) {
    queue.add(5);
    queue.add(6);
    queue.add(7);
    queue.add(8);
    queue.add(7);
    queue.add(8);
    System.out.println("testAddFullAndMove result:" + queue.getAll());
  }

  private void testAddFullAndMoveAndExpand(SynchronizedQueue<Integer> queue) {
    queue.add(5);
    queue.add(6);
    queue.add(7);
    queue.add(8);
    queue.add(7);
    queue.add(8);
    queue.add(7);
    queue.add(8);
    System.out.println("testAddFullAndMoveAndExpand result:" + queue.getAll());
  }

}
