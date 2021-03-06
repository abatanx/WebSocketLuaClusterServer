/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import jp.cielist.apps.wslua.common.Log;
import org.luaj.vm2.LuaValue;

import java.io.IOException;

public class LuaThread extends Thread
{
	private LuaEnv luaEnv;
	private String luaFilename;

	public LuaThread(String luaFilename)
	{
		this.luaFilename = luaFilename;
		luaEnv = new LuaEnv(null, true, false);
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
			synchronized (CS.mutex.luaLock)
			{
				luaEnv.run(luaFilename, LuaValue.NIL);
			}
		}
		catch (IOException e)
		{
			Log.error("LuaThread exception: %s", e.getMessage());
		}
	}

	public LuaEnv getLuaEnv()
	{
		return luaEnv;
	}
}
