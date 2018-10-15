# Application后处理器（AndroidPostProcessing）： 通过注解配置初始化各模块及应用所需 sdk，按优先级/延时时间/是否只在Debug下有效/执行线程  等条件初始化 sdk

通常，我们要在 Application 中处理一堆的三方 SDK 和自定义框架的初始化，下面的处理方式会带来一些问题：
维护成本，应用启动慢、卡顿，实现方式 low 。
```
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mHandler = new Handler();
        // screen info
        registerScreenActionReceiver();
        // UserCenterManager
        AccountManager.init();
        // 初始化配置
        ConfigManager.init(this);
        // token
        initToken();
        // 友盟
        MobclickAgent.init();
        // Log
        LogUtils.init(BuildConfig.DEBUG);
        // ShareSdk
        ShareSDK.initSDK(mContext);
        // 信鸽推送
        XGPushConfig.init(this);
        // Bugly
        if(!BuildConfig.DEBUG){
            initBugly();
        }
        // 判断程序是否在前台
        registerActivityLifecycleCallbacks(this);

    }
```
### AndroidPostProcessing
Application 的后处理器，利用编译期注解方式，指定线程和任务延时策略处理初始化的问题。
[项目地址](https://github.com/woaigmz/AndroidPostProcessing) 和 demo
![postprocessing.gif](https://upload-images.jianshu.io/upload_images/8886407-d1cfae4b1bc48b39.gif?imageMogr2/auto-orient/strip)

### 使用方式：
引入AndroidPostProcessing和注解处理器,已经上传maven ：）
project/build.gradle
```
allprojects {
    repositories {
        ...
        maven {
            url  "https://dl.bintray.com/woaigmz/AndroidPostProcessing"
        }
    }
}
```
common-lib 模块：
```
    api 'com.woaigmz.app:postprocessing:0.0.1'
    api 'com.woaigmz.app:postprocessing-annotation:0.0.1'
    //如果 common 模块需要用 @App 注解
    annotationProcessor 'com.woaigmz.app:postprocessing-compiler:0.0.1'
```
其他子模块：
```
    implementation project(':common')
    annotationProcessor 'com.woaigmz.app:postprocessing-compiler:0.0.1'
```

1：Application:
```
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidPostProcessing.initialization(this).dispatcher();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AndroidPostProcessing.release();
    }
}
```
2：各处理模块:
① 代理类实现 IApp 接口，类名随意；
② 类头部加 @App() 注解
```
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface App {
    boolean RELEASE = false;
    boolean DEBUG = true;
    String name() default "Main";      //名称
    boolean type() default RELEASE;    //release起作用还是debug时起作用
    int priority() default 0;   //优先级 - 执行顺序
    boolean async() default false;    //是否异步，默认同步，在主线程执行
    long delay() default 0;    //延时时间，默认为0，不延时执行
}
```
###### 注意:
① 关于多进程：每个进程都会 onCreate()  onTerminate() ，初始化时的任务表，所以互不影响，资源释放也不受影响。该库默认所有进程都存在，如果要有主进程库，可以 onCreate 添加判断条件
② 关于调试： ctrl + shif t + F ，全局搜索 @App ，每个 IApp 接口对应的对象可以单独 [hugo](https://github.com/JakeWharton/hugo) 出执行时间
③ 关于 async ，默认主线程，如果为true则运行在子线程，线程优先级为 background
###### eg:
hotfix:
```
@App(name = "Hotfix", priority = 3)
public class HotfixProxy implements IApp {

    @Override
    public void dispatcher(@NonNull Application application) {

        Toast.makeText(application, "Hotfix", Toast.LENGTH_SHORT).show();

    }
}
```
cache:
```
@App(name = "Cache", priority = 2, async = true, delay = 2000)
public class CacheProxy implements IApp {

    @Override
    public void dispatcher(@NonNull Application application) {

        Looper.prepare();
        Toast.makeText(application, "cache", Toast.LENGTH_SHORT).show();
        Looper.loop();

    }
}
```
leakcanary:
```
@App(name = "LeakCanary", type = App.DEBUG, priority = 1, delay = 5000)
public class LeakCanaryProxy implements IApp {

    @Override
    public void dispatcher(@NonNull Application application) {

        Toast.makeText(application, "LeakCanary", Toast.LENGTH_SHORT).show();
    }
}
```

### 实现思路：
① 注解部分：编译生成的中间代理类，都在 com.woaiqw.generate 包下
```
package com.woaiqw.generate;

/**
* Generated code from AndroidPostProcessing . Do not modify!
 */

public final class LeakCanary$$Proxy{

    public static final String path = "com.woaiqw.common.LeakCanaryProxy";
    public static final String name = "LeakCanary";
    public static final boolean type = true;
    public static final int priority = 1;
    public static final boolean async = false;
    public static final long delay = 5000;

}
```
② 注解处理器AbstractProcessor:
[AppProcessor](https://github.com/woaigmz/AndroidPostProcessing/blob/master/postprocessing-compiler/src/main/java/com/woaiqw/appcompiler/AppProcessor.java)

③ [AndroidPostProcessing](https://github.com/woaigmz/AndroidPostProcessing/blob/master/postprocessing/src/main/java/com/woaiqw/postprocessing/AndroidPostProcessing.java) 的api
  初始化注解生成的代理类，按 priority 生成代理列表List<AppDelegate>
  dispatcher 任务，WeakHandler + ScheduledThreadPool
  SharePreference 缓存 优化性能
  资源释放

感谢：）

