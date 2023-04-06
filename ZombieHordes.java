package net.fwoup.zombiehordes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class ZombieHordes extends JavaPlugin implements CommandExecutor, Listener {
    private Random random = new Random();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents( this, this);
        getCommand("zh").setExecutor(this);
        scheduleNextSpawn();
    }

    private void scheduleNextSpawn() {
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
                if (players.length > 0) {
                    Player player = players[random.nextInt(players.length)];
                    World world = player.getWorld();
                    if (world.getTime() >= 13000 && world.getTime() <= 23000) {
                        spawnZombies(player);
                    }
                }
                scheduleNextSpawn();
            }
        }, (15 * 60 + random.nextInt(15 * 60)) * 20);
    }

    private void spawnZombies(Player player) {
        //ORIGINAL CODE (SPAWNS AT EXACT Y-VALUE AS PLAYER)
//        --------------------------------------------------------------------------------------------
//        World world = player.getWorld();
//        double angle = random.nextDouble() * 2 * Math.PI;
//        double distance = 30 + random.nextDouble() * 10;
//        double x = player.getLocation().getX() + distance * Math.cos(angle);
//        double z = player.getLocation().getZ() + distance * Math.sin(angle);
//        double y = player.getLocation().getY();
//        Location spawnLocation = new Location(world, x, y, z);
//        world.strikeLightningEffect(spawnLocation);
//        int numZombies = 5 + random.nextInt(4);
//        for (int i = 0; i < numZombies; i++) {
//            world.spawnEntity(spawnLocation, EntityType.ZOMBIE);
//        }
//    }
//        ---------------------------------------------------------------------------------------------
        //Revised code, tries to spawn mob on ground, gives up if the player is too high.
        World world = player.getWorld();
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = 15 + random.nextDouble() * 20;
        double x = player.getLocation().getX() + distance * Math.cos(angle);
        double z = player.getLocation().getZ() + distance * Math.sin(angle);

        // Start at the player's location and scan downward until you find a solid block
        Location spawnLocation = player.getLocation().clone();
        spawnLocation.setX(x);
        spawnLocation.setZ(z);
        while (spawnLocation.getBlock().getType().isAir() || !spawnLocation.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) { //****Adjust to make sure that spawn location cannot be inside of a wall***!!!!!!!!
            spawnLocation.subtract(0, 1, 0);
            // If you've scanned down more than 10 blocks and still haven't found a solid block, give up and spawn the zombies at the original location
            if (spawnLocation.getY() < player.getLocation().getY() - 300) {
                spawnLocation.add(0, 300, 0);
                break;

            }
        }
        while (spawnLocation.getBlock().getType().isOccluding()) {
            spawnLocation.add(0, 1, 0);
            if (spawnLocation.getY() > player.getLocation().getY() + 50) {
                spawnLocation.subtract(0, 50, 0);
                break;
            }
        }
        spawnLocation.add(0, 1, 0); // Move spawnLocation up by 1 block to make up for subtracted 1.

        world.strikeLightningEffect(spawnLocation);
        int numZombies = 10 + random.nextInt(4);
        for (int i = 0; i < numZombies; i++) {
            world.spawnEntity(spawnLocation, EntityType.ZOMBIE);
        }
        player.sendMessage("A horde of zombies spawned nearby...");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("zh") || command.getName().equalsIgnoreCase("zombiehorde") && args.length == 2 && args[0].equalsIgnoreCase("strike")) {
            if (sender.hasPermission("zh.strike")) {
                Player player = getServer().getPlayer(args[1]);
                if (player != null) {
                    spawnZombies(player);
                    sender.sendMessage("Zombies spawned near " + player.getName() + ".");
                } else {
                    sender.sendMessage("Player not found.");
                }
            } else {
                sender.sendMessage("You do not have permission to use this command.");
            }
            return true;
        }
        return false;
    }
}