## What is Coyote ?
Coyote is the name of the Tomcat's connector component, and is the external api for the clients to 
visit. The clients build the connection with the server, send the request and receive the response 
through Coyote.

## The works Coyote does and Its Architecture.
Coyote encapsulates the underlying network communication, including the Socket request and handling. 
It provides unified interfaces for the Catalina container, **decoupling** Catalina from specific 
request protocol and I/O operation mode.

![img.png](../pics/catalina_coyote.png)

The pic above shows the interaction process between catalina and coyote. The coyote, as an isolated 
module, is only responsible for resolving the related specific communication protocol and I/O mode.
It is not related to the implementation of the Servlet specification.

It is worth noting that Coyote encapsulates the Socket input to the Request Object, which does not 
implement the servlet interface, and transfer the object to Catalina container. And then, Catalina 
will go on encapsulating the request object to the ServletRequest object, and handle further.

## I/O mode and Protocol supported in Coyote.

![img.png](../pics/io_protocol.png)

To support various I/O modes and application layer protocols, **one container in Tomcat is likely 
linked to several connectors**. Isolated container or connector can not provide public serves, which, 
therefore, needs them to work together, and forms a service.

Tomcat can have several services, and every service composes of several connectors and one container. 
**Considering flexibility of tomcat, configuring various services can allow us to visit various web 
applications in tomcat through various ports.**

## Components in Coyote.

![img.png](../pics/coyote_architecture.png)

The architecture of the Coyote component is shown in the pic above. It is mainly composed of three 
sub-components including Endpoint, Processor and Adapter logically.

![img.png](../pics/coyote_endpoint.png)
![img.png](../pics/coyote_components_details.png)

### EndPoint
It is an abstraction for the transport layer.

Logically, the EndPoint component is responsible for monitoring the communication port, receiving the 
socket data and sending it to the processor.

As shown in source codes, the abstract class called AbstractEndpoint encapsulates some common attributes 
or functions of the specific classes. We noticed the member acceptor, which is the thread used to accept
new connections and pass them to worker threads.
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

The Acceptor class implements the runnable interface and override the run function.
```java
class Acceptor implements Runnable {
  public enum AcceptorState {
    NEW, RUNNING, PAUSED, ENDED
  }
  
  protected volatile AcceptorState state = AcceptorState.NEW;
  
  @Override
  public void run() {

    int errorDelay = 0;
    long pauseStart = 0;

    try {
      // Loop until we receive a shutdown command
      while (!stopCalled) {

        // Loop if endpoint is paused.
        // There are two likely scenarios here.
        // The first scenario is that Tomcat is shutting down. In this
        // case - and particularly for the unit tests - we want to exit
        // this loop as quickly as possible. The second scenario is a
        // genuine pause of the connector. In this case we want to avoid
        // excessive CPU usage.
        // Therefore, we start with a tight loop but if there isn't a
        // rapid transition to stop then sleeps are introduced.
        // < 1ms       - tight loop
        // 1ms to 10ms - 1ms sleep
        // > 10ms      - 10ms sleep
        while (endpoint.isPaused() && !stopCalled) {
          if (state != AcceptorState.PAUSED) {
            pauseStart = System.nanoTime();
            // Entered pause state
            state = AcceptorState.PAUSED;
          }
          if ((System.nanoTime() - pauseStart) > 1_000_000) {
            // Paused for more than 1ms
            try {
              if ((System.nanoTime() - pauseStart) > 10_000_000) {
                Thread.sleep(10);
              } else {
                Thread.sleep(1);
              }
            } catch (InterruptedException e) {
              // Ignore
            }
          }
        }

        if (stopCalled) {
          break;
        }
        state = AcceptorState.RUNNING;

        try {
          //if we have reached max connections, wait
          endpoint.countUpOrAwaitConnection();

          // Endpoint might have been paused while waiting for latch
          // If that is the case, don't accept new connections
          if (endpoint.isPaused()) {
            continue;
          }

          U socket = null;
          try {
            // Accept the next incoming connection from the server
            // socket
            socket = endpoint.serverSocketAccept();
          } catch (Exception ioe) {
            // We didn't get a socket
            endpoint.countDownConnection();
            if (endpoint.isRunning()) {
              // Introduce delay if necessary
              errorDelay = handleExceptionWithDelay(errorDelay);
              // re-throw
              throw ioe;
            } else {
              break;
            }
          }
          // Successful accept, reset the error delay
          errorDelay = 0;

          // Configure the socket
          if (!stopCalled && !endpoint.isPaused()) {
            // setSocketOptions() will hand the socket off to
            // an appropriate processor if successful
            if (!endpoint.setSocketOptions(socket)) {
              endpoint.closeSocket(socket);
            }
          } else {
            endpoint.destroySocket(socket);
          }
        } catch (Throwable t) {
          ExceptionUtils.handleThrowable(t);
          String msg = sm.getString("endpoint.accept.fail");
          // APR specific.
          // Could push this down but not sure it is worth the trouble.
          if (t instanceof Error) {
            Error e = (Error) t;
            if (e.getError() == 233) {
              // Not an error on HP-UX so log as a warning
              // so it can be filtered out on that platform
              // See bug 50273
              log.warn(msg, t);
            } else {
              log.error(msg, t);
            }
          } else {
            log.error(msg, t);
          }
        }
      }
    } finally {
      stopLatch.countDown();
    }
    state = AcceptorState.ENDED;
  }
}
```
The NioEndPoint class extends the abstract class AbstractEndPoint and implements the function startInternal.
```java
class NioEndPoint {
  /**
   * Start the NIO endpoint, creating acceptor, poller threads.
   */
  @Override
  public void startInternal() throws Exception {

    if (!running) {
      running = true;
      /**
       * ...
       */
      
      // Create worker collection
      if (getExecutor() == null) {
        createExecutor();
      }

      initializeConnectionLatch();

      // Start poller thread
      poller = new Poller();
      Thread pollerThread = new Thread(poller, getName() + "-Poller");
      pollerThread.setPriority(threadPriority);
      pollerThread.setDaemon(true);
      pollerThread.start();

      startAcceptorThread();
    }
  }
}
```

### Processor
It is an abstraction for the application layer.

Processor component is a protocol-handling component. It receives the socket from the Endpoint component, 
resolving the bytes stream data to the Request Object, and invokes the service of Adapter to transfer 
the Request Object to the ServletRequest Object used by the container.

### Adapter
Transfer the request object to the ServletRequest object the container needs.
