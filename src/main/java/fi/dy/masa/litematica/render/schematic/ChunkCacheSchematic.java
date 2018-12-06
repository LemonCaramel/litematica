package fi.dy.masa.litematica.render.schematic;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap.Type;

public class ChunkCacheSchematic implements IWorldReader
{
    private static final IBlockState AIR = Blocks.AIR.getDefaultState();

    protected int chunkStartX;
    protected int chunkStartZ;
    protected Chunk[][] chunkArray;
    protected boolean empty;
    protected World world;

    public ChunkCacheSchematic(World worldIn, BlockPos pos, int expand)
    {
        this.world = worldIn;
        this.chunkStartX = (pos.getX() - expand) >> 4;
        this.chunkStartZ = (pos.getZ() - expand) >> 4;
        int chunkEndX = (pos.getX() + expand + 15) >> 4;
        int chunkEndZ = (pos.getZ() + expand + 15) >> 4;
        this.chunkArray = new Chunk[chunkEndX - this.chunkStartX + 1][chunkEndZ - this.chunkStartZ + 1];
        this.empty = true;

        for (int cx = this.chunkStartX; cx <= chunkEndX; ++cx)
        {
            for (int cz = this.chunkStartZ; cz <= chunkEndZ; ++cz)
            {
                this.chunkArray[cx - this.chunkStartX][cz - this.chunkStartZ] = worldIn.getChunk(cx, cz);
            }
        }

        for (int cx = pos.getX() >> 4; cx <= (pos.getX() + 15) >> 4; ++cx)
        {
            for (int cz = pos.getZ() >> 4; cz <= (pos.getZ() + 15) >> 4; ++cz)
            {
                Chunk chunk = this.chunkArray[cx - this.chunkStartX][cz - this.chunkStartZ];

                if (chunk != null && chunk.isEmptyBetween(pos.getY(), pos.getY() + 15) == false)
                {
                    this.empty = false;
                    break;
                }
            }
        }
    }

    public boolean isEmpty()
    {
        return this.empty;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos)
    {
        if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int cx = (pos.getX() >> 4) - this.chunkStartX;
            int cz = (pos.getZ() >> 4) - this.chunkStartZ;

            if (cx >= 0 && cx < this.chunkArray.length &&
                cz >= 0 && cz < this.chunkArray[cx].length)
            {
                Chunk chunk = this.chunkArray[cx][cz];

                if (chunk != null)
                {
                    return chunk.getBlockState(pos);
                }
            }
        }

        return AIR;
    }

    @Override
    public Biome getBiome(BlockPos pos)
    {
        int cx = (pos.getX() >> 4) - this.chunkStartX;
        int cz = (pos.getZ() >> 4) - this.chunkStartZ;

        return this.chunkArray[cx][cz].getBiome(pos);
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos pos)
    {
        return this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
    }

    @Nullable
    public TileEntity getTileEntity(BlockPos pos, Chunk.EnumCreateEntityType type)
    {
        int i = (pos.getX() >> 4) - this.chunkStartX;
        int j = (pos.getZ() >> 4) - this.chunkStartZ;
        return this.chunkArray[i][j].getTileEntity(pos, type);
    }

    @Override
    public boolean isAirBlock(BlockPos pos)
    {
        return this.getBlockState(pos).isAir();
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        int sky = 15;
        int block = 0;

        if (block < lightValue)
        {
            block = lightValue;
        }

        return sky << 20 | block << 4;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        return this.getBlockState(pos).getStrongPower(this, pos, direction);
    }

    @Override
    public int getLightFor(EnumLightType type, BlockPos pos)
    {
        return 15;
    }

    @Override
    public int getLightSubtracted(BlockPos pos, int amount)
    {
        return 15;
    }

    @Override
    public boolean isChunkLoaded(int cx, int cz, boolean allowEmpty)
    {
        int x = cx - this.chunkStartX;
        int z = cz - this.chunkStartZ;
        return x >= 0 && x < this.chunkArray.length && z >= 0 && z < this.chunkArray[x].length;
    }

    @Override
    public boolean canSeeSky(BlockPos pos)
    {
        return false;
    }

    @Override
    public int getHeight(Type heightmapType, int x, int z)
    {
        return 0;
    }

    @Override
    public EntityPlayer getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate)
    {
        return null;
    }

    @Override
    public int getSkylightSubtracted()
    {
        return 0;
    }

    @Override
    public WorldBorder getWorldBorder()
    {
        return this.world.getWorldBorder();
    }

    @Override
    public boolean checkNoEntityCollision(Entity entityIn, VoxelShape shape)
    {
        return false;
    }

    @Override
    public boolean isRemote()
    {
        return false;
    }

    @Override
    public int getSeaLevel()
    {
        return 0;
    }

    @Override
    public Dimension getDimension()
    {
        return this.world.getDimension();
    }

    @Override
    public IFluidState getFluidState(BlockPos pos)
    {
        // TODO change when fluids become separate
        return this.getBlockState(pos).getFluidState();
    }
}
