/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import jp.cielist.apps.wslua.common.Log;
import jp.cielist.apps.wslua.common.ProtocolString;
import jp.cielist.apps.wslua.lua.*;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;


public class LuaEnv implements ClientManagerDelegate
{
	// Session
	private Session session;

	// Lua
	private Globals luaGlobals;
	private LuaValue initChunk, startChunk, endChunk;

	public LuaDB luaDB;

	public Globals getLua() { return luaGlobals; }

	public LuaEnv(Session session, boolean isCleanEnv)
	{
		//
		this.session = session;

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
		events.set("onSessionChanged", LuaValue.NIL);

		LuaValue system = LuaValue.tableOf();
		system.set("versions", versions);
		system.set("events", events);
		luaGlobals.set("system", system);

		(new LuaLog()).call(LuaValue.NIL, luaGlobals);
		(new LuaCrypt()).call(LuaValue.NIL, luaGlobals);
		(luaDB = new LuaDB(CSConfig.settings.dbDsn, CSConfig.settings.dbUser, CSConfig.settings.dbPassword)).call(LuaValue.NIL, luaGlobals);
		(new LuaWebSocket(session)).call(LuaValue.NIL, luaGlobals);
		(new LuaHub(session)).call(LuaValue.NIL, luaGlobals);
		(new LuaTimer()).call(LuaValue.NIL, luaGlobals);

		// Stating init.lua
		String initLuaFilename = CSConfig.settings.luaDir + "init.lua";
		String startLuaFilename = CSConfig.settings.luaDir + "start.lua";
		String endLuaFilename = CSConfig.settings.luaDir + "end.lua";
		try
		{
			if( !isCleanEnv )
			{
				initChunk = luaGlobals.loadfile(initLuaFilename);
				startChunk = luaGlobals.loadfile(startLuaFilename);
				endChunk = luaGlobals.loadfile(endLuaFilename);

				initChunk.call();
			}
			else
			{
				initChunk = LuaValue.NIL;
				startChunk = LuaValue.NIL;
				endChunk = LuaValue.NIL;
			}
		}
		catch(Exception e)
		{
			try
			{
				String p = ProtocolString.encode(new String[]{"SERVICE", "NG", "InternalServerError", e.getMessage()});
				if( session != null ) session.getRemote().sendString(p);
				Log.sendLog(p);
			}
			catch(IOException ei)
			{
				Log.debug("Fatal connection error, %s", ei.getMessage());
			}
			if( session != null ) session.close();
		}
	}

	synchronized public void run(String luaFileName, LuaValue value) throws IOException
	{
		LuaTable proto = LuaValue.tableOf();
		luaGlobals.set("in", value);

		try
		{
			LuaValue chunk = luaGlobals.loadfile(CSConfig.settings.luaDir + luaFileName);
			if( startChunk != LuaValue.NIL ) startChunk.call();
			chunk.call();
			if( endChunk != LuaValue.NIL ) endChunk.call();
		}
		catch(Exception e)
		{
			String p = ProtocolString.encode(new String[]{"SERVICE","NG","InternalServerError",luaFileName,e.getMessage()});
			if( session != null ) session.getRemote().sendString(p);
			Log.sendLog(p);
		}
		luaDB.cleanup();
	}

	@Override
	public void onSessionChanged()
	{
		LuaValue system   = luaGlobals.get("system");
		LuaValue events   = system.get("events");
		LuaValue callback = events.get("onSessionChanged");
		if( callback != LuaValue.NIL ) callback.invoke();
	}

	synchronized void cleanup()
	{

	}
}
