/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.common;

import jp.cielist.apps.wslua.settings.Config;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log
{
	static private Date lastLogDate = null;

	static public int LOGTYPE_INFO    = 0x00000001;
	static public int LOGTYPE_NOTICE  = 0x00000002;
	static public int LOGTYPE_WARNING = 0x00000004;
	static public int LOGTYPE_ERROR   = 0x00000008;
	static public int LOGTYPE_FATAL   = 0x00000010;
	static public int LOGTYPE_DEBUG   = 0x00000020;
	static public int LOGTYPE_LUA     = 0x10000000;

	static public int LOGTYPE_ALL     =
			LOGTYPE_INFO | LOGTYPE_NOTICE | LOGTYPE_WARNING |
			LOGTYPE_ERROR | LOGTYPE_FATAL | LOGTYPE_DEBUG | LOGTYPE_LUA;

	static public void write(int logType, String str)
	{
		if ((Config.LOGLEVEL & logType) != 0)
		{
			Date currLogDate = new Date();

			if (lastLogDate != null)
			{
				long last = lastLogDate.getTime();
				long curr = currLogDate.getTime();
				if (curr - last > 5 * 1000)
				{
					long diff = (curr - last) / 1000;
					long min = diff / 60;
					long sec = diff % 60;

					System.out.println(
							String.format("----------------------- After %d sec (%d:%02d) ------",
									diff, min, sec)
					);
				}
			}
			lastLogDate = currLogDate;

			String et = "Srv";
			if ((logType & LOGTYPE_LUA) != 0) et = "Lua";

			String lt = "";
			if ((logType & LOGTYPE_INFO) != 0) lt = lt + "INFO";
			if ((logType & LOGTYPE_NOTICE) != 0) lt = lt + "NOTE";
			if ((logType & LOGTYPE_WARNING) != 0) lt = lt + "WARN";
			if ((logType & LOGTYPE_ERROR) != 0) lt = lt + "ERROR";
			if ((logType & LOGTYPE_FATAL) != 0) lt = lt + "FATAL";
			if ((logType & LOGTYPE_DEBUG) != 0) lt = lt + "DEBG";
			if (lt.equals("")) lt = "?";

			String d = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(currLogDate);
			System.out.println(String.format("%s:[%04X/%s/%s] %s", d, Thread.currentThread().getId(), et, lt, str));
		}
	}

	// For info
	static public void info(String str)
	{
		write(LOGTYPE_INFO, str);
	}

	static public void info(String format, Object... args)
	{
		info(String.format(format, args));
	}

	// For notice
	static public void notice(String str)
	{
		write(LOGTYPE_NOTICE, str);
	}

	static public void notice(String format, Object... args)
	{
		notice(String.format(format, args));
	}

	// For warning
	static public void warning(String str)
	{
		write(LOGTYPE_WARNING, str);
	}

	static public void warning(String format, Object... args)
	{
		warning(String.format(format, args));
	}

	// For error
	static public void error(String str)
	{
		write(LOGTYPE_ERROR, str);
	}

	static public void error(String format, Object... args)
	{
		error(String.format(format, args));
	}

	// For fatal
	static public void fatal(String str)
	{
		write(LOGTYPE_FATAL, str);
	}

	static public void fatal(String format, Object... args)
	{
		fatal(String.format(format, args));
	}

	// For Debug
	static public void debug(String str)
	{
		write(LOGTYPE_DEBUG, str);
	}

	static public void debug(String format, Object... args)
	{
		debug(String.format(format, args));
	}

	// For Debug (Socket)
	static public void sendLog(String str)
	{
		debug("S> " + str);
	}

	static public void sendLog(String format, Object... args)
	{
		sendLog(String.format(format, args));
	}

	static public void receiveLog(String str)
	{
		debug("R< " + str);
	}

	static public void receiveLog(String format, Object... args)
	{
		receiveLog(String.format(format, args));
	}
}
