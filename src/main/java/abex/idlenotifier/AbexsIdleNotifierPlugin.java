package abex.idlenotifier;

import com.google.inject.Provides;
import java.util.stream.Stream;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PluginDescriptor(
	name = "Abex's Idle Notifier"
)
public class AbexsIdleNotifierPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private AbexsIdleNotifierConfig config;

	@Inject
	private Notifier notifier;

	private int id;
	private WorldPoint loc = null;

	@Override
	protected void startUp() throws Exception
	{
		id = -1;
		loc = null;
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked ev)
	{
		switch (ev.getMenuAction())
		{
			case GAME_OBJECT_FIRST_OPTION:
			case GAME_OBJECT_SECOND_OPTION:
			case GAME_OBJECT_THIRD_OPTION:
			case GAME_OBJECT_FOURTH_OPTION:
			case GAME_OBJECT_FIFTH_OPTION:
				if ("Mine".equals(ev.getMenuOption()))
				{
					ObjectComposition lc = client.getObjectDefinition(ev.getId());
					if (lc.getImpostorIds() != null)
					{
						lc = lc.getImpostor();
					}
					id = lc.getId();
					loc = WorldPoint.fromScene(client, ev.getActionParam(), ev.getWidgetId(), client.getPlane());
					break;
				}
				//fallthrough
			default:
				loc = null;
				id = -1;
		}
	}

	private boolean check(TileObject o)
	{
		if (o == null)
		{
			return false;
		}

		return o.getId() == id;
	}

	@Subscribe
	private void onGameTick(GameTick ev)
	{
		if (loc != null)
		{
			try
			{
				LocalPoint lp = LocalPoint.fromWorld(client, loc);
				if (lp != null)
				{
					Tile t = client.getScene().getTiles()[loc.getPlane()][lp.getSceneX()][lp.getSceneY()];
					boolean found = check(t.getWallObject());
					for (TileObject o : t.getGameObjects()) found |= check(o);
					if (found)
					{
						ItemContainer ic = client.getItemContainer(InventoryID.INVENTORY);
						if (ic == null || ic.getItems().length < 28 || Stream.of(ic.getItems()).anyMatch(i -> i.getQuantity() == 0))
						{
							return;
						}
					}
				}
			}
			catch (IndexOutOfBoundsException e)
			{
			}
			id = -1;
			loc = null;
			notifier.notify("you not busy smorc");
		}
	}

	@Provides
	AbexsIdleNotifierConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AbexsIdleNotifierConfig.class);
	}
}
