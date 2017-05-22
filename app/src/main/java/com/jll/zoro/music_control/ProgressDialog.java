package com.jll.zoro.music_control;

import android.content.Context;

public class ProgressDialog {
	public android.app.ProgressDialog dialog;

	public ProgressDialog(Context context) {
		this(context, null);
	}

	public ProgressDialog(Context context, String message) {
		this.dialog = new android.app.ProgressDialog(context);
		this.dialog.setCancelable(false);
		message = (message == null) ? "加载中..." : message;
		this.dialog.setMessage(message);
	}

	public void start() {
		this.dialog.show();
	}

	public void stop() {
		if (this.dialog != null) {
			this.dialog.dismiss();
			this.dialog.cancel();
		}
	}
}
