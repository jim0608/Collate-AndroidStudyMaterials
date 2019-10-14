# 大医Lib

## 引入方法

1. 下载相应的Lib项目
2. 需要引入的项目中 File->New->Import Module..
    - 选择需要lib模块
3. 赋值Lib项目中的 config.gradle 文件到相应位置
4. 项目层级的 build.gradle 首部增加 `apply from: "config.gradle"`
5. 添加项目加载地址
    ```
    allprojects{
         repositories{ 
            ...
            maven { url "https://jitpack.io" }
         }
     }
    ```

6. 在主app模块下添加
    ```
    android{
        ...
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }
    }
    ```
7. 在主app模块下引入项目.   
    `implementation project(':dyzhlib')`
