/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.lua;

import jp.cielist.apps.wslua.common.Log;
import jp.cielist.apps.wslua.server.CSConfig;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class LuaCrypt extends TwoArgFunction
{
	public LuaCrypt()
	{
		Log.debug("Activating Crypt module...");
	}

	public LuaValue call(LuaValue modname, LuaValue env)
	{
		LuaValue library = tableOf();
		library.set("challenge", new challenge());
		library.set("hash", new hash());
		library.set("passwordHash", new passwordHash());
		library.set("md5", new md5());
		env.set("Crypt", library);
		return library;
	}

	static class challenge extends ZeroArgFunction
	{
		public LuaValue call()
		{
			StringBuilder sb = new StringBuilder();
			byte[] token = new byte[16];

			try
			{
				SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
				rnd.nextBytes(token);
				for(byte c : token) sb.append(String.format("%02x", c));
				return LuaValue.valueOf(sb.toString());
			}
			catch(Exception e)
			{
				return LuaValue.NIL;
			}
		}
	}

	static class hash extends OneArgFunction
	{
		public LuaValue call(LuaValue str)
		{
			return LuaValue.valueOf(CSConfig.settings.hash(str.toString()));
		}
	}

	static class passwordHash extends OneArgFunction
	{
		public LuaValue call(LuaValue str)
		{
			return LuaValue.valueOf(CSConfig.settings.passwordHash(str.toString()));
		}
	}

	static class md5 extends OneArgFunction
	{
		public LuaValue call(LuaValue str)
		{
			try
			{
				StringBuilder sb = new StringBuilder();
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				byte[] digest = md5.digest(str.toString().getBytes());
				for(byte c : digest) sb.append(String.format("%02x", c));
				return LuaValue.valueOf(sb.toString());
			}
			catch(Exception e)
			{
				return LuaValue.NIL;
			}
		}
	}
}
