# XFlow

[![jitpack](https://jitpack.io/v/7hens/xflow.svg)](https://jitpack.io/#7hens/xflow)
[![license](https://img.shields.io/github/license/7hens/xflow.svg)](https://github.com/7hens/xflow/blob/master/LICENSE)

## Setting up Dependencies

```groovy
implementation 'com.github.7hens.xflow:java-flow:0.1'
implementation 'com.github.7hens.xflow:android-flow:0.1'
```

## Simple Usages

```java
Flow.just(1, 2, 3, 4, 5)
        .take(3)
        .polyMap(it -> Flow.just(it + 10, it + 20))
        .delayErrors()
        .flatMerge()
        .onCollect(new CollectorHelper<Integer>() {
            @Override
            protected void onEach(Integer s) {
                System.out.println(s);
            }

            @Override
            protected void onComplete() {
            }
        })
        .flowOn(Schedulers.io())
        .autoCancel(AndroidFlow.lifecycle(activity))
        .collect();
```

