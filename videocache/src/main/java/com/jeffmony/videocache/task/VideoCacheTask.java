package com.jeffmony.videocache.task;

import androidx.annotation.NonNull;

import com.jeffmony.videocache.listener.IVideoCacheTaskListener;
import com.jeffmony.videocache.model.VideoCacheInfo;
import com.jeffmony.videocache.utils.StorageUtils;
import com.jeffmony.videocache.utils.VideoProxyThreadUtils;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class VideoCacheTask {

    protected VideoCacheInfo mCacheInfo;
    protected Map<String, String> mHeaders;
    protected IVideoCacheTaskListener mListener;
    protected ThreadPoolExecutor mTaskExecutor;

    protected long mCachedSize;      //当前缓存大小
    protected long mLastCachedSize;  //上一次缓存大小
    protected long mTotalSize;
    protected boolean mIsCompleted;
    protected File mSaveDir;

    public VideoCacheTask(VideoCacheInfo cacheInfo, Map<String, String> headers) {
        mCacheInfo = cacheInfo;
        mHeaders = headers;
        mCachedSize = cacheInfo.getCachedSize();
        mTotalSize = cacheInfo.getTotalSize();
        mIsCompleted = cacheInfo.isCompleted();
        mSaveDir = new File(cacheInfo.getSavePath(), cacheInfo.getMd5());
        if (!mSaveDir.exists()) {
            mSaveDir.mkdir();
        }
    }

    public void setTaskListener(@NonNull IVideoCacheTaskListener listener) {
        mListener = listener;
    }

    public abstract void startCacheTask();

    public abstract void pauseCacheTask();

    public abstract void seekToCacheTask(int curTs);

    public abstract void seekToCacheTask(long curLength);

    public abstract void resumeCacheTask();


    protected void notifyOnTaskStart() {
        mListener.onTaskStart();
    }

    protected void notifyOnTaskFailed(Exception e) {
        mListener.onTaskFailed(e);
    }

    protected void notifyOnTaskCompleted() {
        mListener.onTaskCompleted();
    }

    protected boolean isTaskRunning() {
        return mTaskExecutor != null && !mTaskExecutor.isShutdown();
    }

    protected void saveVideoInfo() {
        VideoProxyThreadUtils.submitRunnableTask(new Runnable() {
            @Override
            public void run() {
                StorageUtils.saveVideoCacheInfo(mCacheInfo, mSaveDir);
            }
        });
    }
}