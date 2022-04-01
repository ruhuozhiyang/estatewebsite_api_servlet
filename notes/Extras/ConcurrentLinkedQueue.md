## 1. background
在单线程编程中我们会经常用到一些集合类，比如 ArrayList,HashMap 等，但是这些类都不是线程安全的类。

比如 ArrayList 不是线程安全的，Vector 是线程安全。
而保障 Vector 线程安全的方式，是非常粗暴的在方法上用**synchronized独占锁，将多线程执行变成串行化**。
要想将 ArrayList 变成线程安全的也可以使用Collections.synchronizedList(List<T> list)方法 ArrayList 
转换成线程安全的，但这种转换方式依然是通过 synchronized 修饰方法实现的，很显然这不是一种高效的方式。

同时，队列也是我们常用的一种数据结构，为了解决线程安全的问题，Doug Lea 大师为我们准备了 ConcurrentLinkedQueue 
这个线程安全的队列。从类名就可以看的出来实现队列的数据结构是链式。

