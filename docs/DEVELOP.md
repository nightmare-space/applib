
### 备份
```sh
tar -zcvf $gz $dataDir
```

### 恢复
```sh
tar -xvf     sysconfig.tar -C /
chown -R  10590:10590 /data/data/com.nightmare.adbtools/
```