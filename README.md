# XFlow

## Setting up Dependencies

```groovy
implementation 'com.github.7hens.xflow:java-flow:-SNAPSHOT'
implementation 'com.github.7hens.xflow:android-flow:-SNAPSHOT'
```

## Simple Usages

```java
Flow.just(1, 2, 3, 4, 5)
        .take(3)
        .map(it -> Flow.just(it + 10, it + 20))
        .transform(FlowX.delayErrors())
        .transform(FlowX.flatMerge())
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
        .collect();
```

