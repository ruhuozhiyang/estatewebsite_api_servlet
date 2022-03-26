#### 4.1.7 SocketProperties
The SocketProperties class is the Properties that can be set in the <Connector> element in the Tomcat
configuration file server.xml. All properties are prefixed with "socket." and are currently only
working for the Nio connector.

The class has an attribute named **bufferPool**, which is the NioChannel pool size for the endpoint,
namely the number of the channels and -1 means unlimited cached, 0 means no cache.

The attribute bufferPoolSize is Buffer pool size in bytes to be cached, which will change dynamically
according to JVM max heap in Tomcat 10. 