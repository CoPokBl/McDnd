package net.serble.mcdnd.schemas;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public abstract class RayCastCallback {

    public abstract void run(Entity hitEntity, Block hitBlock, BlockFace hitBlockFace);
}
