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

import java.io.IOException;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;


public class LuaEnv
{
	// Session
	private Session session;

	// Lua
	private Globals luaGlobals;
	private LuaValue core;
	private LuaValue initChunk, startChunk, endChunk;

	public LuaDB luaDB;

	public Globals getLua()
	{
		return luaGlobals;
	}

	public LuaEnv( Session session, boolean isCleanEnv, boolean hasEvent )
	{
		int ID = CS.mutex.getMutexID();

		//
		this.session = session;

		// Initialize the LuaGlobals
		luaGlobals = JsePlatform.standardGlobals();
		luaGlobals.package_.setLuaPath( CSConfig.settings.luaPackagePath );

		LuaValue versions = LuaValue.tableOf();
		versions.set( "name", LuaValue.valueOf( CSConfig.AppName ) );
		versions.set( "version", LuaValue.valueOf( CSConfig.AppVersion ) );
		versions.set( "lua", LuaValue.valueOf( Lua._VERSION ) );
		versions.set( "jetty", LuaValue.valueOf( Jetty.VERSION ) );
		versions.set( "jre", LuaValue.valueOf( System.getProperty( "java.version" ) ) );
		versions.set( "jvm", LuaValue.valueOf( System.getProperty( "java.vm.version" ) ) );


		core = LuaValue.tableOf();
		core.set( "Versions", versions );
		core.set( "Request", LuaValue.tableOf() );

		if ( hasEvent )
		{
			// Callback to sharedLuaEnv only.
			LuaValue events = LuaValue.tableOf();
			events.set( "OnSessionChanged", LuaValue.NIL );
			events.set( "OnSessionJoin", LuaValue.NIL );
			events.set( "OnSessionLeave", LuaValue.NIL );
			events.set( "OnHubStart", LuaValue.NIL );
			events.set( "OnHubEnd", LuaValue.NIL );
			events.set( "OnHubSessionJoin", LuaValue.NIL );
			events.set( "OnHubSessionLeave", LuaValue.NIL );
			core.set( "Events", events );
		}

		core.set( "Log", ( new LuaLog() ).call() );
		core.set( "Crypt", ( new LuaCrypt() ).call() );
		core.set( "DB", ( luaDB = new LuaDB( CSConfig.settings.dbDsn, CSConfig.settings.dbUser, CSConfig.settings.dbPassword ) ).call() );
		core.set( "WebSocket", ( new LuaWebSocket( session, ID ) ).call() );
		core.set( "Hubs", ( new LuaHubs() ).call() );
		core.set( "Timer", ( new LuaTimer() ).call() );
		core.set( "JSON", ( new LuaJSON() ).call() );
		core.set( "StringUtils", ( new LuaStringUtils() ).call() );
		core.set( "Server", ( new LuaServer() ).call() );
		core.set( "ID", LuaValue.valueOf( ID ) );

		luaGlobals.set( "Core", core );

		if ( CSConfig.settings.isEnableFirebase )
		{
			LuaValue firebase = LuaValue.tableOf();
			firebase.set( "Messaging", ( new LuaFirebaseCloudMessaging() ).call() );
			firebase.set( "Authentication", ( new LuaFirebaseAuthentication() ).call() );
			luaGlobals.set( "Firebase", firebase );
		}

		// Stating init.lua
		String initLuaFilename = CSConfig.settings.luaDir + "init.lua";
		String startLuaFilename = CSConfig.settings.luaDir + "start.lua";
		String endLuaFilename = CSConfig.settings.luaDir + "end.lua";
		try
		{
			if ( !isCleanEnv )
			{
				initChunk = luaGlobals.loadfile( initLuaFilename );
				startChunk = luaGlobals.loadfile( startLuaFilename );
				endChunk = luaGlobals.loadfile( endLuaFilename );

				initChunk.call();
			}
			else
			{
				initChunk = LuaValue.NIL;
				startChunk = LuaValue.NIL;
				endChunk = LuaValue.NIL;
			}
		}
		catch ( Exception e )
		{
			Log.error( e.getMessage() );
			try
			{
				String p = "null";
				if ( session != null ) session.getRemote().sendString( p );
				Log.sendLog( p );
			}
			catch ( IOException ei )
			{
				Log.debug( "Fatal connection error, %s", ei.getMessage() );
			}
		}
	}

	synchronized public void run( String luaFileName, LuaValue value ) throws IOException
	{
		core.set( "Request", value );
		try
		{
			LuaValue chunk = luaGlobals.loadfile( CSConfig.settings.luaDir + luaFileName );
			if ( startChunk != LuaValue.NIL ) startChunk.call();
			chunk.call();
			if ( endChunk != LuaValue.NIL ) endChunk.call();
		}
		catch ( Exception e )
		{
			Log.error( e.getMessage() );
			try
			{
				String p = "null";
				if ( session != null ) session.getRemote().sendString( p );
				Log.sendLog( p );
			}
			catch ( IOException ei )
			{
				Log.error( "Fatal connection error, %s", ei.getMessage() );
			}
		}
		luaDB.cleanup();
	}

	synchronized void cleanup()
	{

	}
}
