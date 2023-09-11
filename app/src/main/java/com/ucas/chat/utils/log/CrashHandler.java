package com.ucas.chat.utils.log;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import com.ucas.chat.MyApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.TreeSet;

/**
 * 
 * 
 * UncaughtExceptionHandler：线程未捕获异常控制器是用来处理未捕获异常的�?如果程序出现了未捕获异常默认情况下则会出现强行关闭对话框
 * 实现该接口并注册为程序中的默认未捕获异常处理 这样当未捕获异常发生时，就可以做些异常处理操�?例如：收集异常信息，发�?错误报告 等�?
 * 
 * UncaughtException处理�?当程序发生Uncaught异常的时�?由该类来接管程序,并记录发送错误报�?
 */
public class CrashHandler implements UncaughtExceptionHandler {
	/** Debug Log Tag */
	public static final String TAG = "CrashHandler";
	/** 是否�?��日志输出, 在Debug状�?下开�? 在Release状�?下关闭以提升程序性能 */
	public static final boolean DEBUG = true;
	/** CrashHandler实例 */
	private static CrashHandler INSTANCE;
	/** 程序的Context对象 */
	private Context mContext;
	/** 系统默认的UncaughtException处理�?*/
	private UncaughtExceptionHandler mDefaultHandler;

	/** 使用Properties来保存设备的信息和错误堆栈信�?*/
	private Properties mDeviceCrashInfo = new Properties();
	private static final String VERSION_NAME = "versionName";
	private static final String VERSION_CODE = "versionCode";
	private static final String STACK_TRACE = "STACK_TRACE";
	/** 错误报告文件的扩展名 */
	private static final String CRASH_REPORTER_EXTENSION = ".txt";

	/** 保证只有�?��CrashHandler实例 */
	private CrashHandler() {
	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		if (INSTANCE == null)
			INSTANCE = new CrashHandler();
		return INSTANCE;
	}

	/**
	 * 初始�?注册Context对象, 获取系统默认的UncaughtException处理�? 设置该CrashHandler为程序的默认处理�?
	 * 
	 * @param ctx
	 */
	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		handleException(ex);
		if (mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处�?
			mDefaultHandler.uncaughtException(thread, ex);
		}
		
	}

	/**
	 * 自定义错误处�?收集错误信息 发�?错误报告等操作均在此完成. �?��者可以根据自己的情况来自定义异常处理逻辑
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false
	 */
	private boolean handleException(final Throwable ex) {
		if (ex == null) {
			return true;
		}
		final String msg = ex.getLocalizedMessage();
		new Thread() {
			@Override
			public void run() {
				// Toast 显示�?��出现在一个线程的消息队列�?
				Looper.prepare();
				Log.d( TAG,""+ex.getMessage());
				if(ex.getCause() != null){
				    Log.d(TAG, ""+ex.getCause().getMessage());
				}

				Looper.loop();
			}
		}.start();
		// 收集设备信息
		collectCrashDeviceInfo(mContext);
		// 保存错误报告文件
		String crashFileName = saveCrashInfoToFile(ex);
		// 发�?错误报告到服务器
		sendCrashReportsToServer(mContext);
		return true;
	}

	/**
	 * 收集程序崩溃的设备信�?
	 * 
	 * @param ctx
	 */
	public void collectCrashDeviceInfo(Context ctx) {
		try {
			// Class for retrieving various kinds of information related to the
			// application packages that are currently installed on the device.
			// You can find this class through getPackageManager().
			PackageManager pm = ctx.getPackageManager();
			// getPackageInfo(String packageName, int flags)
			// Retrieve overall information about an application package that is
			// installed on the system.
			// public static final int GET_ACTIVITIES
			// Since: API Level 1 PackageInfo flag: return information about
			// activities in the package in activities.
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				// public String versionName The version name of this package,
				// as specified by the <manifest> tag's versionName attribute.
				mDeviceCrashInfo.put(VERSION_NAME,
						pi.versionName == null ? "not set" : pi.versionName);
				// public int versionCode The version number of this package,
				// as specified by the <manifest> tag's versionCode attribute.
				mDeviceCrashInfo.put(VERSION_CODE, pi.versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Error while collect package info", e);
		}
		// 使用反射来收集设备信�?在Build类中包含各种设备信息,
		// 例如: 系统版本�?设备生产�?等帮助调试程序的有用信息
		// 返回 Field 对象的一个数组，这些对象反映�?Class 对象�?��示的类或接口�?��明的�?��字段
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				// setAccessible(boolean flag)
				// 将此对象�?accessible 标志设置为指示的布尔值�?
				// 通过设置Accessible属�?为true,才能对私有变量进行访问，不然会得到一个IllegalAccessException的异�?
				field.setAccessible(true);
				mDeviceCrashInfo.put(field.getName(), field.get(null));
				if (DEBUG) {
					Log.d(TAG, field.getName() + " : " + field.get(null));
				}
			} catch (Exception e) {
				Log.e(TAG, "Error while collect crash info", e);
			}
		}
	}

	/**
	 * 保存错误信息到文件中
	 *
	 * @param ex
	 * @return
	 */
	private String saveCrashInfoToFile(Throwable ex) {
		Writer info = new StringWriter();
		PrintWriter printWriter = new PrintWriter(info);
		// printStackTrace(PrintWriter s)
		// 将此 throwable 及其追踪输出到指定的 PrintWriter
		ex.printStackTrace(printWriter);

		// getCause() 返回�?throwable �?cause；如�?cause 不存在或未知，则返回 null�?
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}

		// toString() 以字符串的形式返回该缓冲区的当前值�?
		String result = info.toString();
		printWriter.close();
		mDeviceCrashInfo.put(STACK_TRACE, result);

		BufferedWriter bWriter = null;
		try {
			String dateText = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			String timeText = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
			String fileName = "chat_log_"+timeText +  CRASH_REPORTER_EXTENSION;
			String dirPath = "/sdcard/Android/data/com.ucas.chat/Log/";
			Log.d(TAG, "saveCrashInfoToFile: dirPath = "+dirPath);
			String filePath = dirPath + fileName;

//			File dirFile = new File(dirPath);
//			File file = new File(filePath);
//			if(!dirFile.exists()) {
//				dirFile.mkdirs();
//			}
//			if(!file.exists()) {
//				file.createNewFile();
//			}


			mContext = MyApplication.getContext();// TODO: 2021/7/20
			//Log.i("创建文件","创建文件");
			File file = new File(mContext.getExternalFilesDir(""), fileName);// TODO: 2021/7/20 改为存储在外部存储的私有文件夹中
			if (!file.exists()) {
				try {
					//在指定的文件夹中创建文件
					file.createNewFile();
				} catch (Exception e) {
				}
			}



			FileWriter writer = new FileWriter(file, true);
			bWriter = new BufferedWriter(writer);
			bWriter.newLine();
			String content = timeText +"\n"+ result;
			bWriter.write(content);
			bWriter.flush();
			bWriter.close();
			bWriter = null;
			return fileName;
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing report file...", e);
		} finally {
			try {
				if(bWriter != null){
					bWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 把错误报告发送给服务�?包含新产生的和以前没发�?�?
	 * 
	 * @param ctx
	 */
	private void sendCrashReportsToServer(Context ctx) {
		String[] crFiles = getCrashReportFiles(ctx);
		if (crFiles != null && crFiles.length > 0) {
			TreeSet<String> sortedFiles = new TreeSet<String>();
			sortedFiles.addAll(Arrays.asList(crFiles));

			for (String fileName : sortedFiles) {
				File cr = new File(ctx.getFilesDir(), fileName);
				postReport(cr);
				cr.delete();// 删除已发送的报告
			}
		}
	}

	/**
	 * 获取错误报告文件�?
	 * 
	 * @param ctx
	 * @return
	 */
	private String[] getCrashReportFiles(Context ctx) {
		File filesDir = ctx.getFilesDir();
		// 实现FilenameFilter接口的类实例可用于过滤器文件�?
		FilenameFilter filter = new FilenameFilter() {
			// accept(File dir, String name)
			// 测试指定文件是否应该包含在某�?��件列表中�?
			public boolean accept(File dir, String name) {
				return name.endsWith(CRASH_REPORTER_EXTENSION);
			}
		};
		// list(FilenameFilter filter)
		// 返回�?��字符串数组，这些字符串指定此抽象路径名表示的目录中满足指定过滤器的文件和目录
		return filesDir.list(filter);
	}

	private void postReport(File file) {
		
	}

	/**
	 * 在程序启动时�? 可以调用该函数来发�?以前没有发�?的报�?
	 */
	public void sendPreviousReportsToServer() {
		sendCrashReportsToServer(mContext);
	}
}
