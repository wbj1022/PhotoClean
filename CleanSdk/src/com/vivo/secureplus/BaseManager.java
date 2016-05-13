package com.vivo.secureplus;

import android.content.Context;

public abstract class BaseManager {
	private BaseManager mBaseManager;
	public static final int TYPE_AUTO = 0;
	public static final int TYPE_FOREVER = 1;
	public static final int TYPE_ONCE = 2;

	public abstract void create(Context paramContext);
	public abstract void destroy();

	public int getSingletonType() {
		return this.mBaseManager != null ? this.mBaseManager.getSingletonType() : 0;
	}

	protected <ImplType extends BaseManager> void setBManager(ImplType paramImplType) {
		this.mBaseManager = paramImplType;
	}

	@SuppressWarnings("unchecked")
	protected <ImplType extends BaseManager> ImplType getBManager() {
		return (ImplType) this.mBaseManager;
	}
}