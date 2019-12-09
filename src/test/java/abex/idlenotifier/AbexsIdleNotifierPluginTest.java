package abex.idlenotifier;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AbexsIdleNotifierPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AbexsIdleNotifierPlugin.class);
		RuneLite.main(args);
	}
}