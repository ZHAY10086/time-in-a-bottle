package com.haoict.tiab.items;

import com.haoict.tiab.config.Constants;
import com.haoict.tiab.config.NBTKeys;
import com.haoict.tiab.config.TiabConfig;
import com.haoict.tiab.utils.lang.Styles;
import com.haoict.tiab.utils.lang.Translation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ItemTimeInABottle extends AbstractItemTiab {

    public ItemTimeInABottle() {
        super();
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(itemStack, level, entity, itemSlot, isSelected);
        if (level.isClientSide) {
            return;
        }

        if (level.getGameTime() % Constants.TICK_CONST == 0) {
            int storedTime = this.getStoredEnergy(itemStack);
            if (storedTime < TiabConfig.COMMON.maxStoredTime.get()) {
                this.setStoredEnergy(itemStack, storedTime + Constants.TICK_CONST);
            }
        }

        // remove time if player has other TIAB items in his inventory, check every 10 sec
        if (level.getGameTime() % (Constants.TICK_CONST * 10) == 0) {
            if (!(entity instanceof Player)) {
                return;
            }

            Player player = (Player) entity;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (invStack.getItem() == this) {
                    if (invStack != itemStack) {
                        int otherTimeData = this.getStoredEnergy(invStack);
                        int myTimeData = this.getStoredEnergy(itemStack);

                        if (myTimeData < otherTimeData) {
                            setStoredEnergy(itemStack, 0);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(itemStack, world, tooltip, flag);

        int storedTime = this.getStoredEnergy(itemStack);
        int storedSeconds = storedTime / Constants.TICK_CONST;
        int hours = storedSeconds / 3600;
        int minutes = (storedSeconds % 3600) / 60;
        int seconds = storedSeconds % 60;

        tooltip.add(Translation.TOOLTIP_STORED_TIME.componentTranslation(String.format("%02d", hours), String.format("%02d", minutes), String.format("%02d", seconds)).setStyle(Styles.GREEN));
    }

    @Override
    public int getStoredEnergy(ItemStack stack) {
        return stack.getOrCreateTag().getInt(NBTKeys.STORED_TIME);
    }

    @Override
    public void setStoredEnergy(ItemStack stack, int energy) {
        int newStoredTime = Math.min(energy, TiabConfig.COMMON.maxStoredTime.get());
        stack.getOrCreateTag().putInt(NBTKeys.STORED_TIME, newStoredTime);
    }

    @Override
    public void applyDamage(ItemStack stack, int damage) {
        setStoredEnergy(stack, getStoredEnergy(stack) - damage);
    }

}
