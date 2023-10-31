#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
int main(int argc, char *argv[])
{
    // 执行子命令
    // 打印 argv
    for (int i = 0; i < argc; i++)
    {
        printf("argv[%d] = %s\n", i, argv[i]);
    }
    
    printf("hello\n");
    pid_t pid = fork();

    if (pid < 0)
    {
        return -1;
    }

    if (pid == 0)
    {    // 将子进程的标准输出和标准错误输出重定向到父进程的标准输出和标准错误输出
        // dup2(STDOUT_FILENO, STDERR_FILENO);
        sleep(2);
        printf("子进程\n");
        system(argv[1]);
    }
    else
    {
        printf("父进程\n");
    }
}
