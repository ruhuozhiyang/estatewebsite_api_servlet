## Java EE   
Java EE是一种架构,也可以说是一种技术规范. Java EE 是由一系列抽象的标准规范所组成,
是针对企业级软件开发中普遍面临问题的一套解决方案. Java EE 是将JSR文档以代码
的形式展现出来 **(每个抽象的JSR规范都是要求有对应的参考实现的.)**.

也就是说, Java EE = 多个JSR正式规范 + 运行环境.

Java EE 作为一套技术规范,其商业实现产品有多种,单单国产的Java EE应用服务器实现就有3个. "一种规范 + 多种实现"的方式
对企业非常友好, 当企业遇到某供应商产品服务不靠谱的话迁移起来就很容易.

Java EE SDK则是java提供的用于开发企业级应用的开发工具包. 它是在Java SE SDK的基础上添加了一些额外的jar包.
用于支持企业级应用开发. 包括但不限于: ejb,servlet,jsp,xml,jpa等;   

企业级应用开发一般分为两个阵营: **1、使用Java EE开发; 2、使用Spring这种所谓的轻量级企业应用框架开发;**

相关概念:  
1、JCP 与 JSR.  
Java Community Process是一个由oracle（曾经是sun）领导的,负责管理java和接受各种Java Specification Requests的
组织,这个组织很多大厂（例如谷歌，IBM等）都加入了. 
Java Specification Request(JSR)是java的spec,在没有正式确定某版本之前会存在很多Java Specification Requests,
最终JSR会由JCP的成员投票决定. 例如: lambda在JSR335的相关讨论.

## Web.xml
如果 Web 应用不包含任何 servlet、过滤器、或监听器组件或使用注解声明相同的，那么可以不需要 web.xml 文件。
换句话说，只包含静态文件或 JSP 页面的应用程序并不需要一个 web.xml 的存在。

