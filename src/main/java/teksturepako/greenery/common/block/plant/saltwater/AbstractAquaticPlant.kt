package teksturepako.greenery.common.block.plant.saltwater

import git.jbredwards.fluidlogged_api.api.block.BlockWaterloggedPlant
import net.minecraft.block.Block
import net.minecraft.block.IGrowable
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import teksturepako.greenery.Greenery
import teksturepako.greenery.client.ModSoundTypes
import java.util.*

abstract class AbstractAquaticPlant(name: String) : BlockWaterloggedPlant(Material.PLANTS), IGrowable {
    companion object {
        val ALLOWED_SOILS = setOf<Material>(
            Material.GROUND, Material.SAND, Material.GRASS, Material.CLAY, Material.ROCK
        )
    }

    lateinit var itemBlock: Item

    init {
        setRegistryName(name)
        translationKey = name
        soundType = ModSoundTypes.SEAWEED
        creativeTab = Greenery.creativeTab
    }

    fun createItemBlock(): Item {
        itemBlock = ItemBlock(this).setRegistryName(registryName).setTranslationKey(translationKey)
        return itemBlock
    }

    @SideOnly(Side.CLIENT)
    fun registerItemModel() {
        Greenery.proxy.registerItemBlockRenderer(itemBlock, 0, registryName.toString())
    }

    override fun getCollisionBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos): AxisAlignedBB? {
        return NULL_AABB
    }

    //Rendering
    @SideOnly(Side.CLIENT)
    override fun getRenderLayer(): BlockRenderLayer {
        return BlockRenderLayer.CUTOUT
    }

    override fun isFullCube(state: IBlockState): Boolean {
        return false
    }

    override fun isOpaqueCube(state: IBlockState): Boolean {
        return false
    }

    override fun onEntityCollision(worldIn: World?, pos: BlockPos?, state: IBlockState?, entityIn: Entity) {
        entityIn.motionX = entityIn.motionX / 1.1
        entityIn.motionY = entityIn.motionY / 1.1
        entityIn.motionZ = entityIn.motionZ / 1.1
    }

    //Block behavior
    abstract override fun canBlockStay(worldIn: World, pos: BlockPos, state: IBlockState): Boolean

    override fun isReplaceable(world: IBlockAccess, pos: BlockPos): Boolean {
        return false
    }

    override fun canPlaceBlockOnSide(worldIn: World, pos: BlockPos, side: EnumFacing): Boolean {
        return canBlockStay(worldIn, pos, defaultState)
    }

    override fun canPlaceBlockAt(worldIn: World, pos: BlockPos): Boolean {
        return canBlockStay(worldIn, pos, defaultState)
    }

    override fun neighborChanged(state: IBlockState, worldIn: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos) {
        checkAndDropBlock(worldIn, pos, state)
    }

    // IGrowable implementation
    override fun canUseBonemeal(worldIn: World, rand: Random, pos: BlockPos, state: IBlockState): Boolean {
        return canGrow(worldIn, pos, state, false)
    }
}