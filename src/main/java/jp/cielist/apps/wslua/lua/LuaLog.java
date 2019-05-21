/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.lua;

import jp.cielist.apps.wslua.common.Log;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

public class LuaLog extends TwoArgFunction
{
	public LuaLog()
	{
		Log.debug("Activating Logging module...");
	}

	public LuaValue call(LuaValue modname, LuaValue env)
	{
		LuaValue library = tableOf();
		library.set("info", new log(Log.LOGTYPE_INFO | Log.LOGTYPE_LUA));
		library.set("notice", new log(Log.LOGTYPE_NOTICE | Log.LOGTYPE_LUA));
		library.set("warning", new log(Log.LOGTYPE_WARNING | Log.LOGTYPE_LUA));
		library.set("error", new log(Log.LOGTYPE_ERROR | Log.LOGTYPE_LUA));
		library.set("fatal", new log(Log.LOGTYPE_FATAL | Log.LOGTYPE_LUA));
		library.set("debug", new log(Log.LOGTYPE_DEBUG | Log.LOGTYPE_LUA));
		env.set("Log", library);
		return library;
	}

	/**
	 * Log.log
	 */
	class log extends OneArgFunction
	{
		private int logType;

		private log(int logType)
		{
			this.logType = logType;
		}

		public LuaValue call(LuaValue string)
		{
			Log.write(logType, string.tojstring());
			return LuaValue.TRUE;
		}
	}
}
