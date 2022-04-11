## 1. Overview
Service 是一个接口。

Service 是一组一个或多个连接器，它们共享一个容器来处理它们传入的请求。例如，这种安排允许非 SSL 和 SSL 连接器共享相同的
web应用程序。给定的JVM可以包含任意数量的 Service 实例；但是，它们彼此完全独立，只共享基本JVM设施和系统类路径上的类。

类 StandardService 是 Service 接口的标准实现，关联的 Container 通常是 Engine 的实例，但这不是必需的。。
## 2. Members
## 2.1 ArrayList<Executor>
这个成员变量是这个 Service 持有的 executors 列表。Tomcat需要并发处理用户的请求，自然就需要线程池，而 Executor 就是
Tomcat 中的线程池。

初始化语句如下所示。
```markdown
protected final ArrayList<Executor> executors = new ArrayList<>();
```

org.apache.catalina.Executor 是个接口，类 [StandardThreadExecutor](./Service/StandardThreadExecutor.md) 
是其标准实现。

可以看见，Tomcat 自己构造一个 StandardThreadExecutor 而不是直接使用 ThreadPoolExecutor。体现了最少知识原则和
结构型-外观的设计模式。

在配置文件 server.xml 中可以配置某个 Service 的 Executor，代码如下所示。
```xml
<!-- 1. 属性说明 name: Service的名称 -->
<Service name="Catalina">
    <!--2. 一个或多个executors -->
    <Executor name="tomcatThreadPool" namePrefix="catalina-exec-"
        maxThreads="150" minSpareThreads="4"/>
</Service>  
```

如果在 server.xml 中配置了 Executor 标签，以及配置了 Executor 的 maxThreads 以及 minSpareThreads 属性，
那么 StandardService 中 Executor 的线程数量由此决定，如果没有配置，则默认数量区间为 25 ~ 200，当然如果 server.xml
标签中没有配置 Executor 标签，那么 StandardService 中 executors 的 size 为 0。

StandardService 只是负责 Executor 的启动，添加等操作，翻遍整个类，都没有找到 Executor 被使用的方法。这儿可以先记住
StandardService 中有一组 Executor，同时也记住 Executor 中线程数量在 25 ~ 200 之间。

```java
class ConnectorCreateRule {
  @Override
  public void begin(String namespace, String name, Attributes attributes)
      throws Exception {
    Service svc = (Service)digester.peek();
    Executor ex = null;
    if ( attributes.getValue("executor")!=null ) {
      //获取StandardService中的Executor
      ex = svc.getExecutor(attributes.getValue("executor"));
    }
    //创建了一个新的连接
    Connector con = new Connector(attributes.getValue("protocol"));

    if ( ex != null )  _setExecutor(con,ex);

    digester.push(con);
  }

  public void _setExecutor(Connector con, Executor ex) throws Exception {
    //可以看到通过反射找到了Connector中的ProtocolHandler的setExecutor方法
    //但ProtocolHandler的setExecutor方法其实是调用endpoint的setExecutor方法
    Method m = IntrospectionUtils.findMethod(con.getProtocolHandler().getClass(),"setExecutor",new Class[] {java.util.concurrent.Executor.class});
    if (m!=null) {
      m.invoke(con.getProtocolHandler(), new Object[] {ex});
    }else {
      log.warn("Connector ["+con+"] does not support external executors. Method setExecutor(java.util.concurrent.Executor) not found.");
    }
  }
}
```

## 2.2 connectors[]
这个成员变量是与这个 Service 相关的 Connectors 集合。

初始化语句如下所示。
```markdown
protected Connector connectors[] = new Connector[0];
``` 
