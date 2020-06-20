package com.sxtanna.mc.arcade.game

import com.sxtanna.mc.arcade.base.Addon
import com.sxtanna.mc.arcade.base.Named
import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.data.base.Vec3
import com.sxtanna.mc.arcade.func.AtomicCounter
import com.sxtanna.mc.arcade.func.WeakHashSet
import com.sxtanna.mc.arcade.func.map
import com.sxtanna.mc.arcade.func.resetPlayer
import com.sxtanna.mc.arcade.game.area.GameArea
import com.sxtanna.mc.arcade.game.base.GameInfo
import com.sxtanna.mc.arcade.game.base.GamePlace
import com.sxtanna.mc.arcade.game.base.GameState
import com.sxtanna.mc.arcade.game.base.GameUser
import com.sxtanna.mc.arcade.game.data.GameData
import com.sxtanna.mc.arcade.game.data.GameDatas
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.team.GameTeam
import com.sxtanna.mc.arcade.game.team.TeamState
import com.sxtanna.mc.arcade.game.wait.area.Selector
import com.sxtanna.mc.arcade.game.wait.area.SelectorMode
import com.sxtanna.mc.arcade.game.wins.Winner
import com.sxtanna.mc.arcade.game.wins.WinnerCheck
import com.sxtanna.mc.arcade.hook.ArcadeGamePlugin
import com.sxtanna.mc.arcade.hook.mods.damage.base.DiedEvent
import com.sxtanna.mc.arcade.hook.mods.damage.base.HurtEvent
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level

abstract class Game protected constructor() : Named, State, Addon, GameUser, Listener
{
	
	abstract override val plugin: ArcadeGamePlugin
	
	
	// base
	val info: GameInfo
		get() = plugin.info
	
	final override val name: String
		get() = info.name
	final override val game: Game
		get() = this
	
	// opts
	private val opts = mutableMapOf<GameData<*>, Any?>()
	
	
	// state
	private var state = GameState.AWAIT
	
	// teams
	private val teams = mutableSetOf<GameTeam>()
	private val teamPicks = mutableMapOf<UUID, GameTeam>()
	
	// gears
	private val gears = mutableSetOf<GameGear>()
	private val gearPicks = mutableMapOf<UUID, GameGear>()
	
	// users
	private val users = WeakHashSet<Player>()
	
	// areas
	private var using = AtomicReference<GameArea>()
	private val areas by lazy { plugin.area() }
	
	
	private var header = null as? BossBar?
	
	// select
	private var select = null as? Selector?
	
	// winner
	private var winner = null as? Winner?
	
	
	override fun load()
	{
		areas.load()
		
		plugin.server.pluginManager.registerEvents(this, plugin)
		
		areas.random(::awaitArea)
		
		if (get(GameDatas.OPTION_GAME_WIN_CHECK) == null)
		{
			
			val check = if (teams().size > 1)
			{
				WinnerCheck.Team
			}
			else
			{
				WinnerCheck.User
			}
			
			set(GameDatas.OPTION_GAME_WIN_CHECK, check)
		}
	}
	
	override fun kill()
	{
		if (users.isEmpty())
		{
			using.get()?.let(::clearArea)
			areas.kill()
		}
		else
		{
			val done = AtomicCounter(users.size)
			{
				using.get()?.let(::clearArea)
				areas.kill()
			}
			
			users.forEach()
			{ user ->
				gear(user)?.take(user)
				
				user.resetPlayer()
				
				server.onlinePlayers.forEach()
				{ other ->
					user.showPlayer(plugin, other)
					other.showPlayer(plugin, user)
				}
				
				move(user, GamePlace.LOBBY)
				{
					done.count()
				}
			}
		}
		
		state = GameState.AWAIT
		
		teams.forEach(GameTeam::kill)
		gears.forEach(GameGear::kill)
		
		users.clear()
		
		teamPicks.clear()
		gearPicks.clear()
		
		select?.kill()
		select = null
		winner = null
		
		HandlerList.unregisterAll(this)
	}
	
	
	operator fun <T> get(data: GameData<T>): T
	{
		return this.opts[data]?.let(data::get) ?: data.def()
	}
	
	operator fun <T> set(data: GameData<T>, value: T)
	{
		this.opts[data] = value
	}
	
	
	// game state
	fun state(): GameState
	{
		return this.state
	}
	
	fun state(state: GameState, cause: GameState.SetCause = GameState.SetCause.NORMAL)
	{
		
		this.state = state
		
		if (cause is GameState.SetCause.FAILURE)
		{
			return plugin.logger.log(Level.WARNING, "state failure: ${cause.message}")
		}
		
		select?.kill()
		select = null
		
		when (state)
		{
			// move them to the lobby?
			GameState.LOBBY ->
			{
				if (users.isEmpty())
				{
					users += server.onlinePlayers
				}
				
				users.forEach { move(it, GamePlace.LOBBY) }
				
				gears.forEach(GameGear::load)
				teams.forEach(GameTeam::load)
				
				select = Selector(plugin, this, SelectorMode.TEAM)
				select?.load()
			}
			// start countdown
			GameState.READY ->
			{
				joinPicks()
				countdown()
				
				select = Selector(plugin, this, SelectorMode.GEAR)
				select?.load()
			}
			// move them to the world
			GameState.START ->
			{
				if (users.isEmpty())
				{
					return state(GameState.GOING)
				}
				
				val wait = AtomicCounter(users.size)
				{
					giveGears()
					
					GameData.values().forEach()
					{ option ->
						if (option !is GameData.UserOpt)
						{
							return@forEach
						}
						
						val value = opts[option] ?: option.def() ?: return@forEach
						users.forEach()
						{ user ->
							option.apply(value, user)
						}
					}
					
					state(GameState.GOING)
				}
				
				users.forEach()
				{ user ->
					move(user, GamePlace.SPAWN)
					{
						wait.count()
					}
				}
			}
			// move them to the lobby?
			GameState.ENDED ->
			{
				announceWinner()
				
				if (users.isEmpty())
				{
					return state(GameState.RESET)
				}
				
				
				fun beginReset()
				{
					val wait = AtomicCounter(users.size)
					{
						state(GameState.RESET)
					}
					
					users.forEach()
					{ user ->
						
						gear(user)?.take(user)
						
						user.resetPlayer()
						
						move(user, GamePlace.LOBBY)
						{
							wait.count()
							
							server.onlinePlayers.forEach()
							{ other ->
								user.showPlayer(plugin, other)
								other.showPlayer(plugin, user)
							}
						}
					}
				}
				
				
				if (get(GameDatas.OPTION_GAME_ENDS_INSTANTLY))
				{
					beginReset()
				}
				else
				{
					later(20 * 4)
					{
						beginReset()
					}
				}
			}
			GameState.RESET ->
			{
				users.clear()
				
				gears.forEach(GameGear::kill)
				teams.forEach(GameTeam::kill)
				
				teamPicks.clear()
				gearPicks.clear()
				
				winner = null
				
				clearArea(using.get() ?: return state(GameState.LOBBY))
				
				later(10)
				{
					areas.random(::awaitArea)
				}
			}
			else            ->
			{
			
			}
		}
	}
	
	
	// team state
	fun state(user: Player): TeamState
	{
		return team(user)?.get(user) ?: TeamState.NONE
	}
	
	fun users(): List<Player>
	{
		return users.toList()
	}
	
	fun users(state: TeamState): List<Player>
	{
		return teams.flatMap { it[state] }
	}
	
	
	// game join and quit
	fun join(user: Player)
	{
		if (state >= GameState.START)
		{
			return spec(user) // game has already started
		}
		
		users += user
		
		// game is counting down, put them on a team before it starts
		if (state == GameState.READY)
		{
			join(user, bestTeam())
		}
	}
	
	fun quit(user: Player)
	{
		users -= user
		
		team(user)?.quit(user, false)
		
		header?.removePlayer(user)
		
		if (state().active())
		{
			later(1)
			{
				winner = checkWinCondition()
				
				if (winner != null)
				{
					state(GameState.ENDED)
				}
			}
		}
		
		if (state() == GameState.READY)
		{
			// cancel countdown if there isn't enough players
		}
	}
	
	fun spec(user: Player)
	{
		user.resetPlayer()
		user.isCollidable = false
		
		user.gameMode = GameMode.ADVENTURE
		
		user.allowFlight = true
		user.isFlying = true
		
		val live = users(TeamState.LIVE)
		val dead = users(TeamState.DEAD)
		
		live.forEach()
		{ other ->
			other.hidePlayer(plugin, user)
		}
		
		dead.forEach()
		{ other ->
			user.showPlayer(plugin, other)
		}
		
		team(user)?.set(user, TeamState.DEAD)
		
		move(user, GamePlace.WORLD)
	}
	
	
	// team join
	fun join(user: Player, team: GameTeam)
	{
		val prev = team(user)
		
		if (prev != null && prev != team)
		{
			prev.quit(user, true)
		}
		
		team.join(user, true)
	}
	
	// user movement
	fun move(user: Player, place: GamePlace, done: (Player) -> Unit = { })
	{
		when (place)
		{
			GamePlace.LOBBY ->
			{
				plugin.arcade.waitingArea.move(user, done)
			}
			GamePlace.WORLD ->
			{
				val area = area() ?: return done.invoke(user)
				val wrld = area.world ?: return done.invoke(user)
				
				plugin.arcade.plugin.worlds.move(area.spawn.toBukkit().toLocation(wrld), listOf(user), done)
			}
			GamePlace.SPAWN ->
			{
				val area = area() ?: return done.invoke(user)
				val wrld = area.world ?: return done.invoke(user)
				
				val team = team(user)
				if (team == null || team[user] == TeamState.DEAD)
				{
					return move(user, GamePlace.WORLD, done)
				}
				
				val pos = area.point[teams.indexOf(team)]?.minBy { it.uses }
				if (pos == null)
				{
					return move(user, GamePlace.WORLD, done)
				}
				
				pos.uses++
				
				val loc = pos.toBukkit(wrld)
				
				if (pos.dir == Vec3.ZERO)
				{
					val vec = area.spawn.toBukkit().subtract(pos.pos.toBukkit()).normalize()
					vec.y = 0.0
					
					loc.direction = vec
				}
				
				plugin.arcade.plugin.worlds.move(loc, listOf(user), done)
			}
		}
	}
	
	// team and gear choosing
	fun pick(user: Player, team: GameTeam): Boolean
	{
		val curr = team(user)
		val prev = teamPicks[user.uniqueId]
		
		if (curr == team)
		{
			// they are already on this team
			return false
		}
		
		if (prev == team)
		{
			// they have already picked this team
			return false
		}
		
		teamPicks[user.uniqueId] = team
		
		return true
	}
	
	fun pick(user: Player, gear: GameGear): Boolean
	{
		val curr = gear(user)
		val prev = gearPick(user)
		
		if (curr == gear)
		{
			// they are already using this gear
			return false
		}
		
		if (prev == gear)
		{
			// they have already picked this team
			return false
		}
		
		gearPicks[user.uniqueId] = gear
		
		return true
	}
	
	
	// actual team and gear choice
	fun team(user: Player): GameTeam?
	{
		return teams.find { user in it }
	}
	
	fun teamPick(user: Player): GameTeam?
	{
		return teamPicks[user.uniqueId]
	}
	
	
	fun gear(user: Player): GameGear?
	{
		return gears.find { it.using(user) }
	}
	
	fun gearPick(user: Player): GameGear?
	{
		return gearPicks[user.uniqueId]
	}
	
	
	// teams and gears
	fun teams(): List<GameTeam>
	{
		return this.teams.toList()
	}
	
	fun gears(): List<GameGear>
	{
		return this.gears.toList()
	}
	
	
	fun bestTeam(): GameTeam
	{
		var best = teams.first()
		
		teams.forEach()
		{ next ->
			if (next.size() < best.size())
			{
				best = next
			}
		}
		
		return best
	}
	
	fun teamSize(): Map<GameTeam, Int>
	{
		return teams().associateWith { it.size() }
	}
	
	fun sameTeam(user0: Player, user1: Player): Boolean
	{
		return team(user0) == team(user1)
	}
	
	
	fun area(): GameArea?
	{
		return using.get()
	}
	
	fun reduceCountdown(amount: Int = 1)
	{
		set(GameDatas.COUNTDOWN_VALUE, get(GameDatas.COUNTDOWN_VALUE).toInt() - amount)
	}
	
	
	fun announceWinner(winner: Winner? = this.winner)
	{
		val name = when (winner)
		{
			null           ->
			{
				"No One"
			}
			is Winner.User ->
			{
				winner.user.name
			}
			is Winner.Team ->
			{
				winner.team.external
			}
		}
		
		users.forEach()
		{ user ->
			user.sendTitle("${Colour.Red}Game Over", "${Colour.Gray}$name won the game", 10, 60, 20)
		}
	}
	
	open fun checkWinCondition(): Winner?
	{
		val check = get(GameDatas.OPTION_GAME_WIN_CHECK) ?: return null
		return with(check) { check() }
	}
	
	
	// areas
	private fun awaitArea(area: GameArea?)
	{
		if (area == null)
		{
			return state(GameState.AWAIT, GameState.SetCause.FAILURE("no areas"))
		}
		
		state(GameState.LOBBY)
		
		areas.loadWorld(this, area)
		{
			area.world = it ?: return@loadWorld
			
			GameData.values().forEach()
			{ option ->
				if (option !is GameData.AreaOpt)
				{
					return@forEach
				}
				
				val value = opts[option] ?: option.def() ?: return@forEach
				option.apply(value, it)
			}
			
			val cent = area.bound.midpoint()
			val size = get(GameDatas.OPTION_AREA_LOAD_SIZE).toInt()
			
			plugin.arcade.plugin.worlds.load(it, cent, size,
				// status
				                             {
				                                 users.forEach()
				                                 { user ->
					                                 user.sendActionBar(it.statusBar())
				                                 }
			                                 },
				// complete
				                             {
				                                 using.set(area)
				
				                                 users.forEach()
				                                 { user ->
					                                 user.sendActionBar("${Colour.Green}Area Ready!")
				                                 }
			                                 })
		}
	}
	
	private fun clearArea(area: GameArea)
	{
		areas.killWorld(this, area)
	}
	
	// teams
	private fun joinPicks()
	{
		// put players on the teams they chose in the lobby
		users.forEach()
		{ user ->
			join(user, teamPick(user) ?: bestTeam())
		}
		
		// if team balancing isn't enabled, return
		if (users.size < 2 || !get(GameDatas.OPTION_FORCE_TEAM_BALANCE))
		{
			return
		}
		
		// safe guard against an infinite loop
		var done = users.size
		// track users that were moved
		val move = mutableSetOf<Player>()
		
		// team sizes
		var size = teamSize()
		// team with least players
		var minT = size.minBy { it.value }?.key
		// team with most players
		var maxT = size.maxBy { it.value }?.key
		
		// team sizes aren't all the same, we haven't tried enough, and min and max teams are valid
		while (size.values.distinct().count() > 1 && done-- > 0 && minT != null && maxT != null)
		{
			val user = maxT[TeamState.NONE].firstOrNull() ?: break
			
			// move player from max team to min team
			maxT.quit(user, false)
			minT.join(user, false)
			
			move += user
			
			// reset values, and loop again
			size = teamSize()
			minT = size.minBy { it.value }?.key
			maxT = size.maxBy { it.value }?.key
		}
		
		// inform moved players
		move.forEach()
		{ user ->
			val new = team(user)
			val old = teamPick(user)
			
			if (new == null || new == old)
			{
				return@forEach
			}
			
			user.sendMessage("${Colour.Gray}You were moved to ${new.external}${Colour.Gray}!")
		}
		
		move.clear()
	}
	
	private fun giveGears()
	{
		users.forEach()
		{ user ->
			(gearPick(user) ?: gears.first()).give(user)
		}
	}
	
	private fun countdown()
	{
		set(GameDatas.COUNTDOWN_VALUE, get(GameDatas.COUNTDOWN_START))
		
		val bar = server.createBossBar("Game Starting...", BarColor.WHITE, BarStyle.SEGMENTED_10, BarFlag.CREATE_FOG)
		
		users.forEach(bar::addPlayer)
		
		timerAsync(20)
		{
			val time = get(GameDatas.COUNTDOWN_VALUE).toInt()
			
			if (state() != GameState.READY)
			{
				bar.removeAll()
				return@timerAsync it.cancel()
			}
			
			set(GameDatas.COUNTDOWN_VALUE, time - 1)
			
			if (time > 0)
			{
				val max = get(GameDatas.COUNTDOWN_START).toDouble()
				val map = map(time, 0, max, 0, 10)
				
				bar.progress = map / 10.0
				
				when
				{
					time <= 1            ->
					{
						bar.color = BarColor.RED
					}
					time <= (max * 0.10) ->
					{
						bar.color = BarColor.YELLOW
					}
					time <= (max * 0.25) ->
					{
						bar.color = BarColor.GREEN
					}
				}
				
				if (time <= 10)
				{
					users.forEach()
					{ user ->
						user.playSound(user.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.AMBIENT, 1.0F, if (time <= 3) 1.8F else 1.0F)
					}
				}
				
				return@timerAsync
			}
			
			it.cancel()
			bar.removeAll()
			
			later(10)
			{
				state(GameState.START)
			}
		}
	}
	
	
	protected operator fun GameTeam.unaryPlus()
	{
		teams += this
	}
	
	protected operator fun GameGear.unaryPlus()
	{
		gears += this
	}
	
	
	// events
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	fun BlockPlaceEvent.onPlace()
	{
		if (state.active() && state(player) != TeamState.LIVE || !get(GameDatas.OPTION_CAN_PLACE))
		{
			isCancelled = true
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	fun BlockBreakEvent.onBreak()
	{
		if (state.active() && state(player) != TeamState.LIVE || !get(GameDatas.OPTION_CAN_BREAK))
		{
			isCancelled = true
		}
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun HurtEvent.onHurt()
	{
		if (!state.active() || damaged.world != using.get()?.world)
		{
			return
		}
		
		if (get(GameDatas.OPTION_NO_DAMAGE) || cause in get(GameDatas.OPTION_NO_DAMAGE_FROM))
		{
			isCancelled = true
		}
		
		val damager = damager as? Player ?: return
		
		if (state(damager) != TeamState.LIVE)
		{
			isCancelled = true
		}
		
		val damaged = damaged as? Player ?: return
		
		if (sameTeam(damaged, damager) && !get(GameDatas.OPTION_USER_FRIENDLY_FIRE))
		{
			isCancelled = true
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun DiedEvent.onDied()
	{
		if (!state.active() || hurtEvent.damaged.world != using.get()?.world)
		{
			return
		}
		
		val user = hurtEvent.damaged as? Player ?: return
		
		if (state(user) != TeamState.LIVE)
		{
			return
		}
		
		if (!get(GameDatas.OPTION_CAN_DROP))
		{
			shouldDropItems = false
		}
		
		respawnLocation = using.get()?.world?.spawnLocation
		
		if (get(GameDatas.OPTION_USER_DEATH_CAUSES_SPEC))
		{
			spec(user)
		}
		
		// don't interrupt the death process
		later(1)
		{
			winner = checkWinCondition()
			
			if (winner != null)
			{
				state(GameState.ENDED)
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun FoodLevelChangeEvent.onHunger()
	{
		if (state.active() && get(GameDatas.OPTION_NO_HUNGER))
		{
			isCancelled = true
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun CreatureSpawnEvent.onSpawn()
	{
		if (!state.active() || location.world != using.get()?.world || get(GameDatas.OPTION_AREA_ALLOW_SPAWNING) || spawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM)
		{
			return
		}
		
		isCancelled = true
	}
	
}