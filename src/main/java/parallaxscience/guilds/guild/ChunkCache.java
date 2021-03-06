package parallaxscience.guilds.guild;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import parallaxscience.guilds.utility.FileUtility;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that is used to store and manage claimed chunk information
 * Holds the master list of claimed chunks
 * @author Tristan Jay
 */
public final class ChunkCache
{
    /**
     * Filepath to the ChunkCache save file location
     */
    private static final String fileName = FileUtility.guildDirectory + "/" + "ChunkCache.dat";

    /**
     * List of all of the claimed chunks and the owning guild
     * Implements a double-HashMap for fast indexing
     * @see HashMap
     */
    private static HashMap<Integer, HashMap<Integer, String>> chunkMap;

    /**
     * Initialize function for the class
     * Attempts to load the chunk data from file
     * If no chunk data is found, create a new HashMap
     */
    @SuppressWarnings("unchecked")
    public static void initialize()
    {
        try
        {
            chunkMap = (HashMap<Integer, HashMap<Integer, String>>) FileUtility.readFromFile(fileName);
        }
        catch(Exception e)
        {
            chunkMap = new HashMap<>();
        }
    }

    /**
     * Returns the name of the owner of a chunk
     * @param x X coordinate for the chunk
     * @param z Z coordinate for the chunk
     * @return String name of the owning guild
     */
    public static String getChunkOwner(int x, int z)
    {
        if(!chunkMap.containsKey(x)) return null;
        return chunkMap.get(x).get(z);
    }

    /**
     * Returns the name of the owner of a block
     * @param blockPos BlockPos of the block
     * @return String name of the owning guild
     */
    public static String getChunkOwner(BlockPos blockPos)
    {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        return getChunkOwner(chunkPos.x, chunkPos.z);
    }

    /**
     * Returns the name of the owner of a chunk
     * @param chunkPos ChunkPos of the chunk
     * @return String name of the owning guild
     */
    public static String getChunkOwner(ChunkPos chunkPos)
    {
        return getChunkOwner(chunkPos.x, chunkPos.z);
    }

    /**
     * Checks to see if the chunk is connected to the rest of the guild's territory
     * @param chunkPos ChunkPos of the chunk
     * @param guild Guild object reference
     * @return true if the chunk is connected to the rest of the guild territory
     */
    public static boolean isConnected(ChunkPos chunkPos, Guild guild)
    {
        if(guild.getTerritoryCount() == 0) return true;

        String guildName = guild.getGuildName();

        int x = chunkPos.x;
        int z = chunkPos.z;
        if(chunkMap.containsKey(x))
        {
            if(guildName.equals(chunkMap.get(x).get(z + 1))) return true;
            else if(guildName.equals(chunkMap.get(x).get(z - 1))) return true;
        }
        if(chunkMap.containsKey(x + 1))
        {
            if(guildName.equals(chunkMap.get(x + 1).get(z))) return true;
        }
        if(chunkMap.containsKey(x - 1))
        {
            return guildName.equals(chunkMap.get(x - 1).get(z));
        }
        return false;
    }

    /**
     * Sets the owner of a chunk
     * @param chunkPos ChunkPos of the chunk
     * @param guildName String name of the guild
     */
    public static void setChunkOwner(ChunkPos chunkPos, String guildName)
    {
        int x = chunkPos.x;
        int z = chunkPos.z;
        if(chunkMap.containsKey(x))
        {
            if(chunkMap.containsKey(z))
            {
                chunkMap.get(x).replace(z, guildName);
            }
            else
            {
                chunkMap.get(x).put(z, guildName);
            }
        }
        else
        {
            HashMap<Integer, String> temp = new HashMap<>();
            temp.put(z, guildName);
            chunkMap.put(x, temp);
        }
    }

    /**
     * Removes a chunk from a guild's territory
     * Removes the chunk from the chunk list
     * @param chunkPos ChunkPos of the chunk
     */
    public static boolean removeChunkOwner(ChunkPos chunkPos)
    {
        int x = chunkPos.x;
        int z = chunkPos.z;

        if(canRemoveChunk(x, z))
        {
            chunkMap.get(x).remove(z);
            return true;
        }
        return false;
    }

    /**
     * Returns whether or not the chunk can be abandoned or not
     * Uses chunk adjacency check algorithm
     * @param x X coordinate for the chunk
     * @param z Z coordinate for the chunk
     * @return if the chunk can be abandoned
     */
    private static boolean canRemoveChunk(int x, int z)
    {
        HashMap<Integer, String> subMap = chunkMap.get(x);
        String owner = subMap.get(z);
        if(owner == null) return false;

        boolean e = owner.equals(chunkMap.get(x + 1).get(z));
        boolean w = owner.equals(chunkMap.get(x - 1).get(z));

        if(owner.equals(subMap.get(z + 1))) {if(owner.equals(subMap.get(z - 1))) if(w) return !e;}
        else if(w) return !e;
        return true;
    }

    /**
     * Remove all of a guild's claimed chunks
     * @param guildName String name of the guild
     */
    static void removeAllClaimed(String guildName)
    {
        for(Map.Entry<Integer, HashMap<Integer, String>> entry : chunkMap.entrySet())
        {
            for(Map.Entry<Integer, String> subEntry : entry.getValue().entrySet())
            {
                if(subEntry.getValue().equals(guildName)) chunkMap.get(entry.getKey()).remove(subEntry.getKey());
            }
        }
    }

    /**
     * Saves the chunk data to file
     */
    public static void save()
    {
        FileUtility.saveToFile(fileName, chunkMap);
    }
}
