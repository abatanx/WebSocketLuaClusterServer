package jp.cielist.apps.wslua.server;
import org.eclipse.jetty.websocket.api.Session;

public class CSSessionSupport
{
	public static String getRemoteAddress(Session session)
	{
		String remoteHostAddress = null;
		if( !session.isOpen() ) return null;
		if( CSConfig.settings.viaProxy ) remoteHostAddress = session.getUpgradeRequest().getHeader("X-Forwarded-For");
		if( remoteHostAddress == null  ) remoteHostAddress = session.getRemoteAddress().getAddress().getHostAddress();
		return remoteHostAddress;
	}
}
