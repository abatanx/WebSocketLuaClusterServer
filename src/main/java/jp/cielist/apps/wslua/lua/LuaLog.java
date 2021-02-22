/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.lua;

import jp.cielist.apps.wslua.common.Log;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class LuaLog extends ZeroArgFunction
{
	public LuaLog()
	{
		Log.debug("Activating Logging module...");
	}

	public LuaValue call()
	{
		LuaValue library = tableOf();
		library.set("info", new log(Log.LOGTYPE_INFO | Log.LOGTYPE_LUA, 0));
		library.set("notice", new log(Log.LOGTYPE_NOTICE | Log.LOGTYPE_LUA,0));
		library.set("warning", new log(Log.LOGTYPE_WARNING | Log.LOGTYPE_LUA,0));
		library.set("error", new log(Log.LOGTYPE_ERROR | Log.LOGTYPE_LUA,0));
		library.set("fatal", new log(Log.LOGTYPE_FATAL | Log.LOGTYPE_LUA,0));
		library.set("debug", new log(Log.LOGTYPE_DEBUG | Log.LOGTYPE_LUA,0));

		library.set("infoDump", new log(Log.LOGTYPE_INFO | Log.LOGTYPE_LUA, 1));
		library.set("noticeDump", new log(Log.LOGTYPE_NOTICE | Log.LOGTYPE_LUA,1));
		library.set("warningDump", new log(Log.LOGTYPE_WARNING | Log.LOGTYPE_LUA,1));
		library.set("errorDump", new log(Log.LOGTYPE_ERROR | Log.LOGTYPE_LUA,1));
		library.set("fatalDump", new log(Log.LOGTYPE_FATAL | Log.LOGTYPE_LUA,1));
		library.set("debugDump", new log(Log.LOGTYPE_DEBUG | Log.LOGTYPE_LUA,1));
		return library;
	}

	/**
	 * Log.log
	 */
	public class log extends OneArgFunction
	{
		private int logType;
		private int logDumpType;

		private log(int logType, int logDumpType)
		{
			this.logType = logType;
			this.logDumpType = logDumpType;
		}

		private void extract(LuaValue luaValue, ArrayList<String> logLines, String prefixSpace, String prefix, String suffix, int depth)
		{
			depth ++;
			if( depth > 10 )
			{
				logLines.add(prefixSpace + "-- Overflow depth");
				return;
			}

			if(      luaValue.isnil()     ) logLines.add(prefixSpace + prefix + "nil" + suffix);
			else if( luaValue.isboolean() ) logLines.add(prefixSpace + prefix + (luaValue.toboolean() ? "true" : "false") + suffix + "\t-- (boolean)");
			else if( luaValue.isint()     ) logLines.add(prefixSpace + prefix + String.valueOf(luaValue.toint()) + suffix + "\t-- (int)");
			else if( luaValue.islong()    ) logLines.add(prefixSpace + prefix + String.valueOf(luaValue.tolong()) + suffix + "\t-- (long)");
			else if( luaValue.isnumber()  ) logLines.add(prefixSpace + prefix + String.valueOf(luaValue.todouble()) + suffix + "\t-- (double)");
			else if( luaValue.isstring()  ) logLines.add(prefixSpace + prefix + "\"" + luaValue.tojstring() + "\"" + suffix + "\t-- (string)");
			else if( luaValue.istable()   )
			{
				logLines.add(prefixSpace + prefix + "{");
				ArrayList<Varargs> v = new ArrayList<>();

				LuaValue key = LuaValue.NIL;
				while( true )
				{
					Varargs n = luaValue.next(key);
					if ((key = n.arg1()).isnil()) break;
					v.add(n);
				}

				for( int i=0 ; i < v.size() ; i++ )
				{
					String comma = (i < v.size() - 1) ? "," : "";
					Varargs n = v.get(i);

					if( n.arg1().isint() )
					{
						extract(n.arg(2), logLines, prefixSpace + "\t","", comma, depth);
					}
					else
					{
						extract(n.arg(2), logLines, prefixSpace + "\t", String.format("%s\t=\t", n.arg1().tojstring()), comma, depth);
					}
				}

				logLines.add(prefixSpace + "}" + suffix);
			}
		}

		public LuaValue call(LuaValue luaValue)
		{
			switch( logDumpType )
			{
				case 0:
					Log.write(logType, luaValue.tojstring());
					break;

				case 1:
					ArrayList<String> logLines = new ArrayList<>();
					extract(luaValue, logLines, "", "","", 0);
					Log.write(logType, "\n" + String.join("\n", logLines.toArray(new String[0])));
					logLines.clear();
					break;
			}
			return LuaValue.TRUE;
		}
	}
}
