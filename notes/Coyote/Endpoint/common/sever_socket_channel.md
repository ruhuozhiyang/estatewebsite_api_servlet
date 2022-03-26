在默认的阻塞模式下，accept() 方法会一直阻塞直到有新的连接到达。ServerSocketChannel 可以设置成非阻塞模式。

在非阻塞模式下，accept() 方法会立刻返回，如果没有新进来的连接，返回 null。因此，通过检查返回的 SocketChannel 是否
为 null 来判断是否有请求连接。

关于 ServerSocketChannel 的介绍和使用可见[链接🔗](https://www.cnblogs.com/binarylei/p/9977580.html) 。

在使用accept函数的时候，如果服务器面向多个客户的连接，

必须使用while循环，每次循环阻塞在accept函数，等待新的连接到来，这样才能返回新的socket。如果不使用while死循环每次阻塞在accept函数，也可以面向多个客户连接，此时将在socket抽象层，自动建立socket，并且该socket不受控制
必须使用数组（或其他数据结构）保存当前accept创建的socket，否则下次新的socket建立后，当前的socket将不受控制。