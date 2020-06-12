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

		LuaValue events = LuaValue.tableOf();
		events.set("OnClose", LuaValue.NIL);
		events.set("OnJoin", LuaValue.NIL);
		events.set("OnJoined", LuaValue.NIL);
		events.set("OnLeave", LuaValue.NIL);
		events.set("OnLeft", LuaValue.NIL);

		core = LuaValue.tableOf();
		core.set("Versions", versions);
		core.set("Events", events);
		core.set("Request", LuaValue.tableOf());

		core.set("Log", (new LuaLog()).call());
		core.set("Crypt", (new LuaCrypt()).call());
		core.set("DB", (luaDB = new LuaDB(CSConfig.settings.dbDsn, CSConfig.settings.dbUser, CSConfig.settings.dbPassword)).call());
//		core.set("WebSocket", (new LuaWebSocket(session)).call());
//		core.set("Hub",       (new LuaHub(session)).call());
		core.set("Timer", (new LuaTimer()).call());
		core.set("JSON", (new LuaJSON()).call());
		core.set("Hub", (new LuaHub(null,hub)).call());

		luaGlobals.set("Core", core);

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

	private void invokeEvent(String callbackFunction, LuaValue object)
	{
		LuaValue core = luaGlobals.get("Core");
		if (!core.isnil())
		{
			LuaValue events = core.get("Events");
			if (!events.isnil())
			{
				LuaValue callback = events.get(callbackFunction);
				if (!callback.isnil()) callback.invoke(object);
			}
		}
	}

	@Override
	public void join(LuaValue object)
	{
		invokeEvent("onJoin", object);
	}

	@Override
	public void joined(LuaValue object)
	{
		invokeEvent("onJoined", object);
	}

	@Override
	public void leave(LuaValue object)
	{
		invokeEvent("onLeave", object);
	}

	@Override
	public void left(LuaValue object)
	{
		invokeEvent("onLeft", object);
	}

	@Override
	public void close()
	{
		invokeEvent("onClose", LuaValue.NIL);
		cleanup();
	}

	synchronized void cleanup()
	{
		luaDB.cleanup();
		luaGlobals = null;
	}
}
