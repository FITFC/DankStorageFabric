
package tfar.dankstorage.blockentity;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tfar.dankstorage.DankStorage;
import tfar.dankstorage.block.DockBlock;
import tfar.dankstorage.container.DockMenu;
import tfar.dankstorage.world.DankInventory;
import tfar.dankstorage.item.DankItem;
import tfar.dankstorage.utils.DankStats;
import tfar.dankstorage.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DockBlockEntity extends BlockEntity implements Nameable, MenuProvider, Container {

    public CompoundTag settings;

    public int numPlayersUsing = 0;
    protected Component customName;
    protected boolean originalName;

    public DockBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DankStorage.dank_tile,blockPos, blockState);
    }

    public static final DankInventory DUMMY = new DankInventory(DankStats.zero, -1);

    public DankInventory getInventory() {
        if (settings != null && settings.contains(Utils.ID)) {
            int id = settings.getInt(Utils.ID);
            DankInventory dankInventory = DankStorage.instance.data.getInventory(id);

            //if the id is too high
            if (dankInventory == null) {
                int next = DankStorage.instance.data.getNextID();
                dankInventory = DankStorage.instance.data
                        .getOrCreateInventory(next,DankStats.values()[getBlockState().getValue(DockBlock.TIER)]);
                settings.putInt(Utils.ID,next);
            }

            return dankInventory;
        }
        return DUMMY;
    }

    public int getComparatorSignal() {
        return this.getInventory().calcRedstone();
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.numPlayersUsing = type;
            this.setChanged();
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.settings = compound.getCompound(Utils.SET);
        if (compound.contains("CustomName", 8)) {
            this.setCustomName(Component.Serializer.fromJson(compound.getString("CustomName")));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (settings != null) {
            tag.put(Utils.SET, settings);
        }
        if (this.hasCustomName()) {
            tag.putString("CustomName", Component.Serializer.toJson(this.customName));
        }
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return super.getUpdateTag();//save(new CompoundTag());
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    //Inventory nonsense

    @Override
    public int getContainerSize() {
        return getInventory().getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return getInventory().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return getInventory().getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return getInventory().removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return getInventory().removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        getInventory().setItem(slot, stack);
    }

    @Override
    public int getMaxStackSize() {
        return getInventory().getMaxStackSize();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public Component getName() {
        return customName != null ? customName : getDefaultName();
    }

    public Component getDefaultName() {
        return new TranslatableComponent("container.dankstorage.dank_" + getBlockState().getValue(DockBlock.TIER));
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return customName;
    }

    public void setCustomName(Component text) {
        this.customName = text;
    }

    @Nullable
    @Override
    public DockMenu createMenu(int syncId, Inventory inventory, Player player) {

        int tier = getBlockState().getValue(DockBlock.TIER);

        DankInventory dankInventory = getInventory();

        if (dankInventory.dankStats != DankStats.values()[tier]) {
            Utils.warn(player,DankStats.values()[tier],dankInventory.dankStats);
            return null;
        }

        return switch (getBlockState().getValue(DockBlock.TIER)) {
            case 1 -> DockMenu.t1s(syncId, inventory, dankInventory,this);
            case 2 -> DockMenu.t2s(syncId, inventory, dankInventory,this);
            case 3 -> DockMenu.t3s(syncId, inventory, dankInventory,this);
            case 4 -> DockMenu.t4s(syncId, inventory, dankInventory,this);
            case 5 -> DockMenu.t5s(syncId, inventory, dankInventory,this);
            case 6 -> DockMenu.t6s(syncId, inventory, dankInventory,this);
            case 7 -> DockMenu.t7s(syncId, inventory, dankInventory,this);
            default -> null;
        };
    }

    public void removeTankWithItemSpawn() {
        ItemStack dankInStack = removeDankWithoutItemSpawn();
        ItemEntity entity = new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), dankInStack);
        level.addFreshEntity(entity);
    }

    public ItemStack removeDankWithoutItemSpawn() {
        int tier = getBlockState().getValue(DockBlock.TIER);

        if (tier == 0) {
            throw new RuntimeException("tried to remove a null dank?");
        }

        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(DockBlock.TIER, 0));
        ItemStack stack = new ItemStack(Utils.getItemFromTier(tier));

        if (settings != null) {
            stack.getOrCreateTag().put(Utils.SET,settings);
        }

        settings = null;

        if (hasCustomName() && originalName) {
            stack.setHoverName(getCustomName());
        }

        setCustomName(null);
        originalName = false;
        setChanged();
        return stack;
    }

    public void addDank(ItemStack tank) {
        if (tank.getItem() instanceof DankItem) {
            DankStats stats = ((DankItem) tank.getItem()).stats;
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(DockBlock.TIER, stats.ordinal()));
            setCustomName(tank.getHoverName());
            originalName = tank.hasCustomHoverName();
            CompoundTag iSettings = Utils.getSettings(tank);
            tank.shrink(1);

            if (iSettings != null && iSettings.contains(Utils.ID)) {
                this.settings = iSettings;
            } else {
                this.settings = new CompoundTag();
                int newId = DankStorage.instance.data.getNextID();
                DankStorage.instance.data.getOrCreateInventory(newId,stats);
                settings.putInt(Utils.ID,newId);
            }
            setChanged();
        }
    }

    @Override
    public void clearContent() {

    }
}