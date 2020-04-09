### IO模型
##### IO阻塞模型
```
try {
    ServerSocket serverSocket = new ServerSocket(5555);
    System.out.println("socket bind on port: 5555");

    while (true) {
        Socket socket = serverSocket.accept();
        InputStream inputStream = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        while (true) {
            System.out.println("accept client: " + socket.toString() + " message: " + bufferedReader.readLine());
        }
    }
} catch (IOException e) {
    e.printStackTrace();
}
```
> 执行： __strace -ff -o ./io java -jar xxx.jar__
```
安装：yum install strace
strace命令用于跟踪进程执行时的系统调用和所接受的信号
-f 跟踪由fork调用所产生的子进程
-ff -o 将所有进程的跟踪结果输出到相应的文件.pid中，pid为各进程的进程号
``` 

![image](https://github.com/cnnc/file-server/blob/master/io/starce_1.png)

从图中可以看出主进程是23350，对应io.23350文件，查看io.23350文件，在最后出现一行：

![iamge](https://github.com/cnnc/file-server/blob/master/io/starce_2.png)

调用了 __clone__ 方法，clone出了23351线程，"clone"为系统调用，查看io.23351文件

> socket(AF_INET6, SOCK_STREAM, IPPROTO_IP) = 6

![image](https://github.com/cnnc/file-server/blob/master/io/starce_3.png)

>1. 通过socket创建了文件描述符6: socket() = 6
>2. 绑定5555到文件描述符6上: bind(6, 5555)
>3. 监听文件描述符6: listen(6)
>4. poll(fd 6) 阻塞住线程

查看linux中 __/proc/23350/fd__ 目录

![image](https://github.com/cnnc/file-server/blob/master/io/starce_4.png)

```
0   标准输入流
1   标准输出流
2   标准错误输出流
3、4 java程序打开的文件
5、6 socket绑定的文件，ipv4、ipv6
```
查看linux中 __/proc/23350/task__ 目录：为该jvm进程产生的所有线程

通过 __ulimit -a__ 命令查看

![image](https://github.com/cnnc/file-server/blob/master/io/starce_5.png)
```
open files  为os中每个进程打开的最大文件个数
max user processes 为os中用户可以创建最大的进程/线程个数
```

通过命令： __man ***__  查看linux中systemcall函数

| 代号        | 代表内容    |
| --------   | :-----   |
| 1        | User commands that may be started by everyone.      |
| 2        | System calls, that is, functions provided by the kernel.      |
| 3        | Subroutines, that is, library functions.      |
| 4        | Devices, that is, special files in the /dev directory.      |
| 5        | File format descriptions, e.g. /etc/passwd.      |
| 6        | Games, self-explanatory.      |
| 7        | Miscellaneous, e.g. macro packages, conventions.      |
| 8        | System administration tools that only root can execute.      |
| 9        | Another (Linux specific) place for kernel routine documentation.      |

通过nc工具连接socket
```
安装： yum install nc
使用： nc localhost 5555
```
查看 __/proc/23350/fd__ 目录，增加了一个7文件描述符

通过命令 __netstat -natp__ 查看网络连接
> tcp6       0      0 ::1:5555                ::1:46862               ESTABLISHED 23350/java
客户端client生成一个随机46862与服务端5555连接

| 状态    | 含义    |
| ----- | ----- |
| LISTENING | 侦听状态  |
| ESTABLISHED   | 建立连接，两台机器正在通信 |
| CLOSE_WAIT    | 对方主动关闭连接或者网络异常导致连接中断，这时我方的状态会变成CLOSE_WAIT 此时我方要调用close()来使得连接正确关闭 |
| TIME_WAIT     | 我方主动调用close()断开连接，收到对方确认后状态变为TIME_WAIT |
| SYN_SENT      | 请求连接 |

##### 问题分析

> 此模型为阻塞模型，只能有一个socket连入，会阻塞在接收客户端发送消息中，导致其他客户端无法再连入进来


--------------------------------------------
解决上面的问题

看代码中的NonBIODemo类，将读取数据的内容抛出一个子线程里去执行
```
try {
    ServerSocket serverSocket = new ServerSocket(6666);
    System.out.println("socket bind on port: 6666");

    while (true) {
        final Socket socket = serverSocket.accept();
        System.out.println("accept socket: " + socket.toString());
        new Thread() {
            @Override
            public void run() {
                InputStream inputStream = null;
                try {
                    inputStream = socket.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    System.out.println("accept message from socket: " + socket.toString() + " " + bufferedReader.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
} catch (IOException e) {
    e.printStackTrace();
}
```
这样解决了由于需要等待接收已连入的client发送消息而阻塞住主线程，其他client无法连入的问题。

##### 问题分析

>server端每次为了读取client端的消息需要进行线程的切换，线程切换需要保留当前线程的运行状态，
频繁切换占用CPU资源，有可能只有最后一个client发送了消息，这样对server端的性能损耗很大

--------------------------------------------

解决上面的问题

>引入 __NIO__ 非阻塞IO, NIO有两个含义，一个是java中表示NewIO，另一个是OS中的非阻塞IO，
>OS提供了三个系统调用： __select__、__poll__、__epoll__

# TODO






















































































