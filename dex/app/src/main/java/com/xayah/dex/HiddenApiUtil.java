package com.xayah.dex;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.UserHandle;

import androidx.core.content.pm.PermissionInfoCompat;

public class HiddenApiUtil {

    private static void onHelp() {
        System.out.println("HiddenApiUtil commands:");
        System.out.println("  help");
        System.out.println();
        System.out.println("  getPackageUid USER_ID PACKAGE");
        System.out.println();
        System.out.println("  getRuntimePermissions USER_ID PACKAGE");
        System.out.println();
        System.out.println("  grantRuntimePermission USER_ID PACKAGE PERM_NAME");
        System.out.println();
        System.out.println("  revokeRuntimePermission USER_ID PACKAGE PERM_NAME");
    }

    private static void onCommand(String cmd, String[] args) {
        switch (cmd) {
            case "getPackageUid":
                getPackageUid(args);
            case "getRuntimePermissions":
                getRuntimePermissions(args);
            case "grantRuntimePermission":
                grantRuntimePermission(args);
            case "revokeRuntimePermission":
                revokeRuntimePermission(args);
            default:
                onHelp();
        }
    }

    public static void main(String[] args) {
        String cmd;
        if (args != null && args.length > 0) {
            cmd = args[0];
            onCommand(cmd, args);
        } else {
            onHelp();
        }
        System.exit(0);
    }

    private static void getPackageUid(String[] args) {
        try {
            Context ctx = HiddenApi.getContext();
            int userId = Integer.parseInt(args[1]);
            String packageName = args[2];
            System.out.println(HiddenApi.getPackageUid(ctx.getPackageManager(), packageName, 0, userId));
            System.exit(0);
        } catch (Exception e) {
            System.out.printf("Failed: %s, %s\n", e.getCause(), e.getMessage());
            onHelp();
            System.exit(1);
        }
    }

    private static void getRuntimePermissions(String[] args) {
        try {
            Context ctx = HiddenApi.getContext();
            PackageManager packageManager = ctx.getPackageManager();
            int userId = Integer.parseInt(args[1]);
            String packageName = args[2];
            PackageInfo packageInfo = HiddenApi.getPackageInfoAsUser(packageManager, packageName, PackageManager.GET_PERMISSIONS, userId);
            String[] requestedPermissions = packageInfo.requestedPermissions;
            int[] requestedPermissionsFlags = packageInfo.requestedPermissionsFlags;
            for (int i = 0; i < requestedPermissions.length; i++) {
                try {
                    PermissionInfo permissionInfo = packageManager.getPermissionInfo(requestedPermissions[i], 0);
                    int protection = PermissionInfoCompat.getProtection(permissionInfo);
                    int protectionFlags = PermissionInfoCompat.getProtectionFlags(permissionInfo);
                    boolean isGranted = (requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0;
                    if (protection == PermissionInfo.PROTECTION_DANGEROUS || (protectionFlags & PermissionInfo.PROTECTION_FLAG_DEVELOPMENT) != 0) {
                        System.out.println(requestedPermissions[i] + " " + isGranted);
                    }
                } catch (PackageManager.NameNotFoundException ignored) {
                }
            }
            System.exit(0);
        } catch (Exception e) {
            System.out.printf("Failed: %s, %s\n", e.getCause(), e.getMessage());
            onHelp();
            System.exit(1);
        }
    }

    private static void grantRuntimePermission(String[] args) {
        try {
            Context ctx = HiddenApi.getContext();
            int userId = Integer.parseInt(args[1]);
            String packageName = args[2];
            String permName = args[3];
            UserHandle user = HiddenApi.getUserHandle(userId);
            HiddenApi.grantRuntimePermission(ctx.getPackageManager(), packageName, permName, user);
            System.exit(0);
        } catch (Exception e) {
            System.out.printf("Failed: %s, %s\n", e.getCause(), e.getMessage());
            onHelp();
            System.exit(1);
        }
    }

    private static void revokeRuntimePermission(String[] args) {
        try {
            Context ctx = HiddenApi.getContext();
            int userId = Integer.parseInt(args[1]);
            String packageName = args[2];
            String permName = args[3];
            UserHandle user = HiddenApi.getUserHandle(userId);
            HiddenApi.revokeRuntimePermission(ctx.getPackageManager(), packageName, permName, user);
            System.exit(0);
        } catch (Exception e) {
            System.out.printf("Failed: %s, %s\n", e.getCause(), e.getMessage());
            onHelp();
            System.exit(1);
        }
    }
}
