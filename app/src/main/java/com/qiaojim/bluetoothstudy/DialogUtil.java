package com.qiaojim.bluetoothstudy;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.WindowManager;

import cn.pedant.SweetAlert.ProgressHelper;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * author : 简玉锋
 * e-mail : yufeng_jian@fpi-inc.com
 * date   : 2019/6/25 17:05
 * desc   :
 * version: 1.0
 */
public class DialogUtil {
    private volatile static DialogUtil instance;

    public static DialogUtil getInstance() {
        if (instance == null) {
            synchronized (DialogUtil.class) {
                if (instance == null) {
                    instance = new DialogUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 隐藏dialog
     */
    public void dismissDialog(Dialog dialog) {
        if (dialog instanceof SweetAlertDialog && dialog.isShowing()) {
            ((SweetAlertDialog) dialog).dismissWithAnimation();
        }
    }

    /**
     * 显示进度框
     *
     * @param context
     */
    public Dialog showProgressDialog(Context context, String text, boolean mCancelable) {
        SweetAlertDialog pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        ProgressHelper progressHelper = pDialog.getProgressHelper();
        progressHelper.setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(text);
        pDialog.showContentText(false);
        pDialog.setCancelable(mCancelable);
        return pDialog;
    }

    /**
     * 只显示内容的提醒框
     *
     * @param context
     * @param content
     */
    public void showMessage(Context context, String content) {
        new SweetAlertDialog(context)
                .setTitleText(content)
                .show();
    }

    /**
     * 显示普通的提醒框
     *
     * @param context
     * @param title
     * @param content
     */
    public void showTitleMessage(Context context, String title, String content) {
        new SweetAlertDialog(context)
                .setTitleText(title)
                .setContentText(content)
                .show();
    }

    /**
     * 显示错误信息的提醒框
     *
     * @param context
     * @param title
     * @param content
     */
    public void showErrorMessage(Context context, String title, String content) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(content);
        if (!(context instanceof Activity)) {
            sweetAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        sweetAlertDialog.show();
    }

    /**
     * 显示警告的提醒框
     *
     * @param context
     * @param title
     * @param content
     * @param confirmText
     */
    public void showWarnMessage(Context context, String title, String content, String confirmText) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(content)
                .setConfirmText(confirmText);
        if (!(context instanceof Activity)) {
            sweetAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        sweetAlertDialog.show();
    }

    /**
     * 显示成功的提醒框
     *
     * @param context
     * @param title
     * @param content
     */
    public void showSuccessMessage(Context context, String title, String content) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(content);
        if (!(context instanceof Activity)) {
            sweetAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        sweetAlertDialog.show();
    }

    /**
     * 带有图标的提醒框
     *
     * @param context
     * @param title
     * @param content
     * @param resourceId
     */
    public void showCustomIcon(Context context, String title, String content, int resourceId) {
        new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(title)
                .setContentText(content)
                .setCustomImage(resourceId)
                .show();
    }

    /**
     * 监听确定按钮
     *
     * @param context
     * @param title
     * @param content
     * @param confirmText
     * @param listener
     */
    public void showWarnConfim(Context context, String title, String content,
                               String confirmText, SweetAlertDialog.OnSweetClickListener listener) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(content)
                .setConfirmText(confirmText)
                .setConfirmClickListener(listener)
                .show();
    }

    /**
     * 监听取消按钮
     *
     * @param context
     * @param title
     * @param content
     * @param confirmText
     * @param cancelText
     * @param listener
     */
    public void showWarnCancel(Context context, String title, String content,
                               String confirmText, String cancelText, SweetAlertDialog.OnSweetClickListener listener) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(content)
                .setCancelText(cancelText)
                .setConfirmText(confirmText)
                .showCancelButton(true)
                .setCancelClickListener(listener)
                .show();
    }

    /**
     * 变化的dialog
     *
     * @param context
     * @param title
     * @param content
     * @param confirmText
     * @param cancelText
     * @param listener
     */
    public void changeWarnStyle(Context context, String title, String content,
                                String confirmText, String cancelText, SweetAlertDialog.OnSweetClickListener listener) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Won't be able to recover this file!")
                .setConfirmText("Yes,delete it!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog
                                .setTitleText("Deleted!")
                                .setContentText("Your imaginary file has been deleted!")
                                .setConfirmText("OK")
                                .setConfirmClickListener(null)
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    }
                })
                .show();
    }
}
