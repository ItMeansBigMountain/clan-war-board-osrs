package com.itmeansbigmountain.clanwarboard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

final class ClanWarBoardPlayerOverlay extends Overlay
{
	private static final Color TEXT_COLOR = new Color(255, 210, 95);
	private final Client client;
	private final ClanWarBoardPlugin plugin;
	private final ClanWarBoardConfig config;

	@Inject
	ClanWarBoardPlayerOverlay(Client client, ClanWarBoardPlugin plugin, ClanWarBoardConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.LOW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showPlayerOverheads())
		{
			return null;
		}
		for (Player player : client.getPlayers())
		{
			if (player == null || player.getName() == null)
			{
				continue;
			}
			NearbyPlayerProfile profile = plugin.publicProfile(player.getName());
			if (profile == null)
			{
				continue;
			}
			String text = profile.overheadText(config.overheadRatingMode());
			Point point = player.getCanvasTextLocation(graphics, text, player.getLogicalHeight() + 40);
			if (point != null)
			{
				OverlayUtil.renderTextLocation(graphics, point, text, TEXT_COLOR);
			}
		}
		return null;
	}
}
