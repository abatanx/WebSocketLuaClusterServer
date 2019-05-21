/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import jp.cielist.apps.wslua.common.Log;

import java.io.IOException;

public class LuaThread extends Thread
{
	private LuaEnv luaEnv;
	private String luaFilename;

	public LuaThread(String luaFilename)
	{
		this.luaFilename = luaFilename;
		luaEnv = new LuaEnv(null, true);
	}

	public void cleanup()
	{
		luaEnv.cleanup();
	}

	@Override
	public void run()
	{
		super.run();
		try
		{
			luaEnv.run(luaFilename, new String[0]);
		}
		catch (IOException e)
		{
			Log.debug("LuaThread exception: %s", e.getMessage());
		}
	}
}
