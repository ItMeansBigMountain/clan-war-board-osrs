package com.itmeansbigmountain.clanwarboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.runelite.client.ui.PluginPanel;

class ClanWarBoardPanel extends PluginPanel
{
	private static final int CONTENT_WIDTH = PluginPanel.PANEL_WIDTH - PluginPanel.SCROLLBAR_WIDTH - (PluginPanel.BORDER_OFFSET * 2) - 8;
	private static final Color PANEL_BG = new Color(36, 32, 28);
	private static final Color CARD_BG = new Color(49, 44, 38);
	private static final Color BORDER = new Color(87, 78, 64);
	private static final Color TEXT = new Color(230, 224, 210);
	private static final Color MUTED = new Color(178, 170, 150);
	private static final Color LEADER = new Color(98, 152, 214);

	private final JPanel content = new JPanel();

	ClanWarBoardPanel()
	{
		super(false);
		setLayout(new BorderLayout());
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
		content.setBackground(PANEL_BG);
		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);
	}

	void update(ClanWarBoardConfig config, String clanName, String playerName, String rankName, boolean leader, ClanWarBoardApiStatus apiStatus)
	{
		content.removeAll();
		addTitle("Clan War Board");
		addCard("Your access", new String[] {
			"Player: " + clean(playerName, "Unknown"),
			"Clan: " + clean(clanName, "Join a clan to detect rank"),
			"Rank: " + clean(rankName, "Unknown"),
			leader ? "Mode: Leader setup" : "Mode: Member view"
		}, leader ? LEADER : MUTED);
		addOnlineCard(config, apiStatus);

		if (leader)
		{
			addLeaderTools(config);
		}
		else
		{
			addMemberView(config);
		}

		content.revalidate();
		content.repaint();
	}

	private void addOnlineCard(ClanWarBoardConfig config, ClanWarBoardApiStatus apiStatus)
	{
		ClanWarBoardApiStatus status = apiStatus == null ? ClanWarBoardApiStatus.offline("Waiting for service refresh") : apiStatus;
		addCard("Online board", new String[] {
			status.isOnline() ? "Status: connected" : "Status: unavailable",
			status.getMessage(),
			"Registered plugin clans: " + status.getClanCount(),
			"Open challenges: " + status.getOpenFightCount()
		}, status.isOnline() ? LEADER : MUTED);
	}

	private void addLeaderTools(ClanWarBoardConfig config)
	{
		addCard("Fight setup", new String[] {
			"War: " + clean(config.warName(), "Unnamed war"),
			"Opponent: " + clean(config.opponentClan(), "Set opponent clan"),
			"When: " + clean(config.warDate(), "Set date/time"),
			"World: " + clean(config.warWorld(), "Set world"),
			"Hotspot: " + clean(config.hotspot(), "Set hotspot")
		}, TEXT);
		addCard("Leader actions", new String[] {
			"• Edit fight details in plugin config.",
			"• Share the war plan with members.",
			"• Members see a read-only rally card.",
			"• Future: save wars and post-war summaries."
		}, LEADER);
		addCard("Rules / notes", new String[] {clean(config.rules(), "No rules set.")}, MUTED);
	}

	private void addMemberView(ClanWarBoardConfig config)
	{
		addCard("Upcoming fight", new String[] {
			clean(config.warName(), "No war configured"),
			"Vs: " + clean(config.opponentClan(), "TBD"),
			"When: " + clean(config.warDate(), "TBD"),
			"World: " + clean(config.warWorld(), "TBD"),
			"Rally: " + clean(config.hotspot(), "TBD")
		}, TEXT);
		addCard("Member instructions", new String[] {
			"• Leaders/admins configure fights.",
			"• Members use this as the rally board.",
			"• Check world, hotspot, and rules before war."
		}, MUTED);
		addCard("Rules / notes", new String[] {clean(config.rules(), "No rules set.")}, MUTED);
	}

	private void addTitle(String text)
	{
		JLabel label = label(text, 18, Font.BOLD, TEXT);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(label);
		content.add(Box.createVerticalStrut(8));
	}

	private void addCard(String title, String[] lines, Color accent)
	{
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(CARD_BG);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER, 1),
			BorderFactory.createEmptyBorder(7, 7, 7, 7)));
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setMaximumSize(new Dimension(CONTENT_WIDTH, Integer.MAX_VALUE));
		card.setPreferredSize(new Dimension(CONTENT_WIDTH, card.getPreferredSize().height));
		card.add(label(title, 13, Font.BOLD, accent));
		card.add(Box.createVerticalStrut(4));
		for (String line : lines)
		{
			card.add(wrapped(line, 11, TEXT));
			card.add(Box.createVerticalStrut(2));
		}
		content.add(card);
		content.add(Box.createVerticalStrut(8));
	}

	private static JLabel label(String text, int size, int style, Color color)
	{
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(style, (float) size));
		label.setForeground(color);
		return label;
	}

	private static JLabel wrapped(String text, int size, Color color)
	{
		JLabel label = label("<html><body style='width:" + (CONTENT_WIDTH - 18) + "px'>" + escape(text) + "</body></html>", size, Font.PLAIN, color);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private static String clean(String value, String fallback)
	{
		return value == null || value.trim().isEmpty() ? fallback : value.trim();
	}

	private static String escape(String text)
	{
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
