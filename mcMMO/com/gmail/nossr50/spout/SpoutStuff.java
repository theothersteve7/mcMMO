package com.gmail.nossr50.spout;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.gui.GenericTexture;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.player.SpoutPlayer;
import org.getspout.spoutapi.sound.SoundEffect;
import org.getspout.spoutapi.sound.SoundManager;

import com.gmail.nossr50.Users;
import com.gmail.nossr50.m;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.config.LoadProperties;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.datatypes.SkillType;
import com.gmail.nossr50.datatypes.HealthBarMMO;
import com.gmail.nossr50.listeners.mcSpoutListener;
import com.gmail.nossr50.party.Party;

public class SpoutStuff 
{
	private final static mcSpoutListener spoutListener = new mcSpoutListener();
	public static HashMap<Player, GenericTexture> xpbars = new HashMap<Player, GenericTexture>();
	public static HashMap<Player, GenericTexture> xpicons = new HashMap<Player, GenericTexture>();
	public static HashMap<Player, ArrayList<HealthBarMMO>> partyHealthBars = new HashMap<Player, ArrayList<HealthBarMMO>>();
	
	static mcMMO plugin = (mcMMO) Bukkit.getServer().getPluginManager().getPlugin("mcMMO");
	
	public static void registerCustomEvent()
	{
		Bukkit.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, spoutListener, Priority.Normal, Bukkit.getServer().getPluginManager().getPlugin("mcMMO"));
	}
	public static void initializeXpBarDisplay(SpoutPlayer sPlayer)
	{
		//Setup xp bar
		GenericTexture xpbar = new GenericTexture();
		
		if(LoadProperties.xpicon)
		{
			GenericTexture xpicon = new GenericTexture();
			
			xpicon.setUrl(LoadProperties.web_url+"/HUD/Standard/icon.png");
			
			xpicon.setHeight(16).setWidth(32).setX(LoadProperties.xpicon_x).setY(LoadProperties.xpicon_y);
			
			SpoutStuff.xpicons.put(sPlayer, xpicon);
			
			sPlayer.getMainScreen().attachWidget(plugin, SpoutStuff.xpicons.get(sPlayer));
		}
		
		xpbar.setUrl(LoadProperties.web_url+"/HUD/Standard/xpbar_inc000.png");
		xpbar.setX(LoadProperties.xpbar_x).setY(LoadProperties.xpbar_y).setHeight(8).setWidth(256);
		
		SpoutStuff.xpbars.put(sPlayer, xpbar);
		
		
		sPlayer.getMainScreen().attachWidget(plugin, SpoutStuff.xpbars.get(sPlayer));
		
		sPlayer.getMainScreen().setDirty(true);
	}
	public static void initializeXpBarMiniDisplay(SpoutPlayer sPlayer)
	{
		//Coordinates 240, 427 are the bottom right.
		GenericTexture xpbar = new GenericTexture();
		GenericTexture xpbar_fill = new GenericTexture();
		
		xpbar.setUrl("http://dl.dropbox.com/u/18212134/xpbar/mini/bar.png");
		xpbar_fill.setUrl("http://dl.dropbox.com/u/18212134/xpbar/mini/bar_fill.png");
		
		xpbar.setWidth(128).setHeight(4).setX(149).setY(10).setDirty(true);
		xpbar_fill.setWidth(2).setHeight(2).setX(150).setY(11).setPriority(RenderPriority.High).setDirty(true);
		
		SpoutStuff.xpbars.put(sPlayer, xpbar);
		
		sPlayer.getMainScreen().attachWidget(plugin, xpbar);
		sPlayer.getMainScreen().attachWidget(plugin, xpbar_fill);
		
		sPlayer.getMainScreen().setDirty(true);
	}
	
	public static String getHealthBarURL(Integer hp)
	{
		String url = "";
		
		if(hp.toString().toCharArray().length > 1)
			url = LoadProperties.web_url+"/HUD/Standard/health_inc"+hp+".png";
		else
			url = LoadProperties.web_url+"/HUD/Standard/health_inc0"+hp+".png";
		
		return url;
	}
	
	public static void playSoundForPlayer(SoundEffect effect, Player player, Location location)
	{
		//Contrib stuff
		SoundManager SM = SpoutManager.getSoundManager();
		SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
		SM.playSoundEffect(sPlayer, effect, location);
	}
	
	public static void initializePartyTracking(SpoutPlayer player)
	{
		if(Users.getProfile(player).inParty())
		{
			int pos = LoadProperties.partybar_y;
			
			ArrayList<HealthBarMMO> hpbars = new ArrayList<HealthBarMMO>();
			for(Player x : Party.getInstance().getPartyMembers(player))
			{
				if(x.isOnline())
				{
					HealthBarMMO hpbar = new HealthBarMMO(x, x.getName());
					hpbar.health_name.setX(LoadProperties.partybar_x+11).setY(pos);
					hpbar.health_bar.setX(LoadProperties.partybar_x).setY(pos+8);
					hpbars.add(hpbar);
					pos+=LoadProperties.partybar_spacing;
				}
			}
			
			if(hpbars.size() >= 1)
				partyHealthBars.put(player, hpbars);
			
			if(partyHealthBars.get(player) != null)
			{
				for(HealthBarMMO x : partyHealthBars.get(player))
				{
					if(x != null)
					{
						player.getMainScreen().attachWidget(plugin, x.health_bar);
						player.getMainScreen().attachWidget(plugin, x.health_name);
					}
				}
				
				player.getMainScreen().setDirty(true);
			}
		}
	}
	public static void resetPartyHealthBarDisplays(final ArrayList<Player> players)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
		new Runnable() 
		{
			public void run() 
			{
				
				for (Player x : players) 
				{
					if(partyHealthBars.get(x) != null)
					{
						final SpoutPlayer sPlayer = SpoutManager.getPlayer(x);
						if (sPlayer.isSpoutCraftEnabled()) 
						{
							ArrayList<Widget> widgets = new ArrayList<Widget>();
							for (Widget w : sPlayer.getMainScreen().getAttachedWidgets()) 
							{
								for (HealthBarMMO hp : partyHealthBars.get(x))
								{
									if(w.getId() == hp.health_bar.getId() || w.getId() == hp.health_name.getId())
									{
										widgets.add(w);
									}
								}
							}
							for (Widget w : widgets) 
							{
								sPlayer.getMainScreen().removeWidget(w);
							}
	
							sPlayer.getMainScreen().setDirty(true);
							partyHealthBars.get(x).clear();
	
							Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
									new Runnable() 
									{
										public void run() {
											initializePartyTracking(sPlayer);
										}
									}, 1);
						}
					} else if (SpoutManager.getPlayer(x).isSpoutCraftEnabled())
					{
						initializePartyTracking(SpoutManager.getPlayer(x));
					}
				}
			}
		}, 1);
	}
	
	public static void resetPartyHealthBarDisplays(final Player player)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
		new Runnable() 
		{
			public void run() 
			{
				if(partyHealthBars.get(player) != null)
				{
					SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
					if (sPlayer.isSpoutCraftEnabled()) 
					{
						System.out.println("Resetting health bars for "+player.getName());
						ArrayList<Widget> widgets = new ArrayList<Widget>();
						for (Widget w : sPlayer.getMainScreen().getAttachedWidgets()) 
						{
							for (HealthBarMMO hp : partyHealthBars.get(player))
							{
								if(w.getId() == hp.health_bar.getId() || w.getId() == hp.health_name.getId())
								{
									widgets.add(w);
								}
							}
						}
						for (Widget w : widgets) 
						{
							System.out.println("Removing hpbar for "+sPlayer.getName());
							sPlayer.getMainScreen().removeWidget(w);
						}
						sPlayer.getMainScreen().setDirty(true);
						partyHealthBars.get(player).clear();
						
						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
						new Runnable() 
						{
							public void run() {
								initializePartyTracking(SpoutManager
										.getPlayer(player));
							}
						}, 1);
					}
				} else if (SpoutManager.getPlayer(player).isSpoutCraftEnabled())
				{
					initializePartyTracking(SpoutManager.getPlayer(player));
				}
			}
		}, 1);
	}
	
	public static void updatePartyHealthBarDisplay(Player player, Integer hp)
	{
		for(Player x : Party.getInstance().getPartyMembers(player))
		{
			SpoutPlayer sPlayer = SpoutManager.getPlayer(x);
			if(sPlayer.isSpoutCraftEnabled())
			{
				for(HealthBarMMO y : partyHealthBars.get(x))
				{
					if(y.playerName.equalsIgnoreCase(player.getName()))
					{
						y.health_bar.setUrl(getHealthBarURL(hp)).setDirty(true);
						sPlayer.getMainScreen().setDirty(true);
					}
				}
			}
		}
	}
	
	public static void playRepairNoise(Player player)
	{
		SoundManager SM = SpoutManager.getSoundManager();
		SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
		SM.playCustomMusic(Bukkit.getServer().getPluginManager().getPlugin("mcMMO"), sPlayer, LoadProperties.web_url+"/Sound/repair.wav", false);
	}
	public static void playLevelUpNoise(Player player)
	{
		//SoundManager SM = SpoutManager.getSoundManager();
		//SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
		//SM.playCustomMusic(Bukkit.getServer().getPluginManager().getPlugin("mcMMO"), sPlayer, "http://dl.dropbox.com/u/18212134/ANUSOUND/"+(int)Math.random()*8+".wav", false);
	}
	
	public static void levelUpNotification(SkillType skillType, SpoutPlayer sPlayer)
	{
		PlayerProfile PP = Users.getProfile(sPlayer);
		
		Material mat = null;
		switch(skillType)
		{
		case SORCERY:
			mat = Material.PORTAL;
			break;
		case TAMING:
			switch(getNotificationTier(PP.getSkillLevel(skillType)))
			{
			case 1:
				mat = Material.PORK;
				break;
			case 2:
				mat = Material.PORK;
				break;
			case 3:
				mat = Material.GRILLED_PORK;
				break;
			case 4:
				mat = Material.GRILLED_PORK;
				break;
			case 5:
				mat = Material.BONE;
				break;
			}
			break;
		case MINING:
			switch(getNotificationTier(PP.getSkillLevel(skillType)))
			{
			case 1:
				mat = Material.COAL_ORE;
				break;
			case 2:
				mat = Material.IRON_ORE;
				break;
			case 3:
				mat = Material.GOLD_ORE;
				break;
			case 4:
				mat = Material.LAPIS_ORE;
				break;
			case 5:
				mat = Material.DIAMOND_ORE;
				break;
			}
			break;
		case WOODCUTTING:
			switch(getNotificationTier(PP.getSkillLevel(skillType)))
			{
			case 1:
				mat = Material.WOOD;
				break;
			case 2:
				mat = Material.WOOD;
				break;
			case 3:
				mat = Material.WOOD;
				break;
			case 4:
				mat = Material.LOG;
				break;
			case 5:
				mat = Material.LOG;
				break;
			}
			break;
		case REPAIR:
			switch(getNotificationTier(PP.getSkillLevel(skillType)))
			{
			case 1:
				mat = Material.COBBLESTONE;
				break;
			case 2:
				mat = Material.IRON_BLOCK;
				break;
			case 3:
				mat = Material.GOLD_BLOCK;
				break;
			case 4:
				mat = Material.LAPIS_BLOCK;
				break;
			case 5:
				mat = Material.DIAMOND_BLOCK;
				break;
			}
			break;
		case HERBALISM:
			switch(getNotificationTier(PP.getSkillLevel(skillType)))
			{
			case 1:
				mat = Material.YELLOW_FLOWER;
				break;
			case 2:
				mat = Material.RED_ROSE;
				break;
			case 3:
				mat = Material.BROWN_MUSHROOM;
				break;
			case 4:
				mat = Material.RED_MUSHROOM;
				break;
			case 5:
				mat = Material.PUMPKIN;
				break;
			}
			break;
		case ACROBATICS:
			switch(getNotificationTier(PP.getSkillLevel(skillType)))
			{
			case 1:
				mat = Material.LEATHER_BOOTS;
				break;
			case 2:
				mat = Material.CHAINMAIL_BOOTS;
				break;
			case 3:
				mat = Material.IRON_BOOTS;
				break;
			case 4:
				mat = Material.GOLD_BOOTS;
				break;
			case 5:
				mat = Material.DIAMOND_BOOTS;
				break;
			}
			break;
		case SWORDS:
			switch(getNotificationTier(PP.getSkillLevel(skillType)))
			{
			case 1:
				mat = Material.WOOD_SWORD;
				break;
			case 2:
				mat = Material.STONE_SWORD;
				break;
			case 3:
				mat = Material.IRON_SWORD;
				break;
			case 4:
				mat = Material.GOLD_SWORD;
				break;
			case 5:
				mat = Material.DIAMOND_SWORD;
				break;
			}
			break;
		case ARCHERY:
			mat = Material.ARROW;
			break;
		case UNARMED:
			switch(getNotificationTier(PP.getSkillLevel(skillType)))
			{
			case 1:
				mat = Material.LEATHER_HELMET;
				break;
			case 2:
				mat = Material.CHAINMAIL_HELMET;
				break;
			case 3:
				mat = Material.IRON_HELMET;
				break;
			case 4:
				mat = Material.GOLD_HELMET;
				break;
			case 5:
				mat = Material.DIAMOND_HELMET;
				break;
			}
			break;
		case EXCAVATION:
			switch(getNotificationTier(PP.getSkillLevel(skillType)))
			{
			case 1:
				mat = Material.WOOD_SPADE;
				break;
			case 2:
				mat = Material.STONE_SPADE;
				break;
			case 3:
				mat = Material.IRON_SPADE;
				break;
			case 4:
				mat = Material.GOLD_SPADE;
				break;
			case 5:
				mat = Material.DIAMOND_SPADE;
				break;
			}
			break;
		case AXES:
			switch(getNotificationTier(PP.getSkillLevel(skillType)))
			{
			case 1:
				mat = Material.WOOD_AXE;
				break;
			case 2:
				mat = Material.STONE_AXE;
				break;
			case 3:
				mat = Material.IRON_AXE;
				break;
			case 4:
				mat = Material.GOLD_AXE;
				break;
			case 5:
				mat = Material.DIAMOND_AXE;
				break;
			}
			break;
		default:
			mat = Material.WATCH;
			break;
		}
			sPlayer.sendNotification(ChatColor.GREEN+"Level Up!", ChatColor.YELLOW+m.getCapitalized(skillType.toString())+ChatColor.DARK_AQUA+" ("+ChatColor.GREEN+PP.getSkillLevel(skillType)+ChatColor.DARK_AQUA+")", mat);
			playLevelUpNoise(sPlayer);
	}
	
	public static Integer getNotificationTier(Integer level)
	{
		if(level < 200)
			return 1;
		else if (level >= 200 && level < 400)
			return 2;
		else if (level >= 400 && level < 600)
			return 3;
		else if (level >= 600 && level < 800)
			return 4;
		else
			return 5;
	}
	
	public static Integer getXpInc(int skillxp, int xptolevel)
	{
		double percentage = (double) skillxp/xptolevel;
		double inc = 0.0039370078740157;
		return (int) (percentage/inc);
	}
	
	public static void updateXpBar(Player player)
	{
		PlayerProfile PP = Users.getProfile(player);
		
		if(PP.getLastGained() != null && !PP.getXpBarLocked())
		{
			int num = getXpInc(PP.getSkillXpLevel(PP.getLastGained()), PP.getXpToLevel(PP.getLastGained()));
			
			xpbars.get(player).setUrl(getUrlBar(num)).setDirty(true);
			xpicons.get(player).setUrl(getUrlIcon(PP.getLastGained())).setDirty(true);
			
			SpoutManager.getPlayer(player).getMainScreen().setDirty(true);
		} else if (PP.getXpBarLocked())
		{
			if(xpbars.get(player) != null && xpicons.get(player) != null)
			{
				updateXpBarUrl(PP, player);
			} else 
			{
				initializeXpBarDisplay(SpoutManager.getPlayer(player));
				updateXpBarUrl(PP, player);
			}
		}
	}
	public static void updateXpBarUrl(PlayerProfile PP, Player player)
	{
		int num = getXpInc(PP.getSkillXpLevel(PP.getSkillLock()), PP.getXpToLevel(PP.getSkillLock()));
		
		xpbars.get(player).setUrl(getUrlBar(num)).setDirty(true);
		xpicons.get(player).setUrl(getUrlIcon(PP.getSkillLock())).setDirty(true);
		
		SpoutManager.getPlayer(player).getMainScreen().setDirty(true);
	}
	public static void updateXpBarFill(Player player)
	{
		PlayerProfile PP = Users.getProfile(player);
		
		if(PP.getLastGained() != null)
		{
			if(PP.getXpBarInc() < 254)
				xpbars.get(player).setUrl(getUrlBar(PP.getXpBarInc()+1)).setDirty(true);
			else
				xpbars.get(player).setUrl(getUrlBar(0)).setDirty(true);
			
			PP.setXpBarInc(PP.getXpBarInc()+1);
			
			if(LoadProperties.xpicon)
				xpicons.get(player).setUrl(getUrlIcon(PP.getLastGained())).setDirty(true);
			
			((SpoutPlayer)player).getMainScreen().setDirty(true);
		}
	}
	
	public static String getUrlBar(Integer number)
	{
		if(number.toString().toCharArray().length == 1)
		{
			return LoadProperties.web_url+"/HUD/Standard/xpbar_inc00"+number+".png";
		} else if (number.toString().toCharArray().length == 2)
		{
			return LoadProperties.web_url+"/HUD/Standard/xpbar_inc0"+number+".png";
		} else {
			return LoadProperties.web_url+"/HUD/Standard/xpbar_inc"+number+".png";
		}
	}
	public static String getUrlIcon(SkillType skillType)
	{
		return LoadProperties.web_url+"/HUD/Standard/"+m.getCapitalized(skillType.toString())+".png";
	}
	public static boolean shouldBeFilled(PlayerProfile PP)
	{
		return PP.getXpBarInc() < getXpInc(PP.getSkillXpLevel(PP.getLastGained()), PP.getXpToLevel(PP.getLastGained()));
	}
}
