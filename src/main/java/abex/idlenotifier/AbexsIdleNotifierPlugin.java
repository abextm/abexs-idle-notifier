package abex.idlenotifier;

import com.google.inject.Provides;
import java.util.stream.Stream;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ItemContainer;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ObjectID;
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

	private boolean shouldNotify(MenuOptionClicked ev)
	{
		switch (ev.getMenuOption())
		{
			case "Mine":
				switch (ev.getId())
				{
					case ObjectID.FOSSIL_VOLCANO_CHAMBER_BLOCKED:
						// ignore vm
						return false;
					default:
						return true;
				}
			case "Cut":
				return ev.getMenuTarget().contains("Redwood") || ev.getMenuTarget().contains("Sulliuscep");
			case "Cut down":
			case "Chop down":
			case "Chip":
				return true;
			default:
				return false;
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked ev)
	{
		switch (ev.getMenuAction())
		{
			case RUNELITE_WIDGET:
			case RUNELITE_HIGH_PRIORITY:
			case RUNELITE:
			case RUNELITE_OVERLAY:
			case RUNELITE_OVERLAY_CONFIG:
			case RUNELITE_PLAYER:
			case RUNELITE_INFOBOX:
			case RUNELITE_LOW_PRIORITY:
				break;
			case GAME_OBJECT_FIRST_OPTION:
			case GAME_OBJECT_SECOND_OPTION:
			case GAME_OBJECT_THIRD_OPTION:
			case GAME_OBJECT_FOURTH_OPTION:
			case GAME_OBJECT_FIFTH_OPTION:
				if (shouldNotify(ev))
				{
					ObjectComposition lc = client.getObjectDefinition(ev.getId());
					if (lc.getImpostorIds() != null)
					{
						lc = lc.getImpostor();
					}
					id = lc.getId();
					loc = WorldPoint.fromScene(client, ev.getParam0(), ev.getParam1(), client.getPlane());
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

		ObjectComposition lc = client.getObjectDefinition(o.getId());
		if (lc.getImpostorIds() != null)
		{
			lc = lc.getImpostor();
		}

		return lc.getId() == id;
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
						ItemContainer ic = client.getItemContainer(InventoryID.INV);
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
