# FXApplication

## 这是什么

一个模块化的javafx桌面应用开发框架，提供java13及以上版本的应用
开发环境，其实这个主要是容器化的思路，ioc的那种，基本上来说受到spring
的风格影响很严重，当然我javafx之前是主要依靠springboot + javafxsupport
做的，但是springboot-javafx-support长久没有维护，而且spring在jdk11以上的
版本似乎有问题。

所以才开始做这个东西。

## 模块

#### 核心模块： fx.framework.core

基础模块，各种类的容器，Application基类以及注解等都在这，整体的启动代码，系统的启动和
关闭流程，生命周期等。

#### 拓展模块： fx.framework.jpa

数据库支持，仿照Spring的JpaRepository接口的数据库操作。
当然是没有那种基于方法名的查询，总感觉那个做起来很麻烦。

默认h2数据库

#### 拓展模块： fx.framework.resource

资源支持，统一的资源接口，支持从classPath，File，ModulePath
压缩文件直接提取数据。

对了，这个拓展会自动加载字体，以及当前theme的字体，内签了
Fontawesome和MaterialIcons两套字体图标。

有bug。

#### 拓展模块：fx.framework.deploy

系统集成，支持mac和windows，提供了进程操作api和文件关联。

## 测试
没写，没时间，以后加。

## 用法和Demo
以后想到了在写。

## 总结
自己用的一个框架，就这样。