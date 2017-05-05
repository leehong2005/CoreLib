
# README

这个Demo分为两个工程### CoreLib该工程是一个Library，它里面包含了一些常用的类，大致类型有如下：
* activity：定义了activity类的基类* anim：定义了3D旋转的动画类* cache：包含了如何利用LruCache来避免OOM等。* data：定义了数据库实现的抽象类* task：包含了task的管理，实现了异步链式调用的功能* thread：定义了线程类* utils：包含了常用的util类，比如bitmap管理等。* widget：实现了Gallery Flow的类。
 最小SDK的Level:  12
### CoreLibTest该工程是测试CoreLib工程的。关于如何使用CoreLib中的类，请参考CoreLebTest工程。