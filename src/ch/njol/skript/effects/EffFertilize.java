/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.skript.effects;

import net.minecraft.server.Item;

import org.bukkit.DyeColor;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Effect;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.Variable;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class EffFertilize extends Effect {
	
	static {
		Skript.registerEffect(EffFertilize.class, "fertili(z|s)e [%blocks%]");
	}
	
	private Variable<Block> blocks;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		blocks = (Variable<Block>) vars[0];
		return true;
	}
	
	@Override
	public void execute(final Event e) {
		for (final Block b : blocks.getArray(e)) {
			Item.INK_SACK.interactWith(new net.minecraft.server.ItemStack(Item.INK_SACK, DyeColor.WHITE.getData(), 1), null, ((CraftWorld) b.getWorld()).getHandle(), b.getX(), b.getY(), b.getZ(), 0);
		}
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		return "fertilize " + blocks.getDebugMessage(e);
	}
	
}
