/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: F:\\NiceMusic\\musiclibrary\\src\\main\\aidl\\com\\lzx\\musiclibrary\\aidl\\listener\\IPlayControl.aidl
 */
package com.lzx.musiclibrary.aidl.source;

public interface IPlayControl extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements IPlayControl {
        private static final java.lang.String DESCRIPTOR = "IPlayControl";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an IPlayControl interface,
         * generating a proxy if needed.
         */
        public static IPlayControl asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof IPlayControl))) {
                return ((IPlayControl) iin);
            }
            return new IPlayControl.Stub.Proxy(obj);
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_playMusic: {
                    data.enforceInterface(DESCRIPTOR);
                    java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> _arg0;
                    _arg0 = data.createTypedArrayList(com.lzx.musiclibrary.aidl.model.SongInfo.CREATOR);
                    int _arg1;
                    _arg1 = data.readInt();
                    boolean _arg2;
                    _arg2 = (0 != data.readInt());
                    this.playMusic(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_playMusicByInfo: {
                    data.enforceInterface(DESCRIPTOR);
                    com.lzx.musiclibrary.aidl.model.SongInfo _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = com.lzx.musiclibrary.aidl.model.SongInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    boolean _arg1;
                    _arg1 = (0 != data.readInt());
                    this.playMusicByInfo(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_playMusicByIndex: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    boolean _arg1;
                    _arg1 = (0 != data.readInt());
                    this.playMusicByIndex(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_pausePlayInMillis: {
                    data.enforceInterface(DESCRIPTOR);
                    long _arg0;
                    _arg0 = data.readLong();
                    this.pausePlayInMillis(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_getCurrPlayingIndex: {
                    data.enforceInterface(DESCRIPTOR);
                    int _result = this.getCurrPlayingIndex();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_pauseMusic: {
                    data.enforceInterface(DESCRIPTOR);
                    this.pauseMusic();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_resumeMusic: {
                    data.enforceInterface(DESCRIPTOR);
                    this.resumeMusic();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_stopMusic: {
                    data.enforceInterface(DESCRIPTOR);
                    this.stopMusic();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_setPlayList: {
                    data.enforceInterface(DESCRIPTOR);
                    java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> _arg0;
                    _arg0 = data.createTypedArrayList(com.lzx.musiclibrary.aidl.model.SongInfo.CREATOR);
                    this.setPlayList(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_setPlayListWithIndex: {
                    data.enforceInterface(DESCRIPTOR);
                    java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> _arg0;
                    _arg0 = data.createTypedArrayList(com.lzx.musiclibrary.aidl.model.SongInfo.CREATOR);
                    int _arg1;
                    _arg1 = data.readInt();
                    this.setPlayListWithIndex(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_getPlayList: {
                    data.enforceInterface(DESCRIPTOR);
                    java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> _result = this.getPlayList();
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                }
                case TRANSACTION_deleteSongInfoOnPlayList: {
                    data.enforceInterface(DESCRIPTOR);
                    com.lzx.musiclibrary.aidl.model.SongInfo _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = com.lzx.musiclibrary.aidl.model.SongInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    boolean _arg1;
                    _arg1 = (0 != data.readInt());
                    this.deleteSongInfoOnPlayList(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_getStatus: {
                    data.enforceInterface(DESCRIPTOR);
                    int _result = this.getStatus();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_playNext: {
                    data.enforceInterface(DESCRIPTOR);
                    this.playNext();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_playPre: {
                    data.enforceInterface(DESCRIPTOR);
                    this.playPre();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_hasPre: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = this.hasPre();
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_hasNext: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = this.hasNext();
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_getPreMusic: {
                    data.enforceInterface(DESCRIPTOR);
                    com.lzx.musiclibrary.aidl.model.SongInfo _result = this.getPreMusic();
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_getNextMusic: {
                    data.enforceInterface(DESCRIPTOR);
                    com.lzx.musiclibrary.aidl.model.SongInfo _result = this.getNextMusic();
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_getCurrPlayingMusic: {
                    data.enforceInterface(DESCRIPTOR);
                    com.lzx.musiclibrary.aidl.model.SongInfo _result = this.getCurrPlayingMusic();
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_setCurrMusic: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    this.setCurrMusic(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_setPlayMode: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    boolean _arg1;
                    _arg1 = (0 != data.readInt());
                    this.setPlayMode(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_getPlayMode: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg0;
                    _arg0 = (0 != data.readInt());
                    int _result = this.getPlayMode(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_getProgress: {
                    data.enforceInterface(DESCRIPTOR);
                    long _result = this.getProgress();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                }
                case TRANSACTION_seekTo: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    this.seekTo(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_reset: {
                    data.enforceInterface(DESCRIPTOR);
                    this.reset();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_updateNotificationCreater: {
                    data.enforceInterface(DESCRIPTOR);
                    com.lzx.musiclibrary.notification.NotificationCreater _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = com.lzx.musiclibrary.notification.NotificationCreater.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    this.updateNotificationCreater(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_updateNotificationFavorite: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg0;
                    _arg0 = (0 != data.readInt());
                    this.updateNotificationFavorite(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_updateNotificationLyrics: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg0;
                    _arg0 = (0 != data.readInt());
                    this.updateNotificationLyrics(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_updateNotificationContentIntent: {
                    data.enforceInterface(DESCRIPTOR);
                    android.os.Bundle _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = android.os.Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    java.lang.String _arg1;
                    _arg1 = data.readString();
                    this.updateNotificationContentIntent(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_registerPlayerEventListener: {
                    data.enforceInterface(DESCRIPTOR);
                    IOnPlayerEventListener _arg0;
                    _arg0 = IOnPlayerEventListener.Stub.asInterface(data.readStrongBinder());
                    this.registerPlayerEventListener(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_unregisterPlayerEventListener: {
                    data.enforceInterface(DESCRIPTOR);
                    IOnPlayerEventListener _arg0;
                    _arg0 = IOnPlayerEventListener.Stub.asInterface(data.readStrongBinder());
                    this.unregisterPlayerEventListener(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_registerTimerTaskListener: {
                    data.enforceInterface(DESCRIPTOR);
                    IOnTimerTaskListener _arg0;
                    _arg0 = IOnTimerTaskListener.Stub.asInterface(data.readStrongBinder());
                    this.registerTimerTaskListener(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_unregisterTimerTaskListener: {
                    data.enforceInterface(DESCRIPTOR);
                    IOnTimerTaskListener _arg0;
                    _arg0 = IOnTimerTaskListener.Stub.asInterface(data.readStrongBinder());
                    this.unregisterTimerTaskListener(_arg0);
                    reply.writeNoException();
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IPlayControl {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            public java.lang.String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }
//播放，并设置播放列表

            @Override
            public void playMusic(java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> list, int index, boolean isJustPlay) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeTypedList(list);
                    _data.writeInt(index);
                    _data.writeInt(((isJustPlay) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_playMusic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//根据音乐信息播放

            @Override
            public void playMusicByInfo(com.lzx.musiclibrary.aidl.model.SongInfo info, boolean isJustPlay) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((info != null)) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(((isJustPlay) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_playMusicByInfo, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//根据索引播放

            @Override
            public void playMusicByIndex(int index, boolean isJustPlay) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(index);
                    _data.writeInt(((isJustPlay) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_playMusicByIndex, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//设置定时时间

            @Override
            public void pausePlayInMillis(long time) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeLong(time);
                    mRemote.transact(Stub.TRANSACTION_pausePlayInMillis, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public int getCurrPlayingIndex() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getCurrPlayingIndex, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
//暂停

            @Override
            public void pauseMusic() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_pauseMusic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//继续

            @Override
            public void resumeMusic() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_resumeMusic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//停止音乐

            @Override
            public void stopMusic() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_stopMusic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//设置播放列表

            @Override
            public void setPlayList(java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> list) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeTypedList(list);
                    mRemote.transact(Stub.TRANSACTION_setPlayList, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//设置播放列表

            @Override
            public void setPlayListWithIndex(java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> list, int index) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeTypedList(list);
                    _data.writeInt(index);
                    mRemote.transact(Stub.TRANSACTION_setPlayListWithIndex, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//得到播放列表

            @Override
            public java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> getPlayList() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getPlayList, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createTypedArrayList(com.lzx.musiclibrary.aidl.model.SongInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
//从播放列表中删除一条信息

            @Override
            public void deleteSongInfoOnPlayList(com.lzx.musiclibrary.aidl.model.SongInfo info, boolean isNeedToPlayNext) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((info != null)) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(((isNeedToPlayNext) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_deleteSongInfoOnPlayList, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public int getStatus() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getStatus, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
//播放下一首

            @Override
            public void playNext() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_playNext, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//播放上一首

            @Override
            public void playPre() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_playPre, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//是否有上一首

            @Override
            public boolean hasPre() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_hasPre, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
//是否有下一首

            @Override
            public boolean hasNext() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_hasNext, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
//得到上一首信息

            @Override
            public com.lzx.musiclibrary.aidl.model.SongInfo getPreMusic() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                com.lzx.musiclibrary.aidl.model.SongInfo _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getPreMusic, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = com.lzx.musiclibrary.aidl.model.SongInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
//得到下一首信息

            @Override
            public com.lzx.musiclibrary.aidl.model.SongInfo getNextMusic() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                com.lzx.musiclibrary.aidl.model.SongInfo _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getNextMusic, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = com.lzx.musiclibrary.aidl.model.SongInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
//得到当前播放信息

            @Override
            public com.lzx.musiclibrary.aidl.model.SongInfo getCurrPlayingMusic() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                com.lzx.musiclibrary.aidl.model.SongInfo _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getCurrPlayingMusic, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = com.lzx.musiclibrary.aidl.model.SongInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
//设置当前音乐信息

            @Override
            public void setCurrMusic(int index) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(index);
                    mRemote.transact(Stub.TRANSACTION_setCurrMusic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//设置播放模式

            @Override
            public void setPlayMode(int mode, boolean isSaveLocal) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeInt(((isSaveLocal) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_setPlayMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public int getPlayMode(boolean isGetLocal) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(((isGetLocal) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_getPlayMode, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
//获取当前进度

            @Override
            public long getProgress() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                long _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getProgress, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
//定位到指定位置

            @Override
            public void seekTo(int position) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(position);
                    mRemote.transact(Stub.TRANSACTION_seekTo, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//初始化

            @Override
            public void reset() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_reset, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//更新通知栏

            @Override
            public void updateNotificationCreater(com.lzx.musiclibrary.notification.NotificationCreater creater) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((creater != null)) {
                        _data.writeInt(1);
                        creater.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_updateNotificationCreater, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//更新通知栏喜欢/收藏按钮选中状态

            @Override
            public void updateNotificationFavorite(boolean isFavorite) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(((isFavorite) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_updateNotificationFavorite, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//更新通知栏桌面歌词按钮选中状态

            @Override
            public void updateNotificationLyrics(boolean isChecked) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(((isChecked) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_updateNotificationLyrics, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//更新通知栏ContentIntent

            @Override
            public void updateNotificationContentIntent(android.os.Bundle bundle, java.lang.String targetClass) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((bundle != null)) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(targetClass);
                    mRemote.transact(Stub.TRANSACTION_updateNotificationContentIntent, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//注册一个播放状态监听器

            @Override
            public void registerPlayerEventListener(IOnPlayerEventListener listener) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder((((listener != null)) ? (listener.asBinder()) : (null)));
                    mRemote.transact(Stub.TRANSACTION_registerPlayerEventListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//解注册一个播放状态监听器

            @Override
            public void unregisterPlayerEventListener(IOnPlayerEventListener listener) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder((((listener != null)) ? (listener.asBinder()) : (null)));
                    mRemote.transact(Stub.TRANSACTION_unregisterPlayerEventListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//注册一个定时播放监听器

            @Override
            public void registerTimerTaskListener(IOnTimerTaskListener listener) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder((((listener != null)) ? (listener.asBinder()) : (null)));
                    mRemote.transact(Stub.TRANSACTION_registerTimerTaskListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
//解注册一个定时播放监听器

            @Override
            public void unregisterTimerTaskListener(IOnTimerTaskListener listener) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder((((listener != null)) ? (listener.asBinder()) : (null)));
                    mRemote.transact(Stub.TRANSACTION_unregisterTimerTaskListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_playMusic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_playMusicByInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_playMusicByIndex = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_pausePlayInMillis = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_getCurrPlayingIndex = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_pauseMusic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_resumeMusic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
        static final int TRANSACTION_stopMusic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
        static final int TRANSACTION_setPlayList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
        static final int TRANSACTION_setPlayListWithIndex = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
        static final int TRANSACTION_getPlayList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
        static final int TRANSACTION_deleteSongInfoOnPlayList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
        static final int TRANSACTION_getStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
        static final int TRANSACTION_playNext = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
        static final int TRANSACTION_playPre = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
        static final int TRANSACTION_hasPre = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
        static final int TRANSACTION_hasNext = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
        static final int TRANSACTION_getPreMusic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
        static final int TRANSACTION_getNextMusic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
        static final int TRANSACTION_getCurrPlayingMusic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
        static final int TRANSACTION_setCurrMusic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
        static final int TRANSACTION_setPlayMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
        static final int TRANSACTION_getPlayMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
        static final int TRANSACTION_getProgress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
        static final int TRANSACTION_seekTo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
        static final int TRANSACTION_reset = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
        static final int TRANSACTION_updateNotificationCreater = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
        static final int TRANSACTION_updateNotificationFavorite = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
        static final int TRANSACTION_updateNotificationLyrics = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
        static final int TRANSACTION_updateNotificationContentIntent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 29);
        static final int TRANSACTION_registerPlayerEventListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 30);
        static final int TRANSACTION_unregisterPlayerEventListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 31);
        static final int TRANSACTION_registerTimerTaskListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 32);
        static final int TRANSACTION_unregisterTimerTaskListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 33);
    }
//播放，并设置播放列表

    public void playMusic(java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> list, int index, boolean isJustPlay) throws android.os.RemoteException;
//根据音乐信息播放

    public void playMusicByInfo(com.lzx.musiclibrary.aidl.model.SongInfo info, boolean isJustPlay) throws android.os.RemoteException;
//根据索引播放

    public void playMusicByIndex(int index, boolean isJustPlay) throws android.os.RemoteException;
//设置定时时间

    public void pausePlayInMillis(long time) throws android.os.RemoteException;

    public int getCurrPlayingIndex() throws android.os.RemoteException;
//暂停

    public void pauseMusic() throws android.os.RemoteException;
//继续

    public void resumeMusic() throws android.os.RemoteException;
//停止音乐

    public void stopMusic() throws android.os.RemoteException;
//设置播放列表

    public void setPlayList(java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> list) throws android.os.RemoteException;
//设置播放列表

    public void setPlayListWithIndex(java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> list, int index) throws android.os.RemoteException;
//得到播放列表

    public java.util.List<com.lzx.musiclibrary.aidl.model.SongInfo> getPlayList() throws android.os.RemoteException;
//从播放列表中删除一条信息

    public void deleteSongInfoOnPlayList(com.lzx.musiclibrary.aidl.model.SongInfo info, boolean isNeedToPlayNext) throws android.os.RemoteException;

    public int getStatus() throws android.os.RemoteException;
//播放下一首

    public void playNext() throws android.os.RemoteException;
//播放上一首

    public void playPre() throws android.os.RemoteException;
//是否有上一首

    public boolean hasPre() throws android.os.RemoteException;
//是否有下一首

    public boolean hasNext() throws android.os.RemoteException;
//得到上一首信息

    public com.lzx.musiclibrary.aidl.model.SongInfo getPreMusic() throws android.os.RemoteException;
//得到下一首信息

    public com.lzx.musiclibrary.aidl.model.SongInfo getNextMusic() throws android.os.RemoteException;
//得到当前播放信息

    public com.lzx.musiclibrary.aidl.model.SongInfo getCurrPlayingMusic() throws android.os.RemoteException;
//设置当前音乐信息

    public void setCurrMusic(int index) throws android.os.RemoteException;
//设置播放模式

    public void setPlayMode(int mode, boolean isSaveLocal) throws android.os.RemoteException;

    public int getPlayMode(boolean isGetLocal) throws android.os.RemoteException;
//获取当前进度

    public long getProgress() throws android.os.RemoteException;
//定位到指定位置

    public void seekTo(int position) throws android.os.RemoteException;
//初始化

    public void reset() throws android.os.RemoteException;
//更新通知栏

    public void updateNotificationCreater(com.lzx.musiclibrary.notification.NotificationCreater creater) throws android.os.RemoteException;
//更新通知栏喜欢/收藏按钮选中状态

    public void updateNotificationFavorite(boolean isFavorite) throws android.os.RemoteException;
//更新通知栏桌面歌词按钮选中状态

    public void updateNotificationLyrics(boolean isChecked) throws android.os.RemoteException;
//更新通知栏ContentIntent

    public void updateNotificationContentIntent(android.os.Bundle bundle, java.lang.String targetClass) throws android.os.RemoteException;
//注册一个播放状态监听器

    public void registerPlayerEventListener(IOnPlayerEventListener listener) throws android.os.RemoteException;
//解注册一个播放状态监听器

    public void unregisterPlayerEventListener(IOnPlayerEventListener listener) throws android.os.RemoteException;
//注册一个定时播放监听器

    public void registerTimerTaskListener(IOnTimerTaskListener listener) throws android.os.RemoteException;
//解注册一个定时播放监听器

    public void unregisterTimerTaskListener(IOnTimerTaskListener listener) throws android.os.RemoteException;
}
