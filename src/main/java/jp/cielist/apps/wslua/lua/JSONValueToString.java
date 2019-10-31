package jp.cielist.apps.wslua.lua;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.luaj.vm2.*;

import java.util.*;

public class JSONValueToString
{
	private String result = "";

	public JSONValueToString(LuaValue luaValue)
	{
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			Object obj = parse(luaValue);
			result = mapper.writeValueAsString(obj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			result = "";
		}
	}

	public String getJSONString()
	{
		return result;
	}

	private Object parse(LuaValue luaValue)
	{
		if(      luaValue.isnil()     ) return null;
		else if( luaValue.isstring()  ) return luaValue.tojstring();
		else if( luaValue.isboolean() ) return luaValue.toboolean();
		else if( luaValue.isint()     ) return luaValue.toint();
		else if( luaValue.islong()    ) return luaValue.tolong();
		else if( luaValue.istable()   )
		{
			LuaValue key;
			ArrayList<Object> array = new ArrayList<>();
			LinkedHashMap<String, Object> hash = new LinkedHashMap<>();

			key = LuaValue.NIL;
			while( true )
			{
				Varargs n = luaValue.next(key);
				if( (key = n.arg1()).isnil() ) break;
				if( key.isint() )
				{
					array.add(parse(n.arg(2)));
				}
				else
				{
					hash.put(key.tojstring(), parse(n.arg(2)));
				}
			}

			if( hash.size() == 0 )
			{
				return array.toArray(new Object[0]);
			}
			else if( array.size() == 0 )
			{
				return hash;
			}
			else
			{
				LinkedHashMap<String, Object> combined = new LinkedHashMap<>();
				for(int i = 0 ; i < array.size() ; i ++ )
				{
					combined.put(String.valueOf( i + 1 ), array.get(i));
				}
				for(Map.Entry<String, Object> ent : hash.entrySet())
				{
					combined.put(ent.getKey(), ent.getValue());
				}
				return combined;
			}
		}
		return null;
	}
}
