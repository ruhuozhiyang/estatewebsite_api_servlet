public class SynchronizedQueue<T> {

  private static final int DEFAULT_SIZE = 128;

  private int size;
  private Object[] queue;
  private int offerFlag = -1;
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
  public synchronized boolean offer(T t) {
    offerFlag ++;
    if (offerFlag == size && removeFlag > -1) {
      move();
    }
    if (offerFlag == size && removeFlag == -1) {
      expand();
    }
    queue[offerFlag] = t;
    return true;
  }

  /**
   * 弹出最早进来的顶元素。
   * @return
   */
  public synchronized T poll() {
    if (offerFlag == removeFlag) {
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
    int newSize = size == 0 ? 1 : size * 2;
    Object[] newQueue = new Object[newSize];

    System.arraycopy(queue, 0, newQueue, 0, size);

    offerFlag = size;
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
      offerFlag = 0;
      return;
    }
    for (int i = removeFlag; i < size - 1; i++) {
      queue[i - removeFlag] = queue[i + 1];
      queue[i + 1] = null;
      offerFlag = i - removeFlag + 1;
    }
    removeFlag = -1;
  }

  public synchronized int size() {
    return offerFlag - removeFlag;
  }

  public synchronized void clear() {
    queue = new Object[size];
    offerFlag = -1;
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
//    SynchronizedQueue<Integer> queue0 = new SynchronizedQueue<>(0);
//    queue0.offer(1);
//    System.out.println(queue0.getAll());
//    queue0.offer(5);
//    System.out.println(queue0.getAll());
//    queue0.offer(5);
//    System.out.println(queue0.getAll());
    /**
     * 1
     * 15
     * 155null
     */

    SynchronizedQueue<Integer> queue = new SynchronizedQueue<>(3);
    queue.testAdd(queue);
    queue.testExpand(queue);
    queue.testPollPart(queue);
    queue.testAddFull_PollPart_Move(queue);
    queue.testPollAll(queue);
    queue.testAdd(queue);
    queue.testAddFullAndExpand(queue);
    queue.testPollPart(queue);
//    queue.testAddFullAndMove(queue);
    queue.testAddFullAndMoveAndExpand(queue);
    System.out.println(queue.size());
    /**
     * test offer result:567
     * test expand result:5679nullnull
     * test pollPart result:nullnull79nullnull
     * testAddFull_PollPart_Move result:69nullnullnullnull
     * test pollAll result:nullnullnullnullnullnull
     * test offer result:nullnull567null
     * testAddFullAndExpand result:5675678nullnullnullnullnull
     * test pollPart result:nullnull75678nullnullnullnullnull
     * testAddFullAndMoveAndExpand result:7567856787878nullnullnullnullnullnullnullnullnullnullnull
     */
  }

  private void testAdd(SynchronizedQueue<Integer> queue) {
    queue.offer(5);
    queue.offer(6);
    queue.offer(7);
    System.out.println("test offer result:" + queue.getAll());
  }

  private void testExpand(SynchronizedQueue<Integer> queue) {
    queue.offer(9);
    System.out.println("test expand result:" + queue.getAll());
  }

  private void testPollPart(SynchronizedQueue<Integer> queue) {
    queue.poll();
    queue.poll();
    System.out.println("test pollPart result:" + queue.getAll());
  }

  private void testAddFull_PollPart_Move(SynchronizedQueue<Integer> queue) {
    queue.offer(5);
    queue.offer(6);
    queue.poll();
    queue.poll();
    queue.poll();
    queue.offer(9);
    System.out.println("testAddFull_PollPart_Move result:" + queue.getAll());
  }

  private void testPollAll(SynchronizedQueue<Integer> queue) {
    queue.poll();
    queue.poll();
    queue.poll();
    queue.poll();
    System.out.println("test pollAll result:" + queue.getAll());
  }

  private void testAddFullAndExpand(SynchronizedQueue<Integer> queue) {
    queue.offer(5);
    queue.offer(6);
    queue.offer(7);
    queue.offer(8);
    System.out.println("testAddFullAndExpand result:" + queue.getAll());
  }

  private void testAddFullAndMove(SynchronizedQueue<Integer> queue) {
    queue.offer(5);
    queue.offer(6);
    queue.offer(7);
    queue.offer(8);
    queue.offer(7);
    queue.offer(8);
    System.out.println("testAddFullAndMove result:" + queue.getAll());
  }

  private void testAddFullAndMoveAndExpand(SynchronizedQueue<Integer> queue) {
    queue.offer(5);
    queue.offer(6);
    queue.offer(7);
    queue.offer(8);
    queue.offer(7);
    queue.offer(8);
    queue.offer(7);
    queue.offer(8);
    System.out.println("testAddFullAndMoveAndExpand result:" + queue.getAll());
  }

}
