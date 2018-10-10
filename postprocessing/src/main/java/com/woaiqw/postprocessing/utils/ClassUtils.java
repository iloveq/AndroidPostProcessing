package com.woaiqw.postprocessing.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.DexFile;

/**
 * Created by haoran on 2018/10/10.
 */
public class ClassUtils {

    private static final String TAG = "ClassUtils";
    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";
    private static final String SECONDARY_FOLDER_NAME;
    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";
    private static final int VM_WITH_MULTIDEX_VERSION_MAJOR = 2;
    private static final int VM_WITH_MULTIDEX_VERSION_MINOR = 1;

    public ClassUtils() {
    }

    private static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences("multidex.version", Build.VERSION.SDK_INT < 11 ? 0 : Context.MODE_MULTI_PROCESS);
    }

    public static <T> List<T> getObjectsWithInterface(Context context, Class<T> clazz, String path) {
        ArrayList objectList = new ArrayList();

        try {
            List<String> classFileNames = getFileNameByPackageName(context, path);
            Iterator var5 = classFileNames.iterator();

            while(var5.hasNext()) {
                String className = (String)var5.next();
                Class aClass = Class.forName(className);
                if (clazz.isAssignableFrom(aClass) && !clazz.equals(aClass) && !aClass.isInterface()) {
                    objectList.add(Class.forName(className).getConstructor().newInstance());
                }
            }

            if (objectList.size() == 0) {
                Log.e("ClassUtils", "No files were found, check your configuration please!");
            }
        } catch (Exception var8) {
            var8.getStackTrace();
            Log.e("ClassUtils", "getObjectsWithInterface error, " + var8.getMessage());
        }

        return objectList;
    }

    public static <T> List<T> getObjectsWithInterface(Context context, Class<T> clazz, List<String> pathList) {
        ArrayList objectList = new ArrayList();

        try {
            Iterator var4 = pathList.iterator();

            while(var4.hasNext()) {
                String path = (String)var4.next();
                List<String> classFileNames = getFileNameByPackageName(context, path);
                Iterator var7 = classFileNames.iterator();

                while(var7.hasNext()) {
                    String className = (String)var7.next();
                    Class aClass = Class.forName(className);
                    if (clazz.isAssignableFrom(aClass) && !clazz.equals(aClass) && !aClass.isInterface()) {
                        objectList.add(Class.forName(className).getConstructor().newInstance());
                    }
                }
            }

            if (objectList.size() == 0) {
                Log.e("ClassUtils", "No files were found, check your configuration please!");
            }
        } catch (Exception var10) {
            var10.getStackTrace();
            Log.e("ClassUtils", "getObjectsWithInterface error, " + var10.getMessage());
        }

        return objectList;
    }

    public static List<String> getFileNameByPackageName(Context context, String packageName) throws PackageManager.NameNotFoundException, IOException {
        List<String> classNames = new ArrayList();
        Iterator var3 = getSourcePaths(context).iterator();

        while(var3.hasNext()) {
            String path = (String)var3.next();
            DexFile dexfile = null;

            try {
                if (path.endsWith(".zip")) {
                    dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                } else {
                    dexfile = new DexFile(path);
                }

                Enumeration dexEntries = dexfile.entries();

                while(dexEntries.hasMoreElements()) {
                    String className = (String)dexEntries.nextElement();
                    if (className.contains(packageName)) {
                        classNames.add(className);
                    }
                }
            } catch (Throwable var16) {
                Log.e("ClassUtils", "Scan map file in dex files made error.", var16);
            } finally {
                if (null != dexfile) {
                    try {
                        dexfile.close();
                    } catch (Throwable var15) {
                        ;
                    }
                }

            }
        }

        Log.d("ClassUtils", "Filter " + classNames.size() + " classes by packageName <" + packageName + ">");
        return classNames;
    }

    public static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        File sourceApk = new File(applicationInfo.sourceDir);
        List<String> sourcePaths = new ArrayList();
        sourcePaths.add(applicationInfo.sourceDir);
        String extractedFilePrefix = sourceApk.getName() + ".classes";
        if (!isVMMultidexCapable()) {
            int totalDexNumber = getMultiDexPreferences(context).getInt("dex.number", 1);
            File dexDir = new File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME);

            for(int secondaryNumber = 2; secondaryNumber <= totalDexNumber; ++secondaryNumber) {
                String fileName = extractedFilePrefix + secondaryNumber + ".zip";
                File extractedFile = new File(dexDir, fileName);
                if (!extractedFile.isFile()) {
                    throw new IOException("Missing extracted secondary dex file '" + extractedFile.getPath() + "'");
                }

                sourcePaths.add(extractedFile.getAbsolutePath());
            }
        }

        return sourcePaths;
    }

    private static List<String> tryLoadInstantRunDexFile(ApplicationInfo applicationInfo) {
        List<String> instantRunSourcePaths = new ArrayList();
        if (Build.VERSION.SDK_INT >= 21 && null != applicationInfo.splitSourceDirs) {
            instantRunSourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
            Log.d("ClassUtils", "Found InstantRun support");
        } else {
            try {
                Class pathsByInstantRun = Class.forName("com.android.tools.fd.runtime.Paths");
                Method getDexFileDirectory = pathsByInstantRun.getMethod("getDexFileDirectory", String.class);
                String instantRunDexPath = (String)getDexFileDirectory.invoke((Object)null, applicationInfo.packageName);
                File instantRunFilePath = new File(instantRunDexPath);
                if (instantRunFilePath.exists() && instantRunFilePath.isDirectory()) {
                    File[] dexFile = instantRunFilePath.listFiles();
                    File[] var7 = dexFile;
                    int var8 = dexFile.length;

                    for(int var9 = 0; var9 < var8; ++var9) {
                        File file = var7[var9];
                        if (null != file && file.exists() && file.isFile() && file.getName().endsWith(".dex")) {
                            instantRunSourcePaths.add(file.getAbsolutePath());
                        }
                    }

                    Log.d("ClassUtils", "Found InstantRun support");
                }
            } catch (Exception var11) {
                Log.e("ClassUtils", "InstantRun support error, " + var11.getMessage());
            }
        }

        return instantRunSourcePaths;
    }

    private static boolean isVMMultidexCapable() {
        boolean isMultidexCapable = false;
        String vmName = null;

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
                            ;
                        }
                    }
                }
            }
        } catch (Exception var7) {
            ;
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

    static {
        SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";
    }

}
