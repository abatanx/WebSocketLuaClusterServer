/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;
import org.luaj.vm2.LuaValue;

interface HubManagerDelegate
{
	public void join(LuaValue object);
	public void joined(LuaValue object);
	public void leave(LuaValue object);
	public void left(LuaValue object);
	public void close();
}
