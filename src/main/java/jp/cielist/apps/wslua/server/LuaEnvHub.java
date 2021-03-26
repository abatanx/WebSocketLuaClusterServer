/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 *
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import jp.cielist.apps.wslua.common.Log;
import jp.cielist.apps.wslua.lua.*;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.websocket.api.Session;
import org.luaj.vm2.Globals;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaEnvHub implements HubManagerDelegate
{
	// Lua
	private Globals luaGlobals;
	private LuaValue core;
	private LuaValue hubChunk;

	public LuaDB luaDB;

	public Globals getLua()
	{
		return luaGlobals;
	}

	public LuaEnvHub(Hub hub)
	{
		int ID = CS.mutex.getMutexID();

		// Initialize the LuaGlobals
		luaGlobals = JsePlatform.standardGlobals();
		luaGlobals.package_.setLuaPath(CSConfig.settings.luaPackagePath);

		LuaValue versions = LuaValue.tableOf();
		versions.set("name", LuaValue.valueOf(CSConfig.AppName));
		versions.set("version", LuaValue.valueOf(CSConfig.AppVersion));
		versions.set("lua", LuaValue.valueOf(Lua._VERSION));
		versions.set("jetty", LuaValue.valueOf(Jetty.VERSION));
		versions.set("jre", LuaValue.valueOf(System.getProperty("java.version")));
		versions.set("jvm", LuaValue.valueOf(System.getProperty("java.vm.version")));

		core = LuaValue.tableOf();
		core.set("Versions", versions);

		core.set("Log", (new LuaLog()).call());
		core.set("Crypt", (new LuaCrypt()).call());
		core.set("DB", (luaDB = new LuaDB(CSConfig.settings.dbDsn, CSConfig.settings.dbUser, CSConfig.settings.dbPassword)).call());
		core.set("Timer", (new LuaTimer()).call());
		core.set("JSON", (new LuaJSON()).call());
		core.set("StringUtils", (new LuaStringUtils()).call());
		core.set("Server", (new LuaServer()).call());
		core.set("ID", LuaValue.valueOf(ID));

		luaGlobals.set("Core", core);

		luaGlobals.set("id", new _instance_methods_._id(hub));
		luaGlobals.set("join", new _instance_methods_.join(hub));
		luaGlobals.set("leave", new _instance_methods_.leave(hub));
		luaGlobals.set("count", new _instance_methods_.count(hub));
		luaGlobals.set("members", new _instance_methods_.members(hub));
		luaGlobals.set("isMember", new _instance_methods_.isMember(hub));

		// Callback to sharedLuaEnv only.
		luaGlobals.set("OnClose", LuaValue.NIL );
		luaGlobals.set("OnJoin", LuaValue.NIL );
		luaGlobals.set("OnJoined", LuaValue.NIL );
		luaGlobals.set("OnLeave", LuaValue.NIL );
		luaGlobals.set("OnLeft", LuaValue.NIL );

		// Stating hub.lua
		String hubLuaFilename = CSConfig.settings.luaDir + "hub.lua";
		try
		{
			hubChunk = luaGlobals.loadfile(hubLuaFilename);
			hubChunk.call();
		}
		catch (Exception e)
		{
			Log.error(e.getMessage());
		}
	}

	private void invokeEvent(String eventName, Varargs v)
	{
		LuaValue callback = getLua().get(eventName);
		if( !callback.isnil() ) callback.invoke(v);
	}

	@Override
	public void join(LuaValue object)
	{
		invokeEvent("OnJoin", LuaValue.varargsOf(new LuaValue[]{object}));
	}

	@Override
	public void joined(LuaValue object)
	{
		invokeEvent("OnJoined", LuaValue.varargsOf(new LuaValue[]{object}));
	}

	@Override
	public void leave(LuaValue object)
	{
		invokeEvent("OnLeave", LuaValue.varargsOf(new LuaValue[]{object}));
	}

	@Override
	public void left(LuaValue object)
	{
		invokeEvent("OnLeft", LuaValue.varargsOf(new LuaValue[]{object}));
	}

	@Override
	public void close()
	{
		invokeEvent("OnClose", LuaValue.varargsOf(new LuaValue[]{}));
		cleanup();
	}

	synchronized void cleanup()
	{
		luaDB.cleanup();
		luaGlobals = null;
	}

	static class _instance_methods_
	{
		static class _id extends OneArgFunction
		{
			private Hub hub;
			public _id(Hub hub)
			{
				this.hub = hub;
			}

			public LuaValue call(LuaValue self)
			{
				return LuaValue.valueOf(CS.hubManager.getId(hub));
			}
		}

		static class join extends TwoArgFunction
		{
			private Hub hub;
			private join(Hub hub)
			{
				this.hub = hub;
			}

			public LuaValue call(LuaValue self, LuaValue arg)
			{
				LuaWebSocket.InternalVariables vars =
						(LuaWebSocket.InternalVariables) CoerceLuaToJava.coerce(
								arg.get(LuaWebSocket.INTERNAL_VARIABLES),
								LuaWebSocket.InternalVariables.class
						);
				hub.join( vars.session, arg);
				return LuaValue.valueOf(true);
			}
		}

		static class leave extends TwoArgFunction
		{
			private Hub hub;
			private leave(Hub hub)
			{
				this.hub = hub;
			}

			public LuaValue call(LuaValue self, LuaValue arg)
			{
				LuaWebSocket.InternalVariables vars =
						(LuaWebSocket.InternalVariables)CoerceLuaToJava.coerce(
								arg.get(LuaWebSocket.INTERNAL_VARIABLES),
								LuaWebSocket.InternalVariables.class
						);
				hub.leave( vars.session );
				return LuaValue.valueOf(true);
			}
		}

		static class isMember extends TwoArgFunction
		{
			private Hub hub;
			private isMember(Hub hub)
			{
				this.hub = hub;
			}

			public LuaValue call(LuaValue self, LuaValue arg)
			{
				LuaWebSocket.InternalVariables vars =
						(LuaWebSocket.InternalVariables)CoerceLuaToJava.coerce(
								arg.get(LuaWebSocket.INTERNAL_VARIABLES),
								LuaWebSocket.InternalVariables.class
						);
				return LuaValue.valueOf(hub.isMember(vars.session));
			}
		}

		static class count extends OneArgFunction
		{
			private Hub hub;
			private count(Hub hub)
			{
				this.hub = hub;
			}

			public LuaValue call(LuaValue self)
			{
				return LuaValue.valueOf(hub.count());
			}
		}

		static class members extends OneArgFunction
		{
			private Hub hub;
			private members(Hub hub)
			{
				this.hub = hub;
			}

			public LuaValue call(LuaValue self)
			{
				int i = 0;
				LuaValue result = tableOf();
				for( Object o : hub.getSessions() )
				{
					if( o instanceof LuaValue ) result.set(i+1, (LuaValue) o);
					i ++;
				}
				return result;
			}
		}
	}
}
