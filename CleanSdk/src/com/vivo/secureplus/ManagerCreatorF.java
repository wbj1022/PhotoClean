package com.vivo.secureplus;

import java.util.HashMap;

import android.content.Context;

public final class ManagerCreatorF {
	private static volatile ManagerCreatorF myCreatorF = null;
	private HashMap<Class<? extends BaseManager>, BaseManager> mRecentUsed = new HashMap<Class<? extends BaseManager>, BaseManager>();
	private Context mContext;

	private ManagerCreatorF(Context context) {
		this.mContext = context.getApplicationContext();
	}

	static ManagerCreatorF getInstance() {
		if (myCreatorF == null) {
			synchronized (ManagerCreatorF.class) {
				if (myCreatorF == null) {
					Context localContext = SecurePlus.getApplicationContext();
					myCreatorF = new ManagerCreatorF(localContext);
				}
			}
		}
		return myCreatorF;
	}

	public static <T extends BaseManager> T getManager(Class<T> mClass) {
		return getInstance().createManager(mClass);
	}

	@SuppressWarnings("unchecked")
	private <T extends BaseManager> T createManager(Class<T> mClass) {
		if (mClass == null) {
			throw new NullPointerException("the param of getManager can't be null.");
		}
		BaseManager localBaseManagerF;
		synchronized (mClass) {
			localBaseManagerF = mClass.cast(this.mRecentUsed.get(mClass));
			if (localBaseManagerF == null)
				try {
					localBaseManagerF = mClass.newInstance();
					localBaseManagerF.create(this.mContext);
					if (localBaseManagerF.getSingletonType() == BaseManager.TYPE_FOREVER) {
						this.mRecentUsed.put(mClass, localBaseManagerF);
					}
				} catch (Exception localException) {
					throw new RuntimeException(localException);
				}
		}
		return (T) localBaseManagerF;
	}
}