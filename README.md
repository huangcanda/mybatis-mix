# mybatis-mix
## 介绍
mybatis-mix是一款基于mybatis的功能增强框架。
## 特征

1. 只做增强，不影响mybatis原有功能的使用。
2. 轻量化设计，仅依赖mybatis本身，可选依赖druid数据源，与mybatis体系同步依赖spring体系。
3. 插拔式架构，项目里的所有功能模块均可通过SPI进行替换或扩展。
4. 全ORM，基于JPA规范，自动生成增删改查。
5. 追求代码质量，杜绝垃圾代码，优雅永不过时。
## 工程目录

- mybatis-mix-api             最小公共包，不依赖其他任何组件，被其他所有组件依赖
- mybatis-mix-apt            注解处理器，在编译时做处理。
- mybatis-mix-core          核心包，依赖mybatis，可不依赖spring直接运行。
- mybatis-mix-spring       spring包，依赖mybatis-spring，提供和spring集成的相关处理。 
- mybatis-mix-boot-starter   基于原生mybatis-spring-boot-starter的mybatis-mix启动器。
- mybatis-mix-test           测试工程，提供实例如何使用mybatis-mix。
## 依赖

- jdk >= 1.8
- mybatis >= 3.5.0
- druid >= 1.2.0   （可选，依赖其SQL解析模块）
## 
模块和功能

### CRUD实现

-      基础CRUD实现，基于SqlProvider机制。
-      通过注解提供乐观锁机制，逻辑删除机制。
-      提供多种更新机制，默认使用ExactUpdate机制，set哪个字段就更新哪个字段，基于apt实现。  
-      提供batch执行的CRUD，可以在同一个事务中随意使用多个执行器。
### Example增强
基于mybatis原生Example机制修改，统一抽象，提供三种Example使用方式

- SimpleExample：基于字符串拼接的方式实现
- LambdaExample：基于lambda表达式的方式实现。
- TypeExample： 根据实体的类型，基于apt生成对应的Example类。

Example机制可实现单表的较复杂查询，本人认为复杂SQL应该通过编写SQL来实现，而非java代码，因为可读性差，不直观。mybatis本是以SQL为核心的框架，如果开发人员非要使用java代码来编写，可选择集成QueryDsl辅助开发。
### 分页实现
    基于pagehelper修改而实现：

- 分页SQL和countSQL都基于MappedStatement和SqlSource管理，贴合mybatis的SQL执行机制。
- 基于SQL解析对count语句进行优化，提高count的执行效率。
- 分页的参数参与预编译，分页逻辑处在mybatis的一二级缓存之前。
- 提供多种方言分页，基于SPI方便扩展。
### SpringJPA风格支持
       在Mapper接口中基于SpringJPA风格定义方法名，自动执行方法名对应的sql语句。
### 填充机制
在执行sql的前后，自动填充值到参数中或者结果中。填充逻辑，填充条件和填充策略均基于SPI，可灵活扩展和替换。
### 日志增强

- 可开启全SQL打印功能。拼接参数，打印完整sql，可对sql进行美化输出。
- 打印执行的耗时，做慢sql检测。
- 打印影响行数或者查询结果行数或者count的记录数。
- 独立对mybatis的执行SQL日志打印进行控制，可全局控制或者临时控制，开启或者屏蔽某个SQL的打印。
### XML热部署
提供XML文件的热部署机制，在xml文件中修改SQL语句后，跟随idea的build在mybatis中更新SQL语句。供项目开发时使用，不建议生产使用。
### 其他
待补充。
## 快速开始
```
<dependency>
   <groupId>org.wanghailu</groupId>
   <artifactId>mybatis-mix-boot-starter</artifactId>
   <version>最新版本</version>
</dependency>
```
嗯...   该项目暂未发布到中央仓库，暂时只能下载代码打包再引用！！！
## 使用说明
见 mybatis-mix-test模块的测试用例。
