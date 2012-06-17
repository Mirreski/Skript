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

package ch.njol.skript.lang;

import java.lang.reflect.Array;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.TriggerFileLoader;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Condition;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.Converter.ConverterUtils;
import ch.njol.skript.api.intern.ConvertedExpression;
import ch.njol.skript.api.intern.SkriptAPIException;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;

/**
 * An implementation of the {@link Expression} interface. You should usually extend this class to make a new expression.
 * 
 * @see Skript#registerExpression(Class, Class, String...)
 * 
 * @author Peter Güttinger
 */
public abstract class SimpleExpression<T> implements Expression<T> {
	
	private boolean and = true;
	
	private int time = 0;
	
	protected SimpleExpression() {}
	
	@Override
	public final T getSingle(final Event e) {
		final T[] all = getArray(e);
		if (all.length == 0)
			return null;
		if (all.length > 1)
			throw new SkriptAPIException("Call to getSingle() on a non-single expression");
		return all[0];
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final T[] getArray(final Event e) {
		final T[] all = getAll(e);
		if (all == null)
			return (T[]) Array.newInstance(getReturnType(), 0);
		if (all.length == 0)
			return all;
		
		int numNonNull = 0;
		for (final T t : all)
			if (t != null)
				numNonNull++;
		
		if (!and) {
			if (all.length == 1 && all[0] != null)
				return all;
			int rand = Skript.random.nextInt(numNonNull);
			final T[] one = (T[]) Array.newInstance(getReturnType(), 1);
			for (final T t : all) {
				if (t != null) {
					if (rand == 0) {
						one[0] = t;
						return one;
					}
					rand--;
				}
			}
			throw new RuntimeException();// shouldn't happen
		}
		
		if (numNonNull == all.length)
			return all;
		final T[] r = (T[]) Array.newInstance(getReturnType(), numNonNull);
		int i = 0;
		for (final T t : all)
			if (t != null)
				r[i++] = t;
		return r;
	}
	
	@Override
	public final <V> V getSingle(final Event e, final Converter<? super T, ? extends V> converter) {
		final T t = getSingle(e);
		if (t == null)
			return null;
		return converter.convert(t);
	}
	
	@Override
	public final <V> V[] getArray(final Event e, final Class<V> to, final Converter<? super T, ? extends V> converter) {
		return ConverterUtils.convert(getArray(e), converter, to);
	}
	
	/**
	 * This is the internal method to get an expression's values.<br>
	 * To get the expression's value from the outside use {@link #getSingle(Event)} or {@link #getArray(Event)}.
	 * 
	 * @param e The event
	 * @return An array of values for this event. May contain nulls.
	 */
	protected abstract T[] getAll(Event e);
	
	@Override
	public final boolean check(final Event e, final Checker<? super T> c, final Condition cond) {
		return check(e, c, cond.isNegated());
	}
	
	@Override
	public final boolean check(final Event e, final Checker<? super T> c) {
		return check(e, c, false);
	}
	
	private final boolean check(final Event e, final Checker<? super T> c, final boolean invert) throws ClassCastException {
		return check(getAll(e), c, invert, and);
	}
	
	public final static <T> boolean check(final T[] all, final Checker<? super T> c, final boolean invert, final boolean and) throws ClassCastException {
		boolean hasElement = false;
		if (all != null) {
			for (final T t : all) {
				if (t == null)
					continue;
				hasElement = true;
				final boolean b = c.check(t);
				if (invert) {
					if (and && b)
						return false;
					if (!and && !b)
						return true;
				} else {
					if (and && !b)
						return false;
					if (!and && b)
						return true;
				}
			}
		}
		if (!hasElement)
			return false;
		return and;
	}
	
	/**
	 * Converts this expression to another type. Unless the expression is special, the default implementation is sufficient.<br/>
	 * This method is guaranteed to never being called with a supertype of the return type of this expression, or the return type itself.
	 * 
	 * @param to The desired return type of the returned expression
	 * @return Expression with the desired return type or null if it can't be converted to the given type
	 * @see ConvertedExpression#newInstance(Expression, Class)
	 * @see Converter
	 * @see SimpleExpression#getConvertedExpression(Class)
	 */
	protected <R> ConvertedExpression<T, ? extends R> getConvertedExpr(final Class<R> to) {
		if (to.isAssignableFrom(getReturnType())) {
			throw new SkriptAPIException("invalid call to SimpleExpression.getConvertedVar (current type: " + getReturnType().getName() + ", requested type: " + to.getName() + ")");
		}
		return ConvertedExpression.newInstance(this, to);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final <R> Expression<? extends R> getConvertedExpression(final Class<R> to) {
		if (to.isAssignableFrom(getReturnType()))
			return (Expression<? extends R>) this;
		return this.getConvertedExpr(to);
	}
	
	@Override
	public boolean getAnd() {
		return and;
	}
	
	/**
	 * this is set automatically and should not be changed.
	 * 
	 * @param and
	 */
	@Override
	public void setAnd(final boolean and) {
		this.and = and;
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc} <br>
	 * <br>
	 * The default implementation sets the time but returns false.
	 * 
	 * @see #setTime(int, Class, Expression...)
	 * @see #setTime(int, Expression, Class...)
	 */
	@Override
	public boolean setTime(final int time) {
		this.time = time;
		return false;
	}
	
	protected final boolean setTime(final int time, final Class<? extends Event> applicableEvent, final Expression<?>... mustbeDefaultVars) {
		if (Utils.contains(TriggerFileLoader.currentEvents, applicableEvent)) {
			for (final Expression<?> var : mustbeDefaultVars) {
				if (var.isDefault()) {
					this.time = time;
					return true;
				}
			}
		}
		return false;
	}
	
	protected final boolean setTime(final int time, final Expression<?> mustbeDefaultVar, final Class<? extends Event>... applicableEvents) {
		if (mustbeDefaultVar.isDefault()) {
			for (final Class<? extends Event> e : applicableEvents) {
				if (Utils.contains(TriggerFileLoader.currentEvents, e)) {
					this.time = time;
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public int getTime() {
		return time;
	}
	
	@Override
	public boolean isDefault() {
		return false;
	}
	
	@Override
	public abstract String toString();
}