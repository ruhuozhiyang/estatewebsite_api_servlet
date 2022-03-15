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

### Processor
It is an abstraction for the application layer.

Processor component is a protocol-handling component. It receives the socket from the Endpoint component, 
resolving the bytes stream data to the Request Object, and invokes the service of Adapter to transfer 
the Request Object to the ServletRequest Object used by the container.

### Adapter
Transfer the request object to the ServletRequest object the container needs.
