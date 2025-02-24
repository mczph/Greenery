package teksturepako.greenery

import com.ferreusveritas.dynamictrees.systems.DirtHelper
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.SoundEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import org.apache.logging.log4j.Logger
import teksturepako.greenery.client.ModCreativeTab
import teksturepako.greenery.common.config.Config
import teksturepako.greenery.common.event.EventBonemeal
import teksturepako.greenery.common.event.EventConfigChanged
import teksturepako.greenery.common.event.EventUseHoe
import teksturepako.greenery.common.event.EventWorldGen
import teksturepako.greenery.common.handler.ModFuelHandler
import teksturepako.greenery.common.recipe.ModRecipes
import teksturepako.greenery.common.registry.ModBlocks
import teksturepako.greenery.common.registry.ModItems
import teksturepako.greenery.common.registry.ModSoundEvents
import teksturepako.greenery.common.util.ConfigUtil.parseValidBiomeTypes
import teksturepako.greenery.common.util.WorldGenUtil.removeBOPGenerators
import teksturepako.greenery.common.world.IGreeneryWorldGenerator
import teksturepako.greenery.common.world.WorldGenHook
import teksturepako.greenery.common.world.crop.WorldGenArrowhead
import teksturepako.greenery.common.world.crop.WorldGenCattail
import teksturepako.greenery.common.world.grass.WorldGenFerns
import teksturepako.greenery.common.world.grass.WorldGenRyegrass
import teksturepako.greenery.common.world.grass.WorldGenTallGrass
import teksturepako.greenery.common.world.plant.WorldGenKelp
import teksturepako.greenery.common.world.plant.WorldGenRivergrass
import teksturepako.greenery.common.world.plant.WorldGenSeagrass
import teksturepako.greenery.proxy.IProxy


@Mod(
    modid = Greenery.MODID,
    name = Greenery.NAME,
    version = Greenery.VERSION,
    dependencies = Greenery.DEPENDENCIES,
    acceptedMinecraftVersions = Greenery.ACCEPTED_MINECRAFT_VERSIONS,
    modLanguageAdapter = Greenery.ADAPTER
)

@Mod.EventBusSubscriber
object Greenery {
    const val MODID = "greenery"
    const val NAME = "Greenery"
    const val VERSION = "1.5"
    const val DEPENDENCIES =
        "required-after:forgelin@[1.8.4,);required-after:fluidlogged_api@[1.8.0,);before:simpledifficulty;after:dynamictrees;after:biomesoplenty"
    const val ACCEPTED_MINECRAFT_VERSIONS = "[1.12,1.12.2,)"
    const val ADAPTER = "net.shadowfacts.forgelin.KotlinAdapter"

    const val SERVER_PROXY = "teksturepako.greenery.proxy.ServerProxy"
    const val CLIENT_PROXY = "teksturepako.greenery.proxy.ClientProxy"

    val creativeTab = ModCreativeTab()
    val generators: MutableList<IGreeneryWorldGenerator> = ArrayList()

    @SidedProxy(serverSide = SERVER_PROXY, clientSide = CLIENT_PROXY)
    lateinit var proxy: IProxy
    lateinit var logger: Logger

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        logger = event.modLog
        proxy.preInit(event)

        if (Config.generation.removeGrass) removeBOPGenerators()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        proxy.init(event)
        GameRegistry.registerWorldGenerator(WorldGenHook(), 0)
        GameRegistry.registerFuelHandler(ModFuelHandler())
        ModRecipes.register()

        MinecraftForge.EVENT_BUS.register(EventBonemeal::class.java)
        MinecraftForge.EVENT_BUS.register(EventConfigChanged::class.java)
        MinecraftForge.EVENT_BUS.register(EventUseHoe::class.java)
        MinecraftForge.TERRAIN_GEN_BUS.register(EventWorldGen::class.java)
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        proxy.postInit(event)
        ModItems.initOreDictionary()
    }

    @SubscribeEvent
    @JvmStatic
    fun onRegisterBlocks(event: RegistryEvent.Register<Block>) {
        logger.info("Registering blocks")
        ModBlocks.register(event.registry)
        if (Loader.isModLoaded("dynamictrees")) {
            DirtHelper.registerSoil(ModBlocks.blockGrass, DirtHelper.DIRTLIKE)
        }
    }

    @SubscribeEvent
    @JvmStatic fun onRegisterItems(event: RegistryEvent.Register<Item>) {
        logger.info("Registering items")
        ModBlocks.registerItemBlocks(event.registry)
        ModItems.register(event.registry)
    }

    @SubscribeEvent
    @JvmStatic
    fun onRegisterSoundEvents(event: RegistryEvent.Register<SoundEvent>) {
        logger.info("Registering sounds")
        ModSoundEvents.register(event.registry)
    }

//    @SubscribeEvent
//    @JvmStatic fun onRegisterBiomes(event: RegistryEvent.Register<Biome>) {
//        if (Config.generation.removeGrass) removeBOPGenerators()
//    }

    fun loadGenerators(): MutableList<IGreeneryWorldGenerator> {
        if (generators.isEmpty()) {

            generators.add(WorldGenCattail())
            generators.add(WorldGenArrowhead())
            generators.add(WorldGenTallGrass())
            generators.add(WorldGenRyegrass())
            generators.add(WorldGenFerns())
            generators.add(WorldGenKelp())
            generators.add(WorldGenRivergrass())
            generators.add(WorldGenSeagrass())

            logger.info("Loading world generators:")
            for (generator in generators) {
                logger.info(
                    "> " + generator.javaClass.name
                        .replace("teksturepako.greenery.common.world.", "")
                )
                parseValidBiomeTypes(generator.validBiomeTypes)
            }

            logger.info("-------------------------")
        }
        return generators
    }

}
