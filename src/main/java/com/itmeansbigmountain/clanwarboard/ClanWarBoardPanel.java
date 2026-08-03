package com.itmeansbigmountain.clanwarboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import net.runelite.client.ui.PluginPanel;

class ClanWarBoardPanel extends PluginPanel
{
	interface MatchActionHandler
	{
		void reloadAll();
		void submitAvailability(String startsAt, String duration, String combatMin, String combatMax, String notes);
		void submitChallenge(String opponent, String startsAt, String duration, String combatMin, String combatMax, String world, String location, String rules);
	}

	private enum Tab { OVERVIEW, BOARD, PRIVATE, HISTORY }
	private enum BoardFilter { OPEN, SCHEDULED }

	private static final int CONTENT_WIDTH = PluginPanel.PANEL_WIDTH - PluginPanel.SCROLLBAR_WIDTH - (PluginPanel.BORDER_OFFSET * 2) - 18;
	private static final Color PANEL_BG = new Color(36, 32, 28);
	private static final Color CARD_BG = new Color(49, 44, 38);
	private static final Color BORDER = new Color(87, 78, 64);
	private static final Color TEXT = new Color(238, 234, 222);
	private static final Color MUTED = new Color(178, 170, 150);
	private static final Color ACCENT = new Color(104, 174, 232);
	private static final Color SUCCESS = new Color(126, 207, 126);

	private final JPanel content = new JPanel();
	private final JLabel footerStatus = new JLabel();
	private final MatchActionHandler actionHandler;
	private ClanWarBoardState state = ClanWarBoardState.offline("Waiting for service refresh");
	private String clanName;
	private String playerName;
	private String rankName;
	private boolean leader;
	private Tab tab = Tab.OVERVIEW;
	private BoardFilter filter = BoardFilter.OPEN;
	private WarBoardFight selectedFight;
	private String privateOpponent = "";
	private boolean reloading;

	ClanWarBoardPanel(MatchActionHandler actionHandler)
	{
		super(false);
		this.actionHandler = actionHandler;
		setLayout(new BorderLayout());
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
		content.setBackground(PANEL_BG);
		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, BorderLayout.CENTER);
		footerStatus.setOpaque(true);
		footerStatus.setBackground(PANEL_BG);
		footerStatus.setForeground(MUTED);
		footerStatus.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
		add(footerStatus, BorderLayout.SOUTH);
		updateFooter();
	}

	void update(String clanName, String playerName, String rankName, boolean leader, ClanWarBoardState state)
	{
		this.clanName = clanName;
		this.playerName = playerName;
		this.rankName = rankName;
		this.leader = leader;
		this.state = state == null ? ClanWarBoardState.offline("Waiting for service refresh") : state;
		if (selectedFight != null)
		{
			selectedFight = this.state.findFightById(selectedFight.getId());
		}
		updateFooter();
		render();
	}

	void setReloading(boolean reloading)
	{
		this.reloading = reloading;
		updateFooter();
		render();
	}

	static boolean canOpenFight(WarBoardFight fight, boolean leader)
	{
		return fight != null && (!fight.needsOpponent() || leader);
	}

	static boolean canCreateFight(boolean leader)
	{
		return leader;
	}

	static String openFightCountLabel(int count)
	{
		return "Open posts: " + Math.max(0, count);
	}

	private void render()
	{
		content.removeAll();
		addTitle(selectedFight == null ? "Clan War Board" : "Fight Details");
		if (selectedFight != null)
		{
			renderFightDetails();
		}
		else
		{
			addNavigation();
			switch (tab)
			{
				case BOARD: renderBoard(); break;
				case PRIVATE: renderPrivateSetup(); break;
				case HISTORY: renderHistory(); break;
				default: renderOverview();
			}
		}
		content.revalidate();
		content.repaint();
	}

	private void addNavigation()
	{
		JPanel nav = new JPanel(new GridLayout(1, 5, 3, 0));
		nav.setOpaque(false);
		nav.setAlignmentX(Component.LEFT_ALIGNMENT);
		nav.setMaximumSize(new Dimension(CONTENT_WIDTH, 34));
		nav.add(navButton("C", "Clan overview", Tab.OVERVIEW));
		nav.add(navButton("B", "Browse the war board", Tab.BOARD));
		nav.add(navButton("+", "Create or challenge", Tab.PRIVATE));
		nav.add(navButton("H", "Fight history", Tab.HISTORY));
		JButton reload = new JButton(reloading ? "…" : "↻");
		reload.setToolTipText("Reload all Clan War Board data");
		reload.setMargin(new Insets(0, 0, 0, 0));
		reload.setFocusable(false);
		reload.setEnabled(!reloading);
		reload.setForeground(Color.WHITE);
		reload.setBackground(CARD_BG);
		reload.addActionListener(event -> {
			reloading = true;
			render();
			actionHandler.reloadAll();
		});
		nav.add(reload);
		content.add(nav);
		content.add(Box.createVerticalStrut(8));
	}

	private JButton navButton(String title, String tooltip, Tab target)
	{
		JButton button = new JButton(title);
		button.setToolTipText(tooltip);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
		button.setFocusable(false);
		button.setForeground(tab == target ? Color.WHITE : MUTED);
		button.setBackground(tab == target ? new Color(62, 94, 125) : CARD_BG);
		button.addActionListener(event -> { tab = target; selectedFight = null; render(); });
		return button;
	}

	private void updateFooter()
	{
		ClanWarBoardApiStatus status = state.getStatus();
		if (reloading)
		{
			footerStatus.setText("Refreshing clan, board, history and statistics…");
			footerStatus.setForeground(ACCENT);
		}
		else if (status.isOnline())
		{
			footerStatus.setText("Online • " + state.getAvailable().size() + " open • "
				+ state.getScheduled().size() + " set");
			footerStatus.setToolTipText(status.getMessage());
			footerStatus.setForeground(SUCCESS);
		}
		else
		{
			footerStatus.setText("Offline • use ↻ to reconnect");
			footerStatus.setToolTipText(status.getMessage());
			footerStatus.setForeground(MUTED);
		}
	}

	private void renderOverview()
	{
		addCard("Clan overview", new String[] {
			"Clan: " + clean(clanName, "No clan detected"),
			"Member: " + clean(playerName, "Unknown"),
			"Rank: " + clean(rankName, "Unknown"),
			"Plugin installs: " + state.getInstalledMembers() + "/" + state.getClanMembers(),
			leader ? "Leader tools: enabled" : "War posts: read-only"
		}, leader ? ACCENT : MUTED);
		PlayerWarMetrics metrics = state.getPlayerMetrics();
		addCard("My war statistics", new String[] {
			"Fights observed: " + metrics.getFightsObserved(),
			"Observed kills: " + metrics.getObservedKills(),
			"Deaths: " + metrics.getDeaths(),
			"Returns: " + metrics.getReturns(),
			"Opponent damage: " + metrics.getOpponentDamage(),
			"Friendly-fire damage: " + metrics.getFriendlyFireDamage(),
			"Total damage inflicted: " + metrics.getDamageInflicted(),
			"Damage received: " + metrics.getDamageReceived(),
			"Third-party damage: " + metrics.getThirdPartyDamage(),
			"Activity samples: " + metrics.getActivitySamples(),
			"Tracked war events: " + metrics.getEventsTracked()
		}, ACCENT);
		addCard("Fight record", new String[] {
			"Previous fights: " + state.getHistory().size(),
			"Scheduled fights: " + state.getScheduled().size(),
			openFightCountLabel(state.getAvailableCount())
		}, TEXT);
		WarBoardFight next = state.getNextScheduled();
		addCard("Next planned war", next == null
			? new String[] {"No future war is currently scheduled."}
			: fightLines(next), next == null ? MUTED : SUCCESS);
		addConnectionCard();
	}

	private void renderBoard()
	{
		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.setMaximumSize(new Dimension(CONTENT_WIDTH, 32));
		header.add(label("Fights", 14, Font.BOLD, TEXT), BorderLayout.WEST);
		if (canCreateFight(leader))
		{
			JButton create = new JButton("+");
			create.setToolTipText("Create fight");
			create.setFocusable(false);
			create.setPreferredSize(new Dimension(38, 30));
			create.setForeground(Color.WHITE);
			create.setBackground(new Color(62, 94, 125));
			create.addActionListener(event -> {
				privateOpponent = "";
				tab = Tab.PRIVATE;
				selectedFight = null;
				render();
			});
			header.add(create, BorderLayout.EAST);
		}
		content.add(header);
		content.add(Box.createVerticalStrut(6));
		JPanel filters = new JPanel(new GridLayout(1, 2, 4, 0));
		filters.setOpaque(false);
		filters.setAlignmentX(Component.LEFT_ALIGNMENT);
		filters.setMaximumSize(new Dimension(CONTENT_WIDTH, 32));
		filters.add(filterButton("Open (" + state.getAvailable().size() + ")", BoardFilter.OPEN));
		filters.add(filterButton("Scheduled (" + state.getScheduled().size() + ")", BoardFilter.SCHEDULED));
		content.add(filters);
		content.add(Box.createVerticalStrut(8));
		List<WarBoardFight> fights = filter == BoardFilter.OPEN ? state.getAvailable() : state.getScheduled();
		if (fights.isEmpty())
		{
			addCard(filter == BoardFilter.OPEN ? "No open war posts" : "No scheduled fights",
				new String[] {filter == BoardFilter.OPEN ? "No clan currently needs an opponent." : "No accepted fight is scheduled."}, MUTED);
			return;
		}
		for (WarBoardFight fight : fights)
		{
			if (canOpenFight(fight, leader))
			{
				addFightButton(fight);
			}
			else
			{
				addCard(clean(fight.getClanId(), "Unknown clan"), fightLines(fight), MUTED);
			}
		}
	}

	private void renderHistory()
	{
		addSectionHeader("Fight history", state.getHistory().size() + " completed");
		if (state.getHistory().isEmpty())
		{
			addCard("No completed fights", new String[] {
				"Completed, service-verified clan fights will appear here.",
				"Use ↻ if a recently completed fight is missing."
			}, MUTED);
			return;
		}
		for (WarBoardFight fight : state.getHistory())
		{
			addFightButton(fight);
		}
	}

	private void addSectionHeader(String title, String detail)
	{
		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.setMaximumSize(new Dimension(CONTENT_WIDTH, 28));
		header.add(label(title, 14, Font.BOLD, TEXT), BorderLayout.WEST);
		header.add(label(detail, 11, Font.PLAIN, MUTED), BorderLayout.EAST);
		content.add(header);
		content.add(Box.createVerticalStrut(6));
	}

	private JButton filterButton(String text, BoardFilter target)
	{
		JButton button = new JButton(text);
		button.setFocusable(false);
		button.setForeground(filter == target ? Color.WHITE : MUTED);
		button.setBackground(filter == target ? new Color(62, 94, 125) : CARD_BG);
		button.addActionListener(event -> { filter = target; render(); });
		return button;
	}

	private void addFightButton(WarBoardFight fight)
	{
		JButton button = new JButton("<html><b>" + escape(clean(fight.getClanId(), "Unknown clan")) + "</b><br>" +
			escape(clean(fight.getStartsAt(), "Time pending")) + (fight.needsOpponent() ? "<br>Needs opponent" : "<br>vs " + escape(fight.getOpponentClanId())) + "</html>");
		button.setHorizontalAlignment(JButton.LEFT);
		button.setForeground(TEXT);
		button.setBackground(CARD_BG);
		button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(7, 7, 7, 7)));
		button.setAlignmentX(Component.LEFT_ALIGNMENT);
		button.setMaximumSize(new Dimension(CONTENT_WIDTH, 70));
		button.addActionListener(event -> { selectedFight = fight; render(); });
		content.add(button);
		content.add(Box.createVerticalStrut(8));
	}

	private void renderFightDetails()
	{
		JButton back = new JButton("← Back to " + (tab == Tab.HISTORY ? "History" : "War Board"));
		back.setAlignmentX(Component.LEFT_ALIGNMENT);
		back.addActionListener(event -> { selectedFight = null; render(); });
		content.add(back);
		content.add(Box.createVerticalStrut(8));
		addCard(clean(selectedFight.getClanId(), "Fight"), fightLines(selectedFight), selectedFight.needsOpponent() ? ACCENT : SUCCESS);
		if (selectedFight.needsOpponent() && leader)
		{
			JButton accept = new JButton("Accept & schedule privately");
			accept.setAlignmentX(Component.LEFT_ALIGNMENT);
			accept.setMaximumSize(new Dimension(CONTENT_WIDTH, 34));
			accept.addActionListener(event -> {
				privateOpponent = clean(selectedFight.getClanId(), "");
				tab = Tab.PRIVATE;
				selectedFight = null;
				render();
			});
			content.add(accept);
		}
	}

	private void renderPrivateSetup()
	{
		addSectionHeader("Create a fight", leader ? "Leader tools" : "Read only");
		if (!leader)
		{
			addCard("Private match setup", new String[] {
				"Only a server-authorized clan administrator can create or accept match terms.",
				"Members can view scheduled fights from the War Board tab."
			}, MUTED);
			return;
		}
		addCard("How it works", new String[] {
			"Leave opponent blank to publish an open war post.",
			"Enter an opponent clan to send exact private terms."
		}, ACCENT);
		JLabel formError = wrapped("", 11, new Color(235, 135, 115));
		formError.setVisible(false);
		content.add(formError);
		JTextField opponent = field(privateOpponent);
		JTextField startsAt = field("");
		JTextField duration = field("30");
		JTextField combatMin = field("70");
		JTextField combatMax = field("126");
		JTextField world = field("");
		JTextField location = field("");
		JTextField rules = field("");
		content.add(label("Basics", 13, Font.BOLD, TEXT));
		content.add(Box.createVerticalStrut(5));
		addField("Opponent clan (blank for public post)", opponent);
		addField("Start time (ISO-8601, UTC)", startsAt);
		addField("Duration minutes", duration);
		addField("Combat minimum", combatMin);
		addField("Combat maximum", combatMax);
		content.add(Box.createVerticalStrut(4));
		content.add(label("Private challenge terms", 13, Font.BOLD, TEXT));
		content.add(Box.createVerticalStrut(5));
		addField("World (private challenge)", world);
		addField("Location (private challenge)", location);
		addField("Rules / notes", rules);
		JButton submit = new JButton(privateOpponent.isEmpty() ? "Post to War Board" : "Send private challenge");
		submit.setAlignmentX(Component.LEFT_ALIGNMENT);
		submit.setMaximumSize(new Dimension(CONTENT_WIDTH, 34));
		submit.addActionListener(event -> {
			String validation;
			if (opponent.getText().trim().isEmpty())
			{
				validation = MatchDraftValidator.validateAvailability(startsAt.getText(), duration.getText(), combatMin.getText(), combatMax.getText());
			}
			else
			{
				validation = MatchDraftValidator.validateChallenge(opponent.getText(), startsAt.getText(), duration.getText(), combatMin.getText(), combatMax.getText(), world.getText(), location.getText());
			}
			if (validation != null)
			{
				formError.setText("<html><body style='width:" + (CONTENT_WIDTH - 30) + "px'>" + escape(validation) + "</body></html>");
				formError.setVisible(true);
				return;
			}
			formError.setVisible(false);
			if (opponent.getText().trim().isEmpty())
			{
				actionHandler.submitAvailability(startsAt.getText(), duration.getText(), combatMin.getText(), combatMax.getText(), rules.getText());
			}
			else
			{
				actionHandler.submitChallenge(opponent.getText(), startsAt.getText(), duration.getText(), combatMin.getText(), combatMax.getText(), world.getText(), location.getText(), rules.getText());
			}
		});
		content.add(submit);
	}

	private void addField(String title, JTextField field)
	{
		content.add(wrapped(title, 11, TEXT));
		field.setAlignmentX(Component.LEFT_ALIGNMENT);
		field.setMaximumSize(new Dimension(CONTENT_WIDTH, 28));
		content.add(field);
		content.add(Box.createVerticalStrut(6));
	}

	private JTextField field(String value)
	{
		JTextField field = new JTextField(value == null ? "" : value);
		field.setBackground(CARD_BG);
		field.setForeground(TEXT);
		field.setCaretColor(TEXT);
		return field;
	}

	private void addConnectionCard()
	{
		ClanWarBoardApiStatus status = state.getStatus();
		addCard("Online board", new String[] {
			status.isOnline() ? "Connected" : "Unavailable",
			status.getMessage(),
			"Registered clans: " + status.getClanCount()
		}, status.isOnline() ? SUCCESS : MUTED);
	}

	private static String[] fightLines(WarBoardFight fight)
	{
		return new String[] {
			fight.needsOpponent() ? "Opponent: needed" : "Opponent: " + clean(fight.getOpponentClanId(), "TBD"),
			"Start: " + clean(fight.getStartsAt(), "TBD"),
			"Duration: " + fight.getDurationMinutes() + " minutes",
			"Combat: " + fight.getCombatMin() + "–" + fight.getCombatMax(),
			clean(fight.getNotes(), "No public notes")
		};
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
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER, 1), BorderFactory.createEmptyBorder(7, 7, 7, 7)));
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setMaximumSize(new Dimension(CONTENT_WIDTH, Integer.MAX_VALUE));
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
		JLabel label = label("<html><body style='width:" + (CONTENT_WIDTH - 30) + "px'>" + escape(text) + "</body></html>", size, Font.PLAIN, color);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private static String clean(String value, String fallback)
	{
		return value == null || value.trim().isEmpty() ? fallback : value.trim();
	}

	private static String escape(String text)
	{
		return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
