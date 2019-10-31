/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import jp.cielist.apps.wslua.common.Log;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.luaj.vm2.Lua;
import scala.tools.jline.console.ConsoleReader;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Timer;

public class CS implements ClientManagerDelegate
{
	public static ClientManager clientManager;
	public static HubManager hubManager;
	public static HashSet<Timer> timerManager;

	@Override
	public void onSessionChanged()
	{
		for (WebSockMain ws : clientManager.getClients() ) ws.getLua().onSessionChanged();
	}

	public void server(String[] args)
	{
		String propFile = "server.properties";

		Log.info("%s %s", CSConfig.AppName, CSConfig.AppVersion);
		Log.info("Copyright 2013, 2017-2019 (C) CIEL, K.K. All rights reserved.");

		clientManager = new ClientManager(this);
		hubManager = new HubManager();
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
		if( !CSConfig.settings.luaBootstrapFile.equals("") )
		{
			Log.info("++ Starting bootstrap...");
			(new LuaThread(CSConfig.settings.luaBootstrapFile)).start();
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
						Log.info("Timer manager has %d timer instance(s).", timerManager.size());
						for(Timer timer : timerManager)
						{
							Log.info("Cancelling: %s", timer.toString());
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
							(new LuaThread(str)).start();
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
			Log.fatal("Can't use readline.");
		}
	}

	public static void main(String[] args)
	{
		new CS().server(args);
	}
}
