package jp.cielist.apps.wslua.lua;

import org.luaj.vm2.LuaTable;

class LuaTableAccessor extends LuaTable
{
	public boolean hasArrayPart()
	{
		return getArrayLength() > 0;
	}

	public boolean hasHashPart()
	{
		return getHashLength() > 0;
	}
}

