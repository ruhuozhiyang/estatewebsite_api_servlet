## Overview
声明了一些 Endpoint 应该具有的基本属性/功能接口。针对不同协议或 I/O 模型的具体的 Endpoint 只需继承此抽象类，
然后扩展实现特定的 Endpoint。

该抽象类中已经实现的方法是所有实现类公用的，**也就是放在抽象类中实现，便于各实现类调用，复用**。

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

  protected void startAcceptorThread() {
    acceptor = new Acceptor<>(this);
    String threadName = getName() + "-Acceptor";
    acceptor.setThreadName(threadName);
    Thread t = new Thread(acceptor, threadName);
    t.setPriority(getAcceptorThreadPriority());
    t.setDaemon(getDaemon());
    t.start();
  }

  public abstract void startInternal() throws Exception;

  public final void start() throws Exception {
    if (bindState == BindState.UNBOUND) {
      bindWithCleanup();
      bindState = BindState.BOUND_ON_START;
    }
    startInternal();
  }
}
```
## Members
### [Acceptor](./common/acceptor.md)
成员 acceptor 和 AbstractEndPoint 是双向关联的。   
AbstractEndPoint 的方法 **startAcceptorThread()** 创建和启动 acceptor 线程；   
实现了 Runnable 的 acceptor 线程的 run 方法中会调用 AbstractEndpoint 中方法，例如: setSocketOptions()等。

## Process analysis
> 1.start过程
> 
> 抽象类 AbstractProtocol 继承实现接口 ProtocolHandler 的方法 start() 中会调用抽象类 AbstractEndpoint 的
> 方法 start()。
> 
> 上文给出的源代码中，抽象类 AbstractEndpoint 的方法 start() 会检查绑定状态后，调用抽象方法 startInternal()，
> 此抽象方法在各个实现类中有着不同的实现。
> 
> 以调用实现类 NioEndpoint 为例，其 startInternal() 方法中，会调用抽象类中的方法如：createExecutor、
> startAcceptorThread，而抽象类中的方法 startAcceptorThread 又会创建 Acceptor 线程并启动，后台运行。Acceptor
> 的方法 run() 中的具体内容可见 [acceptor.run](./common/acceptor.md)

## Implementations
### [NioEndpoint](./nio/nio_endpoint.md)

### [Nio2Endpoint]()
