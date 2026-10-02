package cn.ZeroEngine.Engine.api.v3.feature.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import cn.ZeroEngine.Engine.api.v3.feature.item.SItem;

import java.util.Collections;
import java.util.List;

public abstract class SBlock extends SItem {

    

    public boolean isOpaque() { return true; }

    public boolean isSolid() { return true; }

    public boolean isFlammable() { return false; }

    public boolean isInteractable() { return true; }

    public int lightLevel() { return 0; }

    public float hardness() { return -1f; }

    public float blastResistance() { return -1f; }

    

    public int redstoneRadius() { return 0; }

    public boolean emitsRedstone() { return false; }

    public int redstonePower() { return 0; }

    public void onRedstonePowered(Block block, int power) {}

    public void onRedstoneUnpowered(Block block) {}

    

    public DropMode dropMode() { return DropMode.CUSTOM; }

    public List<ItemStack> drops() { return Collections.emptyList(); }

    public int expDrop() { return 0; }

    public enum DropMode {
        VANILLA,
        CUSTOM,
        NONE
    }

    

    public boolean canPlaceAt(Block block, BlockFace against) { return true; }

    

    public void onPlace(BlockPlaceEvent e) {}

    public void onBreak(BlockBreakEvent e) {}

    public void onBlockDamage(BlockDamageEvent e) {}

    public boolean onBlockRightClick(PlayerInteractEvent e) { return false; }

    public boolean onBlockLeftClick(PlayerInteractEvent e) { return false; }

    public void onBurn(BlockBurnEvent e) {}

    public void onIgnite(BlockIgniteEvent e) {}

    public void onPhysics(BlockPhysicsEvent e) {}

    public void onFade(BlockFadeEvent e) {}

    public void onForm(BlockFormEvent e) {}

    public void onSpread(BlockSpreadEvent e) {}

    public void onFromTo(BlockFromToEvent e) {}

    public void onGrow(BlockGrowEvent e) {}

    public void onPistonExtend(BlockPistonExtendEvent e) {}

    public void onPistonRetract(BlockPistonRetractEvent e) {}

    public void onDispense(BlockDispenseEvent e) {}

    public void onExplode(BlockExplodeEvent e) {}

    public void onLeavesDecay(LeavesDecayEvent e) {}

    public void onMoistureChange(MoistureChangeEvent e) {}

    public void onFluidLevelChange(FluidLevelChangeEvent e) {}

    public void onEntityChangeBlock(EntityChangeBlockEvent e) {}

    public void onSignChange(SignChangeEvent e) {}

    public void onNotePlay(NotePlayEvent e) {}

    public void onExpDrop(BlockExpEvent e) {}
}
