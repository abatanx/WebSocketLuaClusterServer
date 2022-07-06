/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 *
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import java.lang.*;
import java.util.*;
import java.sql.*;

import jp.cielist.apps.wslua.common.Log;
import org.postgresql.core.*;

public class DB
{
	private Connection connection;
	private Statement statement;
	private ArrayList<ResultSet> resultRecs;

	private String dsn, user, password;

	public DB( String dsn, String user, String password )
	{
		this.dsn = dsn;
		this.user = user;
		this.password = password;

		connection = null;
		statement = null;
	}

	public void open() throws SQLException
	{
		Log.debug( "Connecting to database..." );
		connection = DriverManager.getConnection( dsn, user, password );
	}

	public ResultSet query( String format, Object... args ) throws SQLException
	{
		String query;

		if ( statement != null )
		{
			statement.close();
			statement = null;
		}

		if ( connection == null ) open();

		statement = connection.createStatement();

		query = String.format( format, args );

		try
		{
			return statement.executeQuery( query );
		}
		catch ( SQLException e )
		{
			Log.debug( "DB.query failed, %s", query );
			Log.debug( "%s", e.getMessage() );
			throw e;
		}
	}

	public boolean execute( String format, Object... args ) throws SQLException
	{
		String query;

		if ( statement != null )
		{
			statement.close();
			statement = null;
		}

		if ( connection == null ) open();

		statement = connection.createStatement();

		query = String.format( format, args );

		try
		{
			statement.execute( query );
			return true;
		}
		catch ( SQLException e )
		{
			Log.debug( "DB.execute failed, %s", query );
			Log.debug( "%s", e.getMessage() );
			throw e;
		}
	}

	public void close() throws SQLException
	{
		if ( connection != null )
		{
			Log.debug( "Disconnecting from database..." );
			connection.close();
		}
		connection = null;
	}

	public boolean E( String format, Object... args ) throws SQLException
	{
		return execute( format, args );
	}

	public ResultSet Q( String format, Object... args ) throws SQLException
	{
		return query( format, args );
	}

	public static String S( String value ) throws SQLException
	{
		if ( value == null ) return "null";
		StringBuffer sb = Utils.appendEscapedLiteral( null, value, true );
		return "'" + sb.toString() + "'";
	}

	public static String N( int value ) throws SQLException
	{
		StringBuffer sb = Utils.appendEscapedLiteral( null, Integer.toString( value ), true );
		return sb.toString();
	}

	public static String D( double value ) throws SQLException
	{
		StringBuffer sb = Utils.appendEscapedLiteral( null, Double.toString( value ), true );
		return sb.toString();
	}

	public static String B( boolean value ) throws SQLException
	{
		return value ? "true" : "false";
	}
}
