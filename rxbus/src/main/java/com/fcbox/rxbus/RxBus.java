package com.fcbox.rxbus;

import android.os.Looper;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import rx.subscriptions.CompositeSubscription;

/**
 * RxBus处理类 .
 */

public class RxBus {

    private static volatile RxBus defaultInstance;
    private final SubscriberMethodFinder mSubscriberMethodFinder;
    /**
     * 非粘性订阅 .
     */
    private final Subject mBusSubject;

    /**
     * 保存一个注册对象 订阅成功的所有Subscription对象
     */
    private final Map<Object, CompositeSubscription> mObjectSubscriptionMap;

    /**
     * 粘性订阅map .
     */
    private final Map<Class<?>, Object> mStickyEvents;

    private RxBus() {
        mBusSubject = new SerializedSubject<>(PublishSubject.create());
        mSubscriberMethodFinder = new SubscriberMethodFinder();
        mStickyEvents = new ConcurrentHashMap<>();
        mObjectSubscriptionMap = new HashMap<>();
    }

    /**
     * 获取一个单例
     *
     * @return RxBus实例
     */
    public static RxBus getDefault() {
        if (defaultInstance == null) {
            synchronized (RxBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new RxBus();
                }
            }
        }
        return defaultInstance;
    }

    /**
     * 注册一个subscriber类型事件 .
     */
    public void register(Object subscriber) {
        //通过找出subscriber这个注册对象下所有处理事件的方法 .
        if (isRegistered(subscriber)) {
            BusLog.e("repeate register subscriber " + subscriber + " And return .");
            return;
        }
        Class<?> subscriberClass = subscriber.getClass();
        List<SubscriberMethod> subscriberMethods = mSubscriberMethodFinder.findSubscriberMethods(subscriberClass);
        synchronized (this) {
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
                //订阅
                subscribe(subscriber, subscriberMethod);
            }
        }
    }

    /**
     * 订阅事件并绑定事件方法.
     */
    @SuppressWarnings("unchecked")
    private void subscribe(final Object subscriber, final SubscriberMethod subscriberMethod) {
        if (!mObjectSubscriptionMap.containsKey(subscriber)) {
            CompositeSubscription compositeSubscription = new CompositeSubscription();
            mObjectSubscriptionMap.put(subscriber, compositeSubscription);
        }
        CompositeSubscription compositeSubscription = mObjectSubscriptionMap.get(subscriber);

        Subscription subscription = obtainObservable(subscriberMethod.sticky, subscriberMethod.eventType)
                .compose(this.applyThread(subscriberMethod.threadMode))
                .onErrorResumeNext(throwable -> {
                    BusLog.e("事件出错：", throwable);
                    return Observable.just(null);
                })
                .filter(new Func1() {
                    @Override
                    public Object call(Object object) {
                        return object != null;
                    }
                })
                .subscribe(new Action1() {
                    @Override
                    public void call(Object object) {
                        try {
                            subscriberMethod.method.invoke(subscriber, object);
                        } catch (InvocationTargetException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                });
        compositeSubscription.add(subscription);

    }

    @SuppressWarnings("unchecked")
    private <T> Observable<T> obtainObservable(boolean sticky, Class<T> eventType) {

        Object event = mStickyEvents.get(eventType);

        if (sticky && event != null) {
            return mBusSubject.ofType(eventType).mergeWith(Observable.just(eventType.cast(event)));
        } else {
            return mBusSubject.ofType(eventType);
        }

    }

    private <T> Observable.Transformer<T, T> applyThread(ThreadMode threadMode) {

        switch (threadMode) {
            case POSTING:
                return new Observable.Transformer<T, T>() {
                    @Override
                    public Observable<T> call(Observable<T> tObservable) {
                        return tObservable;
                    }
                };

            case ASYNC:
                return new Observable.Transformer<T, T>() {
                    @Override
                    public Observable<T> call(Observable<T> tObservable) {
                        return tObservable.observeOn(Schedulers.newThread());
                    }
                };

            case BACKGROUND:
                return new Observable.Transformer<T, T>() {
                    @Override
                    public Observable<T> call(Observable<T> tObservable) {
                        if (Looper.myLooper() != Looper.getMainLooper()) {
                            return tObservable;
                        } else {
                            return tObservable.observeOn(Schedulers.io());
                        }
                    }
                };

            case MAIN:
                return new Observable.Transformer<T, T>() {
                    @Override
                    public Observable<T> call(Observable<T> tObservable) {
                        return tObservable.observeOn(AndroidSchedulers.mainThread());
                    }
                };
        }

        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return tObservable;
            }
        };
    }

    /**
     * 解注册subscriber类型对象 .
     */
    public synchronized void unregister(Object subscriber) {
        CompositeSubscription compositeSubscription = mObjectSubscriptionMap.get(subscriber);
        if (compositeSubscription == null) {
            BusLog.e("不存在subscriber的订阅事件!");
            return;
        }
        compositeSubscription.clear();
        mObjectSubscriptionMap.remove(subscriber);
    }

    /**
     * 发送event对象类型事件.
     */
    @SuppressWarnings("unchecked")
    public void post(Object event) {
        mBusSubject.onNext(event);
    }

    /**
     * 发送event粘性对象类型事件.
     */
    public void postSticky(Object event) {
        synchronized (mStickyEvents) {
            mStickyEvents.put(event.getClass(), event);
        }
        post(event);
    }

    /**
     * 根据事件类型获取最后一个发送的对象.
     */
    public <T> T getStickyEvent(Class<T> eventType) {
        synchronized (mStickyEvents) {
            return eventType.cast(mStickyEvents.get(eventType));
        }
    }

    /**
     * 移除eventType对应的粘性事件.
     */
    public <T> T removeStickyEvent(Class<T> eventType) {
        synchronized (mStickyEvents) {
            return eventType.cast(mStickyEvents.remove(eventType));
        }
    }

    /**
     * 移除event对应的粘性事件.
     */
    public boolean removeStickyEvent(Object event) {
        synchronized (mStickyEvents) {
            Class<?> eventType = event.getClass();
            Object existingEvent = mStickyEvents.get(eventType);
            if (event.equals(existingEvent)) {
                mStickyEvents.remove(eventType);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 移除所有的粘性 .
     */
    public void removeAllStickyEvents() {
        synchronized (mStickyEvents) {
            mStickyEvents.clear();
        }
    }

    /**
     * 是否注册了subscriber对象类型事件 .
     *
     * @return 是否已注册
     */
    public synchronized boolean isRegistered(Object subscriber) {
        return mObjectSubscriptionMap.containsKey(subscriber);
    }

}
