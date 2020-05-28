# 介绍
dubbo-reactor 为dubbo调用扩展了reactor模式的支持，可以返回Mono和Flux类型

这个扩展支持reactor模式的目的，是为了让dubbo的异步调用编写更简单，多个异步调用之间组合更容易，并且很多应用之前多个dubbo调用，用的是线程池方式，这种方式会浪费线程资源，
而dubbo是可以实现真正的异步而dubbo是可以实现真正的异步调用，不需要开额外的线程池。

首先我们看下不用dubbo-reactor的情况下，实现并发调用dubbo服务的几个方式

方式一：指定线程池
```java
    
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Test
    public void test1() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Future<Object> future1 = executorService.submit(() -> secretService.process("", ""));
        Future<Object> future2 = executorService.submit(() -> secretService.process("", ""));
        Future<Object> future3 = executorService.submit(() -> secretService.process("", ""));
        try {
            future1.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            future2.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            future3.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long elapsed = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        System.out.println(elapsed);
    }
```
以上代码表示了3个dubbo服务的并发调用，不仅啰嗦，还浪费了额外的线程资源
我们再来看方式二：走dubbo原生的异步调用
```java
    @Test
    public void test1() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Future<Object> future1 = RpcContext.getContext().asyncCall(() -> secretService.process("", ""));
        Future<Object> future2 = RpcContext.getContext().asyncCall(() -> secretService.process("", ""));
        Future<Object> future3 = RpcContext.getContext().asyncCall(() -> secretService.process("", ""));
        try {
            future1.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            future2.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            future3.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long elapsed = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        System.out.println(elapsed);
    }
```
以上代码虽然没有了线程池的多余使用，但是代码还是显得很啰嗦

最后我们看看使用dubbo-reactor的方式:

首先添加依赖
```xml
        <dependency>
            <groupId>com.bobo</groupId>
            <artifactId>dubbo-reactor</artifactId>
            <version>0.1</version>
        </dependency>
```
然后编写接口
```java
@DubboReactor(service = SecretService.class)
public interface SecretReactorService {

    Mono<Object> process(String el, String password);
}
```
就是简单写个接口，加下注解，注解里指定原生的dubbo接口，然后方法签名完全拷贝，只是把返回类型套一个Mono,然后需要点击下idea的编译功能触发注解处理器的执行

编译完后，我们应该会看到这样1个实现
```java
@Service
public class SecretReactorServiceImpl implements SecretReactorService {
  @Autowired
  private SecretService secretService;

  @Override
  public Mono<Object> process(String el, String passWord) {
    return cast(Mono.fromFuture(convert(getContext().asyncCall(() -> secretService.process(el,passWord)))));
  }
}
```
可以看到，编译以后就自动生成了上面的接口实现类代码，并且加上了Service注解

所以最后我们就可以直接注入这个接口，并进行reactor的组合调用
```java
    @Test
    public void test2() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Mono<Object> mono1 = secretReactorService.process("", "");
        Mono<Object> mono2 = secretReactorService.process("", "");
        Mono<Object> mono3 = secretReactorService.process("", "");
        Tuple3<Object, Object, Object> tuple3 = Mono.zip(mono1, mono2, mono3).block();
        long elapsed = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        System.out.println(elapsed);
    }
```
最后大家看下reactor的组合调用写法，在跟前面2种写法做对比，就会发现reactor在做异步组合调用的好处了

