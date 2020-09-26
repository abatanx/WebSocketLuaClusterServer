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
import org.luaj.vm2.lib.ZeroArgFunction;

import java.net.URLEncoder;
import java.sql.SQLException;

public class LuaStringUtils extends ZeroArgFunction
{
	public LuaStringUtils()
	{
		Log.debug("Activating StringUtils module...");
	}

	public LuaValue call()
	{
		LuaValue library = tableOf();
		library.set("urlencode", new urlencode());
		library.set("htmlspecialchars", new htmlspecialchars());
		return library;
	}

	/**
	 * StringUtils.urlencode
	 */
	static class urlencode extends OneArgFunction
	{
		public LuaValue call(LuaValue s)
		{
			try
			{
				return LuaValue.valueOf(URLEncoder.encode(s.tojstring(), "UTF-8"));
			}
			catch(Exception e)
			{
				Log.debug("StringUtils: %s", e.getMessage());
				return LuaValue.NIL;
			}
		}
	}

	/**
	 * StringUtils.htmlspecialchars
	 */
	static class htmlspecialchars extends OneArgFunction
	{
		private static String convertHtmlSpecialChars(String source) {

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < source.length(); i++) {
				char c = source.charAt(i);
				switch (c) {
					case '<':
						sb.append("&lt;");
						break;
					case '>':
						sb.append("&gt;");
						break;
					case '&':
						sb.append("&amp;");
						break;
					case '"':
						sb.append("&quot;");
						break;
					case '\'':
						sb.append("&apos;");
						break;
					default:
						sb.append(c);
				}
			}
			return sb.toString();
		}

		public LuaValue call(LuaValue s)
		{
			return LuaValue.valueOf(htmlspecialchars.convertHtmlSpecialChars(s.tojstring()));
		}
	}
}
