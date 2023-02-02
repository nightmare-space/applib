#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
int main()
{
    printf("hello\n");
    pid_t pid = fork();

    if (pid < 0)
    {
        return -1;
    }

    if (pid == 0)
    {
        sleep(2);
        printf("子进程\n");
        system("app_process -Djava.class.path=/sdcard/app_server /system/bin --nice-name=com.nightmare.dex com.nightmare.applib.AppServer open");
    }
    else
    {
        printf("父进程\n");
    }
}
