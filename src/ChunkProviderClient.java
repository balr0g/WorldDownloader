package net.minecraft.src;

/* WORLD DOWNLOADER ---> */
import java.io.IOException;
/* <--- WORLD DOWNLOADER */
import java.util.*;

public class ChunkProviderClient
    implements IChunkProvider
{
    private Chunk blankChunk;
    private LongHashMap chunkMapping;
    private List field_889_c;
    private World worldObj;

    public ChunkProviderClient(World world)
    {
        chunkMapping = new LongHashMap();
        field_889_c = new ArrayList();
        blankChunk = new EmptyChunk(world, new byte[256 * world.worldHeight], 0, 0);
        worldObj = world;
    }

    public boolean chunkExists(int i, int j)
    {
        if (this != null)
        {
            return true;
        }
        else
        {
            return chunkMapping.containsItem(ChunkCoordIntPair.chunkXZ2Int(i, j));
        }
    }

    public void func_539_c(int i, int j)
    {
        Chunk chunk = provideChunk(i, j);
        if (!chunk.isEmpty())
        {
            chunk.onChunkUnload();
        }
        /* WORLD DOWNLOADER ---> */
        if(WorldDL.downloading == true && chunk.neverSave == false && chunk.isFilled == true )
        {
        	saveChunk(chunk);
				try {
					((WorldClient)worldObj).myChunkLoader.saveExtraChunkData(worldObj, chunk);
				} catch (IOException e) {
					e.printStackTrace();
				}
        }
        /* <--- WORLD DOWNLOADER */
        chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(i, j));
        field_889_c.remove(chunk);
    }

    public Chunk loadChunk(int i, int j)
    {
        byte abyte0[] = new byte[256 * worldObj.worldHeight];
        Chunk chunk = new Chunk(worldObj, abyte0, i, j);
        Arrays.fill(chunk.skylightMap.data, (byte) - 1);
        chunkMapping.add(ChunkCoordIntPair.chunkXZ2Int(i, j), chunk);
        chunk.isChunkLoaded = true;
        /* WORLD DOWNLOADER ---> */
        if(WorldDL.downloading)
        {
        	chunk.importOldChunkTileEntities();
        }
        /* <--- WORLD DOWNLOADER */
        return chunk;
    }

    public Chunk provideChunk(int i, int j)
    {
        Chunk chunk = (Chunk)chunkMapping.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(i, j));
        if (chunk == null)
        {
            return blankChunk;
        }
        else
        {
            return chunk;
        }
    }

    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate)
    {
    	/* WORLD DOWNLOADER ---> */
    	if(WorldDL.downloading == false)
    		return true;
    	
    	for(LongHashMapEntry lhme : chunkMapping.getEntries())
    	{
    		while( lhme != null )
    		{
    			Chunk c = (Chunk)lhme.value;
    			
                if( flag && c != null && !c.neverSave && c.isFilled )
                {
                	try
                	{
						((WorldClient)worldObj).myChunkLoader.saveExtraChunkData(worldObj, c);
					}
                	catch (IOException e)
                	{
						e.printStackTrace();
					}
                }
                if( c != null && c.neverSave == false && c.isFilled )
                	saveChunk(c);
                
    			lhme = lhme.nextEntry; // Get next Entry in this linked list 
    		}
    	}

        if(flag)
        {
        	((WorldClient)worldObj).myChunkLoader.saveExtraData();
        }
        /* <--- WORLD DOWNLOADER */

        return true;
    }

    /* WORLD DOWNLOADER ---> */
    private void saveChunk(Chunk chunk)
    {
        if(WorldDL.downloading == false)
        	return;
        chunk.lastSaveTime = worldObj.getWorldTime();
        chunk.isTerrainPopulated = true;
        try {
            for( Object ob : chunk.myChunkTileEntityMap.keySet() )
            {
            	TileEntity te = (TileEntity) chunk.myChunkTileEntityMap.get(ob);
            	if(te != null)
            	{
            		Block block = Block.blocksList[worldObj.getBlockId(te.xCoord, te.yCoord, te.zCoord)];
            		if( block instanceof BlockChest || block instanceof BlockDispenser || block instanceof BlockFurnace || block instanceof BlockNote || block instanceof BlockBrewingStand )
            			chunk.chunkTileEntityMap.put(ob, te);
            	}
            }
            ((WorldClient)worldObj).myChunkLoader.saveChunk(worldObj, chunk);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
//    public Chunk loadChunk(int i, int j)
//    {
//    	Chunk ret = null;
//    	try {
//			ret = WorldDL.myChunkLoader.loadChunk(worldObj, i, j);
//		} catch (IOException e) {}
//		return ret;
//    }

    public void importOldTileEntities()
    {
    	for(LongHashMapEntry lhme : chunkMapping.getEntries())
    	{
    		while( lhme != null )
    		{
    			Chunk c = (Chunk)lhme.value;
                
                if( c != null && c.isFilled )
                {
                	c.importOldChunkTileEntities();
                }
    			
    			lhme = lhme.nextEntry; // Get next Entry in this linked list 
    		}
    	}
    }
    /* <--- WORLD DOWNLOADER */

    public boolean unload100OldestChunks()
    {
        return false;
    }

    public boolean canSave()
    {
        return false;
    }

    public void populate(IChunkProvider ichunkprovider, int i, int j)
    {
    }

    public String makeString()
    {
        return (new StringBuilder()).append("MultiplayerChunkCache: ").append(chunkMapping.getNumHashElements()).toString();
    }

    public List getPossibleCreatures(EnumCreatureType enumcreaturetype, int i, int j, int k)
    {
        return null;
    }

    public ChunkPosition findClosestStructure(World world, String s, int i, int j, int k)
    {
        return null;
    }
}
