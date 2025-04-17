
package abex.idlenotifier;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Notification;

@ConfigGroup("abexsidlenotifier")
public interface AbexsIdleNotifierConfig extends Config
{
	@ConfigItem(
		keyName = "notifications",
		name = "Notifications",
		description = "Configures all notifications"
	)
	default Notification notification()
	{
		return Notification.ON;
	}
}
