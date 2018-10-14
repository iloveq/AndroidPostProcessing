package com.woaiqw.postprocessing.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.DexFile;

/**
 * Created by haoran on 2018/10/10.
 */
public class ClassUtils {

    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";
    private static final String SECONDARY_FOLDER_NAME;
    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";

    static {
        SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";
    }

    public ClassUtils() {
    }

    private static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences(PREFS_FILE, Build.VERSION.SDK_INT < 11 ? 0 : Context.MODE_MULTI_PROCESS);
    }


    public static Set<String> getFileNameByPackageName(Context context, String packageName) throws PackageManager.NameNotFoundException, IOException {
        Set<String> set = new HashSet<>();
        Iterator var3 = getSourcePaths(context).iterator();
        while (var3.hasNext()) {
            String path = (String) var3.next();
            DexFile dexfile = null;
            try {
                if (path.endsWith(EXTRACTED_SUFFIX)) {
                    dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                } else {
                    dexfile = new DexFile(path);
                }

                Enumeration dexEntries = dexfile.entries();
                while (dexEntries.hasMoreElements()) {
                    String className = (String) dexEntries.nextElement();
                    if (className.contains(packageName)) {
                        set.add(className);
                    }
                }

            } catch (Throwable var16) {
                Log.e("ClassUtils", "Scan map file in dex files made error.", var16);
            } finally {
                if (null != dexfile) {
                    try {
                        dexfile.close();
                    } catch (Throwable var15) {
                        Log.e("ClassUtils", "Scan map file in dex files made error.", var15);
                    }
                }
            }
        }
        Log.d("ClassUtils", "Filter " + set.size() + " classes by packageName <" + packageName + ">");
        return set;
    }

    private static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        File sourceApk = new File(applicationInfo.sourceDir);
        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir);
        String extractedFilePrefix = sourceApk.getName() + EXTRACTED_NAME_EXT;
        if (!isVMMultidexCapable()) {
            int totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1);
            File dexDir = new File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME);
            for (int secondaryNumber = 2; secondaryNumber <= totalDexNumber; ++secondaryNumber) {
                String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
                File extractedFile = new File(dexDir, fileName);
                if (!extractedFile.isFile()) {
                    throw new IOException("Missing extracted secondary dex file '" + extractedFile.getPath() + "'");
                }
                sourcePaths.add(extractedFile.getAbsolutePath());
            }
        }
        return sourcePaths;
    }


    private static boolean isVMMultidexCapable() {
        boolean isMultidexCapable = false;
        String vmName;
        try {
            if (isYunOS()) {
                vmName = "'YunOS'";
                isMultidexCapable = Integer.valueOf(System.getProperty("ro.build.version.sdk")) >= 21;
            } else {
                vmName = "'Android'";
                String versionString = System.getProperty("java.vm.version");
                if (versionString != null) {
                    Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
                    if (matcher.matches()) {
                        try {
                            int major = Integer.parseInt(matcher.group(1));
                            int minor = Integer.parseInt(matcher.group(2));
                            isMultidexCapable = major > 2 || major == 2 && minor >= 1;
                        } catch (NumberFormatException var6) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception var7) {
            return false;
        }
        Log.i("galaxy", "VM with name " + vmName + (isMultidexCapable ? " has multidex support" : " does not have multidex support"));
        return isMultidexCapable;
    }

    private static boolean isYunOS() {
        try {
            String version = System.getProperty("ro.yunos.version");
            String vmName = System.getProperty("java.vm.name");
            return vmName != null && vmName.toLowerCase().contains("lemur") || version != null && version.trim().length() > 0;
        } catch (Exception var2) {
            return false;
        }
    }


}
