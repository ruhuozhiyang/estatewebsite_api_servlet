## What is Coyote ?
Coyote is the name of the Tomcat's connector component, and is the external api for the clients to 
visit. The clients build the connection with the server, send the request and receive the response 
through Coyote.

## The works Coyote does and Its Architecture.
Coyote encapsulates the underlying network communication, including the Socket request and handling. 
It provides unified interfaces for the Catalina container, **decoupling** Catalina from specific 
request protocol and I/O operation mode.

![tomcat_connector](../pics/tomcat-connector.png)

The pic above shows the interaction process between catalina and coyote. The coyote, as an isolated 
module, is only responsible for resolving the related specific communication protocol and I/O mode.
It is not related to the implementation of the Servlet specification.

It is worth noting that Coyote encapsulates the Socket input to the Request Object, which does not 
implement the servlet interface, and transfer the object to Catalina container. And then, Catalina 
will go on encapsulating the request object to the ServletRequest object, and handle further.

## I/O mode and Protocol supported in Coyote.
下面图片中展示的是 Tomcat9 Connector 支持的 I/O 模型以及通信协议。（图片来源于网络）
![img.png](../pics/io_protocol.png)

To support various I/O modes and application layer protocols, **one container in Tomcat is likely 
linked to several connectors**. Isolated container or connector can not provide public serves, which, 
therefore, needs them to work together, and forms a service.

Tomcat can have several services, and every service composes of several connectors and one container. 
**Considering flexibility of tomcat, configuring various services can allow us to visit various web 
applications in tomcat through various ports.**

## 4. Components in Coyote.
通过上文中图片我们也能看到，Coyote主要由两部分组成：ProtocolHandler and Adapter，其中 ProtocolHandler 又是由
Endpoint 和 Processor 组成。

### 4.1 Endpoint
It is an abstraction for the transport layer. Logically, the EndPoint component is responsible for 
monitoring the communication port, receiving the socket data and sending it to the processor.

抽象类 [AbstractEndpoint](./Coyote/Endpoint/abstract_endpoint.md) 中声明了一些 Endpoint 应该具有的基本属性
/功能接口。针对不同协议或 I/O 模型的具体的 Endpoint 只需继承此抽象类，然后扩展实现特定的 Endpoint。

### 4.2 Processor
It is an abstraction for the application layer.

Processor component is a protocol-handling component. It receives the socket from the Endpoint component, 
resolving the bytes stream data to the Request Object, and invokes the service of Adapter to transfer 
the Request Object to the ServletRequest Object used by the container.

### 4.3 Adapter
Transfer the request object to the ServletRequest object the container needs.

