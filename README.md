## estatewebsite_api_servlet
### Intro.
the backend api of an estate property company's official website using servlet.

### Details attached.
因为是用servlet写的服务，所以需要servlet容器来对外提供web服务。这里选用了Tomcat servlet容器，同时也正好学习一下
Tomcat的内部架构和源码，见[笔记](./notes/Tomcat.md)。

没有使用诸如Maven等大型项目打包工具，而是简单地使用编译器Intellij将 .java源码编译为 .class目标文件，并设置了导出路径。
该路径是Tomcat容器能访问到的应用路径。

同时对于第三方依赖包，也手动地放入WEB-INF/lib/ 目录下；并新建文件web.xml，用于URL请求和Servlet的映射。