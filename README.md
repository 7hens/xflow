# XFlow

[![jitpack](https://jitpack.io/v/7hens/xflow.svg)](https://jitpack.io/#7hens/xflow)
[![license](https://img.shields.io/github/license/7hens/xflow.svg)](https://github.com/7hens/xflow/blob/master/LICENSE)

Reactive Flows for JVM and Android

## Setting up Dependencies

```groovy
implementation 'com.github.7hens.xflow:java-flow:0.3'
implementation 'com.github.7hens.xflow:android-flow:0.3'
```

## Sample Usage

```java
Flow.just(1, 2, 3, 4, 5)
        .take(3)
        .mapToFlow(it -> Flow.just(it + 10, it + 20))
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

## Supported Operators

| Category     | Operators                                                                       |
| ------------ | ------------------------------------------------------------------------------- |
| Create       | create / just / defer / empty / never / error / from / range / timer / interval |
| Convert      | transform / to / polyTo                                                         |
| Scheduler    | flowOn                                                                          |
| Collect      | onCollect / toCollection / toList                                               |
| Map          | map / mapToFlow / flatMap                                                       |
| Throttle     | throttleFirst / throttleLast                                                    |
| Filter       | filter / distinct / distinctUntilChanged / ignoreElements / skip                |
| Take         | take / takeLast / takeWhile / takeUntil                                         |
| Element      | first / elementAt / last                                                        |
| Repeat       | repeat                                                                          |
| Fold         | reduce                                                                          |
| Timeout      | timeout / autoCancel                                                            |
| Delay        | delay / delayStart                                                              |
| Catch        | catchError / retry                                                              |
| Window       | window / buffer                                                                 |
| Backpressure | onBackpressure                                                                  |
| Poly         | polyWith / delayErrors / flatConcat / flatMerge / flatSwitch / flatZip          |

