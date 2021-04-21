/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.lua;

import jp.cielist.apps.wslua.common.Log;
import jp.cielist.apps.wslua.server.DB;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

public class LuaDB extends ZeroArgFunction
{
	static String INTERNAL_VARIABLES = "__VARS__";

	private String dsn, user, password;
	private ArrayList<DB> connections;

	public LuaDB(String dsn, String user, String password)
	{
		Log.debug("Activating Database connection module...");
		this.dsn = dsn;
		this.user = user;
		this.password = password;

		this.connections = new ArrayList<>();
	}

	public void cleanup()
	{
		Log.debug("Cleaning up database connection...");
		for( DB db : connections )
		{
			try {
				db.close();
			}
			catch(SQLException e)
			{
				Log.error("Closing database failed, %s", e.getMessage());
			}
		}
		connections.clear();
	}

	public LuaValue call()
	{
		LuaValue library = tableOf();
		library.set("new", new _new());
		return library;
	}

	private static String[] VarArgsToStringArray(Varargs args, int start)
	{
		if (start < 1) start = 1;
		if (start > args.narg()) return new String[0];

		String[] result = new String[args.narg() - (start - 1)];
		for (int i = 0; i < args.narg() - (start - 1); i++)
		{
			result[i] = args.arg(start + i).tojstring();
		}
		return result;
	}

	private static LuaTable resultSetToLuaTable(ResultSet rs, ResultSetMetaData rsmd) throws SQLException
	{
		LuaValue fieldName, fieldValue;
		int vi;
		boolean vf;
		String vs;
		double vd;

		LuaTable columnTable = tableOf();
		for (int i = 1; i <= rsmd.getColumnCount(); i++)
		{
			fieldName = LuaValue.valueOf(rsmd.getColumnName(i));
			switch (rsmd.getColumnType(i))
			{
				case Types.INTEGER:
				case Types.TINYINT:
				case Types.SMALLINT:
				case Types.BIGINT:
				case Types.DECIMAL:
				case Types.NUMERIC:
					vi = rs.getInt(i);
					fieldValue = !rs.wasNull() ? LuaValue.valueOf(vi) : LuaValue.NIL;
					break;

				case Types.BOOLEAN:
					vf = rs.getBoolean(i);
					fieldValue = !rs.wasNull() ? LuaValue.valueOf(vf) : LuaValue.NIL;
					break;

				case Types.BIT:
					vs = rs.getString(i);
					fieldValue = !rs.wasNull() && vs != null ? LuaValue.valueOf(vs.equals("t")) : LuaValue.NIL;
					break;

				case Types.DOUBLE:
				case Types.FLOAT:
					vd = rs.getDouble(i);
					fieldValue = !rs.wasNull() ? LuaValue.valueOf(vd) : LuaValue.NIL;
					break;

				case Types.ARRAY:
				case Types.BINARY:
				case Types.BLOB:
				case Types.CHAR:
				case Types.CLOB:
				case Types.DATALINK:
				case Types.DATE:
				case Types.DISTINCT:
				case Types.JAVA_OBJECT:
				case Types.LONGNVARCHAR:
				case Types.LONGVARBINARY:
				case Types.LONGVARCHAR:
				case Types.NCHAR:
				case Types.NCLOB:
				case Types.NULL:
				case Types.NVARCHAR:
				case Types.OTHER:
				case Types.REAL:
				case Types.REF:
				case Types.REF_CURSOR:
				case Types.ROWID:
				case Types.SQLXML:
				case Types.STRUCT:
				case Types.TIME:
				case Types.TIME_WITH_TIMEZONE:
				case Types.TIMESTAMP:
				case Types.TIMESTAMP_WITH_TIMEZONE:
				case Types.VARBINARY:
				case Types.VARCHAR:
					vs = rs.getString(i);
					fieldValue = !rs.wasNull() && vs != null ? LuaValue.valueOf(vs) : LuaValue.NIL;
					break;

				default:
					fieldValue = LuaValue.NIL;
			}
			columnTable.set(fieldName, fieldValue);
		}
		return columnTable;
	}

	class InternalVariables
	{
		private DB db;
		private ResultSet rs;
		private ResultSetMetaData rsmd;

		InternalVariables()
		{
			db = null;
			rs = null;
			rsmd = null;
		}
	}

	class _new extends ZeroArgFunction
	{
		public LuaValue call()
		{
			InternalVariables vars = new InternalVariables();
			vars.db = new DB(dsn, user, password);

			connections.add(vars.db);

			LuaValue library = tableOf();
			library.set("open", new open());
			library.set("close", new close());
			library.set("Q", new Q());
			library.set("QQ", new QQ());
			library.set("F", new F());
			library.set("FALL", new FALL());
			library.set("E", new E());
			library.set("S", new S());
			library.set("N", new N());
			library.set("B", new B());
			library.set("D", new D());
			library.set(INTERNAL_VARIABLES, CoerceJavaToLua.coerce(vars));

			return library;
		}

		class open extends OneArgFunction
		{
			@Override
			public LuaValue call(LuaValue self)
			{
				try
				{
					InternalVariables vars = (InternalVariables)CoerceLuaToJava.coerce(self.get(INTERNAL_VARIABLES), InternalVariables.class);
					vars.db.open();
					return LuaValue.valueOf(true);
				}
				catch (SQLException e)
				{
					Log.error("Database access failed: %s", e.getMessage());
					return LuaValue.valueOf(false);
				}
			}
		}

		class close extends OneArgFunction
		{
			@Override
			public LuaValue call(LuaValue self)
			{
				try
				{
					InternalVariables vars = (InternalVariables)CoerceLuaToJava.coerce(self.get(INTERNAL_VARIABLES), InternalVariables.class);
					connections.remove(vars.db);

					vars.db.close();

					vars.db = null;
					vars.rs = null;
					vars.rsmd = null;

					return LuaValue.valueOf(true);
				}
				catch (SQLException e)
				{
					Log.error("Database access failed: %s", e.getMessage());
					return LuaValue.valueOf(false);
				}
			}
		}

		class E extends VarArgFunction
		{
			public Varargs invoke(Varargs args)
			{
				try
				{
					if (args.narg() < 2) return LuaValue.NIL;

					LuaValue self = args.arg(1);
					InternalVariables vars = (InternalVariables)CoerceLuaToJava.coerce(self.get(INTERNAL_VARIABLES), InternalVariables.class);

					String format = args.arg(2).tojstring();
					String[] params = VarArgsToStringArray(args, 3);

					vars.db.execute(format, (Object[]) params);
					return LuaValue.TRUE;
				}
				catch (Exception e)
				{
					Log.error("Database access failed: %s", e.getMessage());
					return LuaValue.FALSE;
				}
			}
		}

		class Q extends VarArgFunction
		{
			public Varargs invoke(Varargs args)
			{

				try
				{
					if (args.narg() < 2) return LuaValue.NIL;

					LuaValue self = args.arg(1);
					InternalVariables vars = (InternalVariables)CoerceLuaToJava.coerce(self.get(INTERNAL_VARIABLES), InternalVariables.class);

					vars.rs = null;
					vars.rsmd = null;

					String format = args.arg(2).tojstring();
					String[] params = VarArgsToStringArray(args, 3);

					vars.rs   = vars.db.Q(format, (Object[]) params);
					vars.rsmd = vars.rs.getMetaData();
				}
				catch (Exception e)
				{
					Log.error("Database access failed: %s", e.getMessage());
					return LuaValue.FALSE;
				}
				return LuaValue.TRUE;
			}
		}

		class F extends OneArgFunction
		{
			@Override
			public LuaValue call(LuaValue self)
			{
				InternalVariables vars = (InternalVariables)CoerceLuaToJava.coerce(self.get(INTERNAL_VARIABLES), InternalVariables.class);

				if( vars.rs == null || vars.rsmd == null ) return LuaValue.NIL;
				try
				{
					if( vars.rs.next() )
					{
						return resultSetToLuaTable(vars.rs, vars.rsmd);
					}
					else
					{
						return LuaValue.NIL;
					}
				}
				catch (Exception e)
				{
					Log.error("Database access failed: %s", e.getMessage());
					return LuaValue.NIL;
				}
			}
		}

		class FALL extends OneArgFunction
		{
			@Override
			public LuaValue call(LuaValue self)
			{
				InternalVariables vars = (InternalVariables)CoerceLuaToJava.coerce(self.get(INTERNAL_VARIABLES), InternalVariables.class);

				if( vars.rs == null || vars.rsmd == null ) return LuaValue.NIL;
				LuaTable resultSet = tableOf();
				try
				{
					int recordCount = 0;
					while (vars.rs.next())
					{
						resultSet.set(recordCount + 1, resultSetToLuaTable(vars.rs, vars.rsmd));
						recordCount++;
					}
					return resultSet;
				}
				catch (Exception e)
				{
					Log.error("Database access failed: %s", e.getMessage());
					return LuaValue.NIL;
				}
			}
		}

		class QQ extends VarArgFunction
		{
			public Varargs invoke(Varargs args)
			{
				LuaValue resultSet = LuaValue.NIL;
				try
				{
					if (args.narg() < 2) return LuaValue.NIL;

					LuaValue self = args.arg(1);
					InternalVariables vars = (InternalVariables)CoerceLuaToJava.coerce(self.get(INTERNAL_VARIABLES), InternalVariables.class);

					vars.rs = null;
					vars.rsmd = null;

					String format = args.arg(2).tojstring();
					String[] params = VarArgsToStringArray(args, 3);

					vars.rs = vars.db.Q(format, (Object[]) params);
					vars.rsmd = vars.rs.getMetaData();

					int recordCount = 0;
					while (vars.rs.next())
					{
						if (recordCount > 0)
						{
							// 2レコード目をセットしようとした場合、QQ では、検索失敗とする。
							resultSet = LuaValue.NIL;
							break;
						}
						else
						{
							resultSet = resultSetToLuaTable(vars.rs, vars.rsmd);
							recordCount++;
						}
					}
					return resultSet;
				}
				catch (Exception e)
				{
					Log.error("Database access failed: %s", e.getMessage());
					return LuaValue.NIL;
				}
			}
		}

		class S extends TwoArgFunction
		{
			public LuaValue call(LuaValue self, LuaValue v)
			{
				String r;
				try
				{
					r = DB.S(v.tojstring());
				}
				catch (SQLException e)
				{
					Log.error("Database access failed: %s", e.getMessage());
					r = "";
				}
				return LuaValue.valueOf(r);
			}
		}

		class N extends TwoArgFunction
		{
			public LuaValue call(LuaValue self, LuaValue v)
			{
				String r;
				try
				{
					r = DB.N(v.toint());
				}
				catch (SQLException e)
				{
					Log.error("Database access failed: %s", e.getMessage());
					r = "";
				}
				return LuaValue.valueOf(r);
			}
		}

		class D extends TwoArgFunction
		{
			public LuaValue call(LuaValue self, LuaValue v)
			{
				String r;
				try
				{
					r = DB.D(v.todouble());
				}
				catch (SQLException e)
				{
					Log.error("Database access failed: %s", e.getMessage());
					r = "";
				}
				return LuaValue.valueOf(r);
			}
		}

		class B extends TwoArgFunction
		{
			public LuaValue call(LuaValue self, LuaValue v)
			{
				String r;
				try
				{
					r = DB.B(v.toboolean());
				}
				catch (SQLException e)
				{
					Log.error("Database access failed: %s", e.getMessage());
					r = "";
				}
				return LuaValue.valueOf(r);
			}
		}
	}
}
