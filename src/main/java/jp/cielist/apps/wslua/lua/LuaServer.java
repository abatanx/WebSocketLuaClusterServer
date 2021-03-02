/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.lua;

import jp.cielist.apps.wslua.common.Log;
import jp.cielist.apps.wslua.server.CS;
import jp.cielist.apps.wslua.server.CSConfig;
import jp.cielist.apps.wslua.server.Hub;
import jp.cielist.apps.wslua.server.WebSockMain;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class LuaServer extends ZeroArgFunction
{
	public LuaServer()
	{
		Log.debug("Activating Server module...");
	}

	public LuaValue call()
	{
		LuaValue library = tableOf();
		library.set("allHubs", new allHubs());
		library.set("allSessions", new allSessions());
		library.set("shared", new shared());
		library.set("getAppProperty", new getAppProperty());
		return library;
	}

	static class allHubs extends ZeroArgFunction
	{
		public LuaValue call()
		{
			int i = 0;
			LuaValue result = tableOf();
			for( Hub hub : CS.hubManager.hubs() )
			{
				result.set(i+1, hub.getLuaEnv().getLua());
				i ++;
			}
			return result;
		}
	}

	static class allSessions extends ZeroArgFunction
	{
		public LuaValue call()
		{
			int i = 0;
			LuaValue result = tableOf();
			for( WebSockMain ws : CS.clientManager.getClients() )
			{
				result.set(i+1, ws.getLuaEnv().getLua());
				i ++;
			}
			return result;
		}
	}

	static class getAppProperty extends OneArgFunction
	{
		public LuaValue call(LuaValue arg)
		{
			String key = arg.toString();
			if( !key.startsWith("app.") ) return LuaValue.NIL;

			String value = CSConfig.settings.getProperties().getProperty(key, null);
			return value != null ? LuaValue.valueOf(value) : LuaValue.NIL;
		}
	}

	static class shared extends ZeroArgFunction
	{
		public LuaValue call()
		{
			return CS.sharedLuaEnv.getLua();
		}
	}
}
