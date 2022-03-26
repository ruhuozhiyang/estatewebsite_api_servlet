这个抽象类就是声明了一些 Endpoint 公有的属性或方法，然后各个具体的 Endpoint 对象继承并实现该抽象类的抽象方法。
以下为抽象类 AbstractEndpoint 的部分代码。
```java
public abstract class AbstractEndpoint {
  /**
   * External Executor based thread pool.
   */
  private Executor executor = null;
  /**
   * Thread used to accept new connections and pass them to worker threads.
   */
  protected Acceptor<U> acceptor;

  public void createExecutor() {
    internalExecutor = true;
    TaskQueue taskqueue = new TaskQueue();
    TaskThreadFactory tf = new TaskThreadFactory(getName() + "-exec-", daemon, getThreadPriority());
    executor = new ThreadPoolExecutor(getMinSpareThreads(), getMaxThreads(), 60, TimeUnit.SECONDS,taskqueue, tf);
    taskqueue.setParent( (ThreadPoolExecutor) executor);
  }
}
```

这里，成员变量 acceptor对象 和 AbstractEndPoint 是双向关联的。

acceptor 的run方法中会调用AbstractEndpoint中一些方法，例如: setSocketOptions()等。

与此同时，acceptor 线程是在 AbstractEndPoint 的方法 startAcceptorThread() 中创建和启动的。   
而 startAcceptorThread() 该方法又在各 AbstractEndPoint 实现类的方法 startInternal() 中被调用。

也可见该方法是所有实现类公用的，**也就是放在抽象类中实现，便于各实现类调用，复用**。

#### 4.1.4 the member of Endpoint: Acceptor


#### 4.1.2 The implementation of AbstractEndPoint: NioEndPoint