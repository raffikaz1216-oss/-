package jp.kyushu.realscale;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

@Mod(KyushuRealScale.MODID)
public class KyushuRealScale {
    public static final String MODID = "kyushurealscale";
    private static final int TILE_SIZE = 16;
    private static final int SEA_LEVEL = 63;
    private static final int LAND_BASE_Y = 64;
    private static final int MAX_RELIEF = 160;
    private static final int OCEAN_FLOOR_Y = 50;

    public KyushuRealScale() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("kyushu")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("generate")
                    .then(Commands.argument("tileX", IntegerArgumentType.integer())
                        .then(Commands.argument("tileZ", IntegerArgumentType.integer())
                            .executes(ctx -> generateTile(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "tileX"),
                                IntegerArgumentType.getInteger(ctx, "tileZ")
                            ))
                        )
                    )
                )
        );
    }

    private int generateTile(net.minecraft.commands.CommandSourceStack source, int tileX, int tileZ) {
        ServerLevel level = source.getLevel();
        Path tileDir = FMLPaths.CONFIGDIR.get().resolve("kyushurealscale").resolve("tiles");
        File imageFile = tileDir.resolve(tileX + "_" + tileZ + ".png").toFile();

        if (!imageFile.isFile()) {
            source.sendFailure(Component.literal("Tile not found: " + imageFile.getAbsolutePath()));
            return 0;
        }

        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null || image.getWidth() != TILE_SIZE || image.getHeight() != TILE_SIZE) {
                source.sendFailure(Component.literal("Tile PNG must be exactly 16x16 pixels."));
                return 0;
            }

            int changedColumns = 0;
            int minY = level.getMinBuildHeight();
            int maxY = level.getMaxBuildHeight() - 1;

            for (int px = 0; px < TILE_SIZE; px++) {
                for (int pz = 0; pz < TILE_SIZE; pz++) {
                    int argb = image.getRGB(px, pz);
                    int alpha = (argb >>> 24) & 0xff;
                    int x = tileX * TILE_SIZE + px;
                    int z = tileZ * TILE_SIZE + pz;

                    if (alpha == 0) {
                        // Sea surface only; no seabed is generated.
                        for (int y = OCEAN_FLOOR_Y; y <= SEA_LEVEL; y++) {
                            if (y >= minY && y <= maxY) {
                                level.setBlock(new BlockPos(x, y, z), Blocks.WATER.defaultBlockState(), 2);
                            }
                        }
                    } else {
                        int r = (argb >>> 16) & 0xff;
                        int g = (argb >>> 8) & 0xff;
                        int b = argb & 0xff;
                        int gray = (r + g + b) / 3;
                        int surfaceY = LAND_BASE_Y + (gray * MAX_RELIEF / 255);
                        surfaceY = Math.max(minY + 4, Math.min(maxY - 1, surfaceY));

                        BlockState stone = Blocks.STONE.defaultBlockState();
                        BlockState dirt = Blocks.DIRT.defaultBlockState();
                        for (int y = minY + 1; y <= surfaceY; y++) {
                            BlockState state = (y == surfaceY)
                                ? Blocks.GRASS_BLOCK.defaultBlockState()
                                : (y >= surfaceY - 3 ? dirt : stone);
                            level.setBlock(new BlockPos(x, y, z), state, 2);
                        }
                    }
                    changedColumns++;
                }
            }

            final int generatedColumns = changedColumns;
            source.sendSuccess(
                () -> Component.literal("Generated tile " + tileX + "," + tileZ
                    + " (" + generatedColumns + " columns). 1 pixel = 1 metre."),
                true
            );
            return generatedColumns;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Could not read/generate tile: " + e.getMessage()));
            return 0;
        }
    }
}
