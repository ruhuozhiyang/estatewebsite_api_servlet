## Tomcat source code notes & Some servlet codes🐯🐰🐲
### Intro.
It is the notes of learning Tomcat source codes mainly. By the way, write some codes about the 
backend api of an estate website using servlet.

### Details attached.
因为是用servlet写的服务，所以需要 servlet 容器来对外提供 web 服务，这里选用了 Tomcat。

Tomcat 的源码解析，见[Tomcat源码阅读笔记](./notes/Tomcat.md)。

### Build Simply
没有使用诸如 Maven 等大型项目打包工具，而是简单地使用编译器 Intellij 将 .java 源码编译为 .class 目标字节码文件；
并设置了导出路径，该路径是 Tomcat 能访问到的应用路径，在 server.xml 中配置。

对于第三方依赖包，手动地导入 WEB-INF/lib/ 目录下，并新建文件 web.xml，用于 URL 请求和 Servlet 的映射。