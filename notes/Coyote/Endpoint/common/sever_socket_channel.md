## 1. Overview
在默认的阻塞模式下，accept() 方法会一直阻塞直到有新连接到达。

ServerSocketChannel 可以设置成非阻塞模式。在非阻塞模式下，accept() 方法会立刻返回，如果没有新进来的连接，
返回 null。因此，通过检查返回的 SocketChannel 是否为 null 来判断是否有请求连接。

在使用accept函数的时候，如果服务器面向多个客户的连接，必须使用while循环，原因如下：
> 每一次循环都会阻塞在 accept()，不会往下执行代码，等待新连接到来；
> 
> 一旦有连接到来，accept() 就会返回新 SocketChannel，并且往下执行剩余代码，然后进入下一次循环，继续阻塞在 
> accept()，等待新连接；
> 
> 如果不使用 while 循环，一个连接处理接收处理完后程序就结束了，不会继续监听其它连接；
>
> 必须使用数组（或其他数据结构）保存当前accept创建的socket，否则下次新的socket建立后，当前的socket将不受控制。

关于 ServerSocketChannel 的介绍和使用可见[链接🔗](https://www.cnblogs.com/binarylei/p/9977580.html)

测试代码可见 [ServerSocketChannel](../../../../bottomlevel/src/ServerSocketChannelTest.java)

ServerSocketChannel.socket().bind() 中绑定的是一个 endpoint: SocketAddress，The IP address and port 
number to bind to 。

一个端口号只能对应一个 ServerSocketChannel。

## 2. Functions
### 2.1 register()
如下代码所示，register() 有两个重载方法。
```markdown
SelectionKey register(Selector sel, int ops) {
    return register(sel, ops, null);
}
SelectionKey register(Selector sel, int ops, Object att);
```
