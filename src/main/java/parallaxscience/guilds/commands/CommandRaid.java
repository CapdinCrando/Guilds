package parallaxscience.guilds.commands;

import com.sun.istack.internal.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import parallaxscience.guilds.config.RaidConfig;
import parallaxscience.guilds.guild.Guild;
import parallaxscience.guilds.guild.GuildCache;
import parallaxscience.guilds.raid.Raid;
import parallaxscience.guilds.raid.RaidCache;
import scala.actors.threadpool.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandRaid extends CommandBase {

    /**
     * String array of sub-commands uses by the auto tab-completion
     */
    private static final String[] commands = new String[]{
            //For all in a guild:
            "help",
            //Not in raid:
            "join",
            //Part of attackers:
            "leave",
            "start"
    };

    /**
     * Gets the name of the command
     */
    @Override
    @Nonnull
    public String getName() {
        return "raid";
    }


    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public String getUsage(ICommandSender sender) {
        return "/raid <action> [arguments]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(final MinecraftServer server, final ICommandSender sender) {
        return sender instanceof EntityPlayerMP;
    }

    @Override
    @Nonnull
    @SuppressWarnings({"unchecked", "SwitchStatementWithTooFewBranches"})
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if(args.length == 1) return getLastMatchingStrings(args, Arrays.asList(commands));
        else if(args.length == 2)
        {
            Entity entity = sender.getCommandSenderEntity();
            if(entity == null) return new ArrayList<>();
            UUID player = entity.getUniqueID();
            Guild guild = GuildCache.getPlayerGuild(player);

            switch(args[0])
            {
                case "join":
                    if(guild != null) getLastMatchingStrings(args, GuildCache.getGuildList());
                    break;
            }
        }
        return new ArrayList<>();
    }

    private List<String> getLastMatchingStrings(String[] args, List<String> list)
    {
        List<String> matching = new ArrayList<>();
        String string = args[args.length - 1];
        int length = string.length();
        for(String item : list)
        {
            if(string.equals(item.substring(0, length))) matching.add(item);
        }
        return matching;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if(args.length == 0)
        {
            sender.sendMessage(new TextComponentString("Type \"/raid help\" for help"));
        }
        else
        {
            Entity entity = sender.getCommandSenderEntity();
            if(entity == null) return;
            UUID player = entity.getUniqueID();
            Guild guild = GuildCache.getPlayerGuild(player);
            Raid raid = RaidCache.getPlayerRaid(player);

            switch (args[0].toLowerCase())
            {
                case "help": displayHelp(sender, guild, raid);
                    break;
                case "join":
                {
                    joinRaid(sender, player, guild, raid, args[1]);
                } break;
                case "leave":
                {
                    leaveRaid(sender, player, guild, raid);
                } break;
                case "start":
                {
                    startRaid(sender, raid);
                } break;

                default: sender.sendMessage(new TextComponentString("Invalid command! Type /raid help for valid commands!"));
                    break;
            }
        }
    }

    private void displayHelp(ICommandSender sender, Guild guild, Raid raid)
    {
        if(guild == null) sender.sendMessage(new TextComponentString("Only those who are in a guild can use raid commands!"));
        else
        {
            sender.sendMessage(new TextComponentString("/raid help - Displays raid commands."));
            if(raid == null) sender.sendMessage(new TextComponentString("/raid join <guild> - Join a raid on a guild"));
            else if(!raid.isStarted())
            {
                sender.sendMessage(new TextComponentString("/raid start - Start the raid."));
                sender.sendMessage(new TextComponentString("/raid leave - Leave the current raiding party."));
            }
        }
    }

    private void joinRaid(ICommandSender sender, UUID player, Guild guild, Raid playerRaid, String newRaidName)
    {
        if(guild == null) sender.sendMessage(new TextComponentString("Only those who are in a guild may join a raid!"));
        else
        {
            if(playerRaid != null) sender.sendMessage(new TextComponentString("You are already part of a raid!"));
            else if(newRaidName.equals(guild.getGuildName())) sender.sendMessage(new TextComponentString("You cannot join a raid on your own guild!"));
            else
            {
                Raid raid = RaidCache.getRaid(newRaidName);
                if(raid == null)
                {
                    sender.sendMessage(new TextComponentString("Successfully joined the raid on " + newRaidName + "!"));
                    RaidCache.createRaid(newRaidName, player);
                }
                else if(raid.isActive()) sender.sendMessage(new TextComponentString("The raid on " + newRaidName + " has already begun!"));
                else
                {
                    String alliance = guild.getAlliance();
                    if(alliance == null) sender.sendMessage(new TextComponentString("Your guild is not a part of an alliance!"));
                    else if(alliance.equals(GuildCache.getGuild(newRaidName).getAlliance()))
                    {
                        if(raid.isStarted())
                        {
                            raid.addDefender(player);
                            sender.sendMessage(new TextComponentString("Successfully joined the raid on " + newRaidName + " as a defender!"));
                        }
                        else sender.sendMessage(new TextComponentString("A raid has not been started for that guild!"));
                    }
                    else
                    {
                        if(raid.canAttackerJoin())
                        {
                            raid.addAttacker(player);
                            sender.sendMessage(new TextComponentString("Successfully joined the raid on " + newRaidName + "!"));
                        }
                        else sender.sendMessage(new TextComponentString("No more attackers can join the raid at the moment!"));
                    }
                }
            }
        }
    }

    private void leaveRaid(ICommandSender sender, UUID player, Guild guild, Raid raid)
    {
        if(guild == null) sender.sendMessage(new TextComponentString("You are not part of a guild!"));
        else if(raid == null) sender.sendMessage(new TextComponentString("You are not currently a part of a raid!"));
        else if(raid.getDefendingGuild().equals(guild.getGuildName())) sender.sendMessage(new TextComponentString("You are not currently a part of a raid!")); //To hide a potential raid
        else if(raid.isStarted()) sender.sendMessage(new TextComponentString("The raid preparation has already begun!"));
        else
        {
            raid.removePlayer(player);
            sender.sendMessage(new TextComponentString("You have successfully left the raid."));
        }
    }

    private void startRaid(ICommandSender sender, Raid raid)
    {
        if(raid == null) sender.sendMessage(new TextComponentString("You are not currently a part of a raid!"));
        else if(raid.isStarted()) sender.sendMessage(new TextComponentString("Raid is already started!"));
        else
        {
            raid.startRaid();
            PlayerList players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            players.sendMessage(new TextComponentString("The raid on " + raid.getDefendingGuild() + " will begin in " + RaidConfig.prepSeconds + " seconds!"));
        }
    }
}
