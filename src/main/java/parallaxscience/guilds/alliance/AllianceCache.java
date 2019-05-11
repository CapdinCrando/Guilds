package parallaxscience.guilds.alliance;

import java.io.*;
import java.util.HashMap;

public class AllianceCache {

    private final static String fileName = "world/Guilds_AllianceCache.dat";

    private static HashMap<String, Alliance> alliances;

    public static void initialize()
    {
        try
        {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            alliances = (HashMap<String, Alliance>) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        }
        catch(Exception e)
        {
            alliances = new HashMap<>();
        }
    }

    public static void createAlliance(String alliance, String guildName)
    {
        alliances.put(alliance, new Alliance(guildName));
    }

    public static void removeAlliance(String alliance)
    {
        alliances.remove(alliance);
    }

    public static Alliance getAlliance(String allianceName)
    {
        return alliances.get(allianceName);
    }

    public static void save()
    {

        File file = new File(fileName);
        if(!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch(Exception e)
            {

            }
        }

        try
        {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(alliances);
            objectOutputStream.close();
            fileOutputStream.close();
        }
        catch(Exception e)
        {

        }
    }
}