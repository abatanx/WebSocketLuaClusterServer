/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import jp.cielist.apps.wslua.common.Log;

import jp.cielist.apps.wslua.lua.LuaLog;
import jp.cielist.apps.wslua.lua.LuaServer;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import scala.tools.jline.console.ConsoleReader;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Timer;

public class CS implements ClientManagerDelegate
{
	public static LuaEnv sharedLuaEnv;
	public static ClientManager clientManager;
	public static HubManager hubManager;
	public static HashSet<Timer> timerManager;
	public static Mutex mutex;

	private void invokeEvent(String eventName, Varargs v)
	{
		LuaValue system   = sharedLuaEnv.getLua().get("Core");
		LuaValue events   = system.get("Events");
		LuaValue callback = events.get(eventName);
		synchronized (mutex.luaLock)
		{
			if( !callback.isnil() ) callback.invoke(v);
		}
	}

	@Override
	public void onSessionChanged()
	{
		invokeEvent("OnSessionChanged", LuaValue.varargsOf(new LuaValue[0]));
	}

	@Override
	public void onSessionJoin(WebSockMain ws)
	{
		invokeEvent("OnSessionJoin", LuaValue.varargsOf(new LuaValue[]{ws.getLuaEnv().getLua()}));
	}

	@Override
	public void onSessionLeave(WebSockMain ws)
	{
		invokeEvent("OnSessionLeave", LuaValue.varargsOf(new LuaValue[]{ws.getLuaEnv().getLua()}));
	}

	@Override
	public void onHubStart(Hub hub)
	{
		invokeEvent("OnHubStart", LuaValue.varargsOf(new LuaValue[]{hub.getLuaEnv().getLua()}));
	}

	@Override
	public void onHubEnd(Hub hub)
	{
		invokeEvent("OnHubEnd", LuaValue.varargsOf(new LuaValue[]{hub.getLuaEnv().getLua()}));
	}

	@Override
	public void onHubSessionJoin(Hub hub, WebSockMain ws)
	{
		invokeEvent("OnHubSessionJoin",
			LuaValue.varargsOf(new LuaValue[]
			{
				hub.getLuaEnv().getLua(),
				ws.getLuaEnv().getLua()
			})
		);
	}

	@Override
	public void onHubSessionLeave(Hub hub, WebSockMain ws)
	{
		invokeEvent("OnHubSessionLeave",
			LuaValue.varargsOf(new LuaValue[]
			{
				hub.getLuaEnv().getLua(),
				ws.getLuaEnv().getLua()
			})
		);
	}

	public void server(String[] args)
	{
		String propFile = "server.properties";

		Log.info("%s %s", CSConfig.AppName, CSConfig.AppVersion);
		Log.info("Copyright 2013, 2017-2021 (C) CIEL, K.K. All rights reserved.");

		mutex = new Mutex();
		clientManager = new ClientManager(this);
		hubManager = new HubManager(this);
		timerManager = new HashSet<>();

		for(int i=0; i<args.length; i++)
		{
			propFile = args[i];
		}

		Log.debug("++ Loading the server settings, %s ...", propFile);
		try
		{
			CSConfig.settings.load(propFile);
		}
		catch (Exception e)
		{
			Log.fatal(e.getMessage());
			return;
		}

		Log.info("++ Lua version, %s", Lua._VERSION);

		Log.info("++ Checking JDBC Driver, %s...", CSConfig.settings.jdbcDriver);
		try
		{
			org.postgresql.Driver d = new org.postgresql.Driver();
			Log.info("   OK, Major=%d Minor=%d", d.getMajorVersion(), d.getMinorVersion());
		}
		catch(Exception e)
		{
			Log.fatal("Can't load class, %s", e.getMessage());
			return;
		}

		Log.info("++ Creating server instance on port %d...", CSConfig.settings.port);
		Server server = new Server( CSConfig.settings.port );

		// Create DB connection
		Log.info("++ Creating database connection...");
		try
		{
			DB checkdb = new DB(
				CSConfig.settings.dbDsn,
				CSConfig.settings.dbUser,
				CSConfig.settings.dbPassword
			);
			Log.debug("   OK, %s", CSConfig.settings.dbDsn);

			Log.info("++ Checking database connection...");
			checkdb.query("SELECT %s;", DB.S("true"));
			Log.info("   OK");
		}
		catch(SQLException e)
		{
			Log.error(e.getMessage());
			return;
		}

		// bootstrap
		sharedLuaEnv = new LuaEnv(null, true, true);
		if( !CSConfig.settings.luaBootstrapFile.equals("") )
		{
			Log.info("++ Starting bootstrap...");
			try
			{
				sharedLuaEnv.run(CSConfig.settings.luaBootstrapFile, LuaValue.NIL);
			}
			catch (IOException e)
			{
				Log.error("coreLuaEnv exception: %s", e.getMessage());
			}
		}

		// websocket handler
		Log.info("++ Creating instance of WebSocket servlet...");
		WebSock wsservlet = new WebSock();
		ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		contextHandler.addServlet(new ServletHolder(wsservlet), "/");

		// Add handler
		Log.info("++ Adding into server handler...");
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { contextHandler });
		server.setHandler(handlers);

		try
		{
			server.start();
			Log.info("** Server started.");
		}
		catch (Exception e)
		{
			Log.error(e.getMessage());
		}

		try
		{
			ConsoleReader con = new ConsoleReader();
			while(true)
			{
				try
				{
					String cs  = con.readLine(">>");
					if( cs == null ) break;
					String str = cs.trim();

					if( str.equals("/stop") )
					{
						Log.notice("Timer manager has %d timer instance(s).", timerManager.size());
						for(Timer timer : timerManager)
						{
							Log.notice("Cancelling: %s", timer.toString());
							timer.cancel();
						}
						server.setStopAtShutdown(true);
						server.stop();
						break;
					}
					else if( str.length() > 0 )
					{
						try
						{
							synchronized (CS.mutex.luaLock)
							{
								sharedLuaEnv.getLua().load(str).invoke();
							}
						}
						catch(Exception e)
						{
							Log.error(e.getMessage());
						}
					}
				}
				catch(Exception e)
				{
					Log.error(e.getMessage());
					break;
				}
			}
		}
		catch (IOException e)
		{
			Log.error("Can't use readline.");
		}
	}

	public static void main(String[] args)
	{
		new CS().server(args);
	}
}
