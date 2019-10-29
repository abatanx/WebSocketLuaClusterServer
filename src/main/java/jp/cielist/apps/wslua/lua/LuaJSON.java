/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.lua;

import jp.cielist.apps.wslua.common.Log;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.io.IOException;

public class LuaJSON extends ZeroArgFunction
{
	public LuaJSON()
	{
		Log.debug("Activating JSON module...");
	}

	public LuaValue call()
	{
		LuaValue library = tableOf();
		library.set("generate", new generate());
		library.set("parse",    new parse());
		return library;
	}

	/**
	 * JSON.generate
	 */
	static class generate extends OneArgFunction
	{
		public LuaValue call(LuaValue object)
		{
			JSONValueToString c = new JSONValueToString(object);
			return LuaValue.valueOf(c.getJSONString());
		}
	}

	/**
	 * JSON.parse
	 */
	static class parse extends OneArgFunction
	{
		public LuaValue call(LuaValue string)
		{
			JSONStringToValue c = new JSONStringToValue(string.tojstring());
			return c.getLuaValue();
		}
	}
}
