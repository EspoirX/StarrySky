/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: F:\\NiceMusic\\musiclibrary\\src\\main\\aidl\\com\\lzx\\musiclibrary\\aidl\\listener\\IOnTimerTaskListener.aidl
 */
package com.lzx.musiclibrary.aidl.source;
// Declare any non-default types here with import statements

public interface IOnTimerTaskListener extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements IOnTimerTaskListener {
        private static final java.lang.String DESCRIPTOR = "IOnTimerTaskListener";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an IOnTimerTaskListener interface,
         * generating a proxy if needed.
         * @param obj
         * @return IOnTimerTaskListener
         */
        public static IOnTimerTaskListener asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof IOnTimerTaskListener))) {
                return ((IOnTimerTaskListener) iin);
            }
            return new IOnTimerTaskListener.Stub.Proxy(obj);
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
                case TRANSACTION_onTimerFinish: {
                    data.enforceInterface(DESCRIPTOR);
                    this.onTimerFinish();
                    reply.writeNoException();
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IOnTimerTaskListener {
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

            @Override
            public void onTimerFinish() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_onTimerFinish, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_onTimerFinish = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    }

    public void onTimerFinish() throws android.os.RemoteException;
}
