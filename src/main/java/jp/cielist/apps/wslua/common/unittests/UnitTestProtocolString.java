package jp.cielist.apps.wslua.common.unittests;

import jp.cielist.apps.wslua.common.ProtocolString;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class UnitTestProtocolString
{
	static public void main(String[] args)
	{
		String s;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try
		{
			while( true )
			{
				System.out.print(" --> ");
				s = br.readLine();

				String[] proto = ProtocolString.decode(s);
				for(String p : proto)
				{
					System.out.printf("[%s]\n", p);
				}

				System.out.printf("    %s\n", ProtocolString.encode(proto));

			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
}
