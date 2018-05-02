/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: F:\\NiceMusic\\musiclibrary\\src\\main\\aidl\\com\\lzx\\musiclibrary\\aidl\\listener\\IOnPlayerEventListener.aidl
 */
package com.lzx.musiclibrary.aidl.source;

import android.os.RemoteException;

import com.lzx.musiclibrary.aidl.model.SongInfo;

public interface IOnPlayerEventListener extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements IOnPlayerEventListener {
        private static final java.lang.String DESCRIPTOR = "IOnPlayerEventListener";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an IOnPlayerEventListener interface,
         * generating a proxy if needed.
         */
        public static IOnPlayerEventListener asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof IOnPlayerEventListener))) {
                return ((IOnPlayerEventListener) iin);
            }
            return new IOnPlayerEventListener.Stub.Proxy(obj);
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
                case TRANSACTION_onMusicSwitch: {
                    data.enforceInterface(DESCRIPTOR);
                    SongInfo _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = SongInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    this.onMusicSwitch(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onPlayerStart: {
                    data.enforceInterface(DESCRIPTOR);
                    this.onPlayerStart();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onPlayerPause: {
                    data.enforceInterface(DESCRIPTOR);
                    this.onPlayerPause();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onAsyncLoading: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg0;
                    _arg0 = (0 != data.readInt());
                    this.onAsyncLoading(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onPlayCompletion: {
                    data.enforceInterface(DESCRIPTOR);
                    this.onPlayCompletion();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onPlayerStop:{
                    data.enforceInterface(DESCRIPTOR);
                    this.onPlayerStop();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onError: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    this.onError(_arg0);
                    reply.writeNoException();
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IOnPlayerEventListener {
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

            /**
             * 切换歌曲
             */
            @Override
            public void onMusicSwitch(SongInfo music) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((music != null)) {
                        _data.writeInt(1);
                        music.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_onMusicSwitch, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            /**
             * 继续播放
             */
            @Override
            public void onPlayerStart() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_onPlayerStart, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            /**
             * 暂停播放
             */
            @Override
            public void onPlayerPause() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_onPlayerPause, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onAsyncLoading(boolean isFinishLoading) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(((isFinishLoading) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_onAsyncLoading, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            /**
             * 播放完成
             */
            @Override
            public void onPlayCompletion() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_onPlayCompletion, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onPlayerStop() throws RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_onPlayerStop, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onError(java.lang.String errorMsg) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(errorMsg);
                    mRemote.transact(Stub.TRANSACTION_onError, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_onMusicSwitch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_onPlayerStart = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_onPlayerPause = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_onAsyncLoading = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_onPlayCompletion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_onPlayerStop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_onError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    }

    /**
     * 切换歌曲
     */
    void onMusicSwitch(SongInfo music) throws android.os.RemoteException;

    /**
     * 继续播放
     */
    void onPlayerStart() throws android.os.RemoteException;

    /**
     * 暂停播放
     */
    void onPlayerPause() throws android.os.RemoteException;

    void onAsyncLoading(boolean isFinishLoading) throws android.os.RemoteException;

    /**
     * 播放完成
     */
    void onPlayCompletion() throws android.os.RemoteException;

    void onPlayerStop() throws android.os.RemoteException;

    void onError(java.lang.String errorMsg) throws android.os.RemoteException;
}
