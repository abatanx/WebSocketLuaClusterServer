/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.common;

import java.util.ArrayList;

/**
 * プロトコル文字列エンコード／デコード
 */
public class ProtocolString
{
	static private boolean isOct(char c)
	{
		return c>='0' && c<='7';
	}

	static private int oct(char c)
	{
		return isOct(c) ? c - '0' : 0;
	}

	static private boolean isHex(char c)
	{
		return (c>='0' && c<='9') || (c>='a' && c<='f') || (c>='A' && c<='F');
	}

	static private int hex(char c)
	{
		if( isHex(c) )
		{
			if( c>='0' && c<='9' )      return c - '0';
			else if( c>='a' && c<='f' ) return c - 'a' + 10;
			else if( c>='A' && c<='F' ) return c - 'A' + 10;
		}
		return 0;
	}

	/**
	 * 必要であれば、クオート／スラッシュ付き文字列にエンコードし、返す。
	 * @param str エンコード前文字列
	 * @return エンコード後文字列
	 */
	static public String encodeString(String str)
	{
		boolean isNeedEscape;
		StringBuilder sb = new StringBuilder();
		int i,len;
		char c;

		len = str.length();
		isNeedEscape = len==0;

		for(i=0; i<len && !isNeedEscape ; i++)
		{
			c = str.charAt(i);
			if( c<=' ' || c=='\"' )	isNeedEscape = true;
		}

		if( isNeedEscape )
		{
			for(i=0; i<len; i++)
			{
				c = str.charAt(i);
				switch(c)
				{
					case '"':	sb.append("\\\"");		break;
					case '\n':	sb.append("\\n");		break;
					case '\r':	sb.append("\\r");		break;
					case '\t':	sb.append("\\t");		break;
					case ' ':	sb.append(" ");			break;
					default:
						if( c<' ' ) sb.append(String.format("\\x%02x", (int)c));
						else sb.append(c);
						break;
				}
			}
			return "\"" + sb.toString() + "\"";
		}
		else
		{
			return str;
		}
	}

	/**
	 * パラメータ配列から、単一行プロトコル文字列を返す
	 * @param params エンコード前文字列配列
	 * @return エンコード後文字列
	 */
	static public String encode(String[] params)
	{
		ArrayList<String> encoded = new ArrayList<>();
		for(String s : params) encoded.add(encodeString(s));
		return String.join(" ", encoded);
	}

	/**
	 * 単一行プロトコル文字列から、パラメータ配列にデコードする。
	 * @param str エンコードされた文字列
	 * @return パラメータ配列
	 */
	static public String[] decode(String str) {
		char c, c2;
		ArrayList<String> parsed = new ArrayList<>();
		StringBuilder sb = new StringBuilder();

		int i, j, len = str.length();
		int x;
		boolean inQuote = false, isTraced = false;

		for (i = 0; i < len; i++)
		{
			c = str.charAt(i);
			switch (c) {
				case '"':
					isTraced = true;
					inQuote ^= true;
					break;

				default:
					if (!inQuote) {
						switch (c) {
							case ' ':
								if( isTraced )
								{
									parsed.add(sb.toString());
									sb = new StringBuilder();
								}
								isTraced = false;
								break;
							default:
								isTraced = true;
								sb.append(c);
								break;
						}
					}
					else
					{
						isTraced = true;
						switch (c)
						{
							case '\\':
								if( ++i < len )
								{
									c = str.charAt(i);
									switch(c) {
										case 'n':
											sb.append('\n');
											break;
										case 'r':
											sb.append('\r');
											break;
										case 't':
											sb.append('\t');
											break;
										case 'v':
											sb.append(Character.toChars(0x0b));
											break;
										case 'e':
											sb.append(Character.toChars(0x1b));
											break;
										case 'f':
											sb.append(Character.toChars(0x0c));
											break;
										case 'x':
											x = 0;
											for (i++, j = 0; i < len && j < 2; i++,j++) {
												c = str.charAt(i);
												if (isHex(c)) x = x * 16 + hex(c);
												else break;
											}
											i--;
											if( Character.isValidCodePoint(x) ) sb.appendCodePoint(x);
											break;
										case 'u':
											x = 0;
											for (i++, j = 0; i < len && j < 6; i++,j++) {
												c = str.charAt(i);
												if (isHex(c)) x = x * 16 + hex(c);
												else break;
											}
											i--;
											if( Character.isValidCodePoint(x) ) sb.appendCodePoint(x);
											break;
										default:
											if (isOct(c)) {
												x = oct(c);
												for (i++, j = 0; i < len && j < 2; i++, j++) {
													c = str.charAt(i);
													if (isOct(c)) x = x * 8 + oct(c);
													else break;
												}
												if( Character.isValidCodePoint(x) ) sb.appendCodePoint(x);
												i--;
											} else {
												sb.append(c);
											}
									}
								}
								break;
							default:
								sb.append(c);
								break;
						}
					}
			}
		}
		if( isTraced ) parsed.add(sb.toString());

		return parsed.toArray(new String[0]);
	}
}
