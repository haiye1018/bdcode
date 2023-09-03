# 1.前期准备工作

1、分别创建ZookeeperC1、2、3

2、在ZookeeperC1的子容器hadoopc1/hadoopc1中操作，New terminal

3、切换到root用户。命令：sudo /bin/bash

4、进入hadoop目录下并查看有哪些文件夹。

5、运行initHost.sh脚本，进行三台机器的认证：./initHosts.sh 。

命令：./initHosts.sh

6、切换到hadoop用户下，进入hadoop根目录，命令：su - hadoop、cd /hadoop/ 

7、启动zookeeper集群，命令：./startAll.sh

# 2.Hadoop安装

1、 在app-11上，确保是hadoop用户，如当前不是hadoop用户，则按照前面的步骤（sudo /bin/bash、su – hadoop）切换到hadoop用户；因为，后面的很多操作可能不是一次性完成了，当关闭了当前连接后再次New terminal时进入的是user用户。

2、 切到hadoop根目录下。命令：cd /hadoop/

3、 创建一个安装hadoop的根目录。命令：mkdir Hadoop

4、 进入到Hadoop目录下。命令：cd Hadoop/

5、 下载hadoop安装包，

命令：wget https://archive.apache.org/dist/hadoop/common/hadoop-3.1.2/hadoop-3.1.2.tar.gz

6、 解压安装包，命令：tar -xf hadoop-3.1.2.tar.gz

7、 进入到/tmp目录下，并创建config目录。命令：cd /tmp/、mkdir config、cd config

8、 通过GitHub下载Spark-stack。命令：git clone https://github.com/haiye1018/Hadoop

9、 进入Hadoop/目录下。命令：cd Hadoop/

10、此时当前目录为/tmp/config/Hadoop。命令：pwd

11、将conf拷贝到hadoop-3.1.2/etc/hadoop目录下，并查看是否成功

命令：cp conf/* /hadoop/Hadoop/hadoop-3.1.2/etc/hadoop/、

cat /hadoop/Hadoop/hadoop-3.1.2/etc/hadoop/workers

12、在环境变量中加入hadoop和HADOOP_HOME路径，命令：vi ~/.bashrc

export HADOOP_HOME=/hadoop/Hadoop/hadoop-3.1.2

export PATH=${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin:${HADOOP_HOME}/lib:$PATH

13、将设置的环境变量生效，命令：source ~/.bashrc

14、echo $PATH

## 2.1将hadoop安装包拷贝集群中的另两台机器上

14、先创建安装的根目录，命令：ssh hadoop@app-12 "mkdir /hadoop/Hadoop"、ssh hadoop@app-13 "mkdir /hadoop/Hadoop"

15、将环境变量拷贝到集群的另外两个机器上，命令：scp ~/.bashrc hadoop@app-12:~/、scp ~/.bashrc hadoop@app-13:~/

16、 将改好的安装包拷贝到集群的另外两个机器上。命令：cd /hadoop/Hadoop、scp -r -q hadoop-3.1.2 hadoop@app-12:/hadoop/Hadoop、scp -r -q hadoop-3.1.2 hadoop@app-13:/hadoop/Hadoop

## 2.2hadoop初始化工作

17、 需要将所有的journalnode守护进程启动，接下来是用一个循环脚本启动。

命令：for name in app-11 app-12 app-13; do ssh hadoop@$name "hdfs --daemon start journalnode"; done

18、 查看journalnode是否启动，命令：for name in app-11 app-12 app-13; do ssh hadoop@$name "jps"; done 注：显示的是三个journalnode守护进程，三个zookeeper守护进程。

19、 在app-11namenode上各式化，命令：hdfs namenode -format 注：会打印很多classpath

20、 因为是两台namenode做ha，需要在另一台机器上拷贝格式化信息，先关闭所有守护进程，命令：for name in app-11 app-12 app-13; do ssh hadoop@$name "hdfs --daemon stop journalnode"; done

## 2.3创建ha节点

21、 创建ha主从应用里zookeeper目录树的目录，先登录到客户端查看ha存不存在，命令：zkCli.sh

22、 查看客户端下有什么，命令：ls /

23、 只有一个zookeeper节点，ha节点并不存在，退出，命令：quit

24、 zookeeper初始化工作，命令：hdfs zkfc -formatZK -force注：打印很多的classpath，force强势初始化。

25、 再次登录到客户端，查看ha节点存不存在。命令：zkCli.sh

26、 查看根目录下有什么，命令：ls /注：这时候有一个hadoop-ha节点

27、 查看hadoop-ha节点下，命令：ls /hadoop-ha

28、 查看dmcluster节点下，dmcluster节点下是空的，dmcluster是ha下的集群，命令：ls /hadoop-ha/dmcluster

29、 证明ha节点存在之后，退出，命令：quit

## 2.4将app-11namenode的初始化信息同步到app-12上

30、 先启动dfs整个的系统，命令：start-dfs.sh

31、 免密登录到app-12上执行，命令：ssh hadoop@app-12 "hdfs namenode -bootstrapStandby"注：打印很多classpath

## 2.5启动整个集群检查hdfs和yarn是否成功

32、 关闭整个dfs系统，因为后续需要通过其他途径启动整个集群。命令：stop-dfs.sh

33、启动整个集群，时间会比较久。命令：start-all.sh

34、检查hdfs和yarn到底是否成功，现将命令打出来。命令：hdfs haadmin

35、使用-getAllServiceState这个命令。命令：hdfs haadmin -getAllServiceState

36、打印yarn的命令行。命令：yarn rmadmin

37、使用-getAllServiceState这个命令。命令：yarn rmadmin -getAllServiceState

38、验证整个集群，先看一下hdfs上有什么。命令：hdfs dfs -ls /注：hdfs为空

# 3.测试hadoop集群启动是否正常

39、为测试方便，创建一个目录。命令：hdfs dfs -mkdir -p /test/data

40、将所有需要mapreduce数据放在data目录下，将配置文件以xml结尾的上传到data目录里。命令：hdfs dfs -put /hadoop/Hadoop/hadoop-3.1.2/etc/hadoop/*.xml /test/data

41、查看是否上传成功。命令：hdfs dfs -ls /test/data

42、提交一个mapreduce任务，用hadoop自带的例子。

命令：hadoop jar /hadoop/Hadoop/hadoop-3.1.2/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.1.2.jar grep /test/data /test/output "dfs[a-z.]+"

注：使用的是grep参数，输入是test/data输出是test/output，这里的output一定不能存在，存在的话用rm命令删除掉。查询以dfs开头的，以小写字母以及.紧接着相邻的很多+字符的字符串。这个命令执行的时候分两步，第一步是查询，查到做正则表达的那一行，然后做排序。

43、整个mapreduce案例已经执行完了，看结果输出什么东西。命令：hdfs dfs -ls /test/output注：输出了两个文件，结果在part-r-00000中

44、查看part-r-00000文件中的内容。命令：hdfs dfs -cat /test/output/part-r-00000

注：数字代表出现的次数，字符串是匹配的字符串。+为贪婪匹配。

45、登录管理界面，查看已经执行完的任务。首先点击右侧chrome-browser。

输入网址，app-11:8088，会自动跳转到app-12:8088。

# 4.常见问题

问题：app-12和app-13的环境变量有问题。

解决办法：检查环境变量，实在不行从头做。