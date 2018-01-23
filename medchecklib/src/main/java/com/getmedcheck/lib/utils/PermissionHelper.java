package com.getmedcheck.lib.utils;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.getmedcheck.lib.R;
import com.getmedcheck.lib.listener.OnDialogClickListener;

import java.util.ArrayList;

public class PermissionHelper {

    public static boolean hasPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean hasPermissions(Context context, String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        // check for permission if any permission is not granted then return false
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissions(AppCompatActivity activity, String[] permissions, int requestCode) {
        String[] permissionArr = getNonGrantedPermissions(activity, permissions);
        if (permissionArr != null) {
            ActivityCompat.requestPermissions(activity, permissionArr, requestCode);
        }
    }

    public static void requestPermissions(Fragment fragment, String[] permissions, int requestCode) {
        String[] permissionArr = getNonGrantedPermissions(fragment.getContext(), permissions);
        if (permissionArr != null) {
            fragment.requestPermissions(permissionArr, requestCode);
        }
    }

    public static String[] getNonGrantedPermissions(Context context, String[] permissions) {
        // request only that permission which is not granted
        ArrayList<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        if (permissionList.size() > 0) {
            String[] permissionArr = new String[permissionList.size()];
            permissionList.toArray(permissionArr);
            return permissionArr;
        }
        return null;
    }

    public static boolean shouldShowPermissionRationale(AppCompatActivity activity, String[] permissions) {
        for (String permission : permissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean shouldShowPermissionRationale(Fragment fragment, String[] permissions) {
        for (String permission : permissions) {
            if (!fragment.shouldShowRequestPermissionRationale(permission)) {
                return false;
            }
        }
        return true;
    }

    public static void openSettingScreen(AppCompatActivity activity, int requestCode) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, requestCode);
    }

    public static void openSettingScreen(Fragment fragment, int requestCode) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + fragment.getActivity().getPackageName()));
        fragment.startActivityForResult(intent, requestCode);
    }

    public interface Permission {
        String[] LOCATIONS_PERMISSION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    /**
     * show dialog
     *
     * @param title               title for dialog
     * @param message             message for particular permission
     * @param dialogClickListener dialog click listener
     */
    public static void showDialog(Context context, String title, String message, String positiveButton, final OnDialogClickListener dialogClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if (dialogClickListener != null) {
                            dialogClickListener.onDialogClick(dialog, OnDialogClickListener.BUTTON_POSITIVE);
                        }
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_label_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (dialogClickListener != null) {
                            dialogClickListener.onDialogClick(dialog, OnDialogClickListener.BUTTON_NEGATIVE);
                        }
                    }
                });
        builder.show();
    }
}
