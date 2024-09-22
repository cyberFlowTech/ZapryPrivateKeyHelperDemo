## Build

### 方式1

build工程会自动生成aar

### 方式2

1.setup gradle

2.项目根目录执行指令

```
./gradlew assemble
```

### 生成的aar路径

`./lib_private_key/build/outputs/aar/lib_private_key-debug.aar`

使用debug的aar即可，在Zapry工程会混淆代码