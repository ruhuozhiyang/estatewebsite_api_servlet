## 1. Overview
这个方法是在抽象类 AbstractEndpoint 中实现的。当 SocketChannel.isReadable() 或者 SocketChannel.isWritable()
为真时，调用执行。

部分源码如下所示。
```java
abstract class AbstractEndpoint {
  /**
   * Process the given SocketWrapper with the given status. Used to trigger
   * processing as if the Poller (for those endpoints that have one)
   * selected the socket.
   *
   * @param socketWrapper The socket wrapper to process
   * @param event         The socket event to be processed
   * @param dispatch      Should the processing be performed on a new
   *                          container thread
   *
   * @return if processing was triggered successfully
   */
  public boolean processSocket(SocketWrapperBase<S> socketWrapper,
      SocketEvent event, boolean dispatch) {
    try {
      if (socketWrapper == null) {
        return false;
      }
      SocketProcessorBase<S> sc = null;
      if (processorCache != null) {
        sc = processorCache.pop();
      }
      if (sc == null) {
        sc = createSocketProcessor(socketWrapper, event);
      } else {
        sc.reset(socketWrapper, event);
      }
      Executor executor = getExecutor();
      if (dispatch && executor != null) {
        executor.execute(sc);
      } else {
        sc.run();
      }
    } catch (RejectedExecutionException ree) {
      getLog().warn(sm.getString("endpoint.executor.fail", socketWrapper) , ree);
      return false;
    } catch (Throwable t) {
      ExceptionUtils.handleThrowable(t);
      // This means we got an OOM or similar creating a thread, or that
      // the pool and its queue are full
      getLog().error(sm.getString("endpoint.process.fail"), t);
      return false;
    }
    return true;
  }
}
```

## 2. Analysis
- 参数 dispatch 意为：处理是否应该在新的容器线程上执行；

关键代码如下。
```markdown
Executor executor = getExecutor();
if (dispatch && executor != null) {
    executor.execute(sc);
} else {
    sc.run();
}
```
首先获取到执行器 Executor，如果确认处理需要在新的容器线程上执行，且获取到的执行器 executor 不为空，那么就交给 executor
的 execute 处理，否则就直接启动 SocketProcessor 线程。

SocketProcessor 是继承自抽象类 SocketProcessorBase，该抽象类实现了 Runnable，重写了 run() 方法，在 run 方法
中调用了抽象方法 doRun()，然后 SocketProcessor 又实现了该抽象方法 doRun()。

这里其实是使用了模版设计模式，将**公共的操作流程**封装在抽象类 SocketProcessorBase 的方法 run() 中，对于抽象方法
doRun()，不同的实现类有着不同的实现，不管是什么样的 SocketProcessorBase 实现类，只需统一通过调用 
SocketProcessorBase.run() 就可以实现"因地制宜"。
