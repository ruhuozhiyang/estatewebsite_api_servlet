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
linked to several connectors**. Isolated container or connector can not provide public serves, which 
needs them to work together, and forms a service namely.

Tomcat can have several services, and every service composes of several connectors and one container. 
**Considering flexibility of tomcat, configuring various services can allow us to visit various web 
applications in tomcat through various ports.**
