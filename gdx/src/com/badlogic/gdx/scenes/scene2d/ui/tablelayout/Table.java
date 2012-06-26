/*******************************************************************************
 * Copyright (c) 2011, Nathan Sweet <nathan.sweet@gmail.com>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.badlogic.gdx.scenes.scene2d.ui.tablelayout;

import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.ParseException;

/** @author Nathan Sweet */
public class Table extends WidgetGroup {
	private final TableLayout layout;
	private Drawable backgroundDrawable;
	private final Rectangle tableBounds = new Rectangle();
	private final Rectangle scissors = new Rectangle();
	private boolean clip;

	public Table () {
		this(null, null);
	}

	public Table (String name) {
		this(null, null);
	}

	public Table (Skin skin) {
		this(skin, null);
	}

	public Table (Skin skin, TableLayout layout) {
		setTransform(false);
		if (layout == null) layout = new TableLayout();
		this.layout = layout;
		layout.setTable(this);
		layout.skin = skin;
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		validate();
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		drawBackground(batch, parentAlpha);

		if (isTransform()) {
			applyTransform(batch);
			if (clip) {
				calculateScissors(batch.getTransformMatrix());
				if (ScissorStack.pushScissors(scissors)) {
					drawChildren(batch, parentAlpha);
					ScissorStack.popScissors();
				}
			} else
				drawChildren(batch, parentAlpha);
			resetTransform(batch);
		} else
			super.draw(batch, parentAlpha);
	}

	/** Called to draw the background, before clipping is applied (if enabled). Default implementation draws the background nine
	 * patch. */
	protected void drawBackground (SpriteBatch batch, float parentAlpha) {
		if (backgroundDrawable != null) {
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			backgroundDrawable.draw(batch, getX(), getY(), getWidth(), getHeight());
		}
	}

	private void calculateScissors (Matrix4 transform) {
		tableBounds.x = 0;
		tableBounds.y = 0;
		tableBounds.width = getWidth();
		tableBounds.height = getHeight();
		if (backgroundDrawable != null) {
			tableBounds.x += layout.getToolkit().width(layout, layout.getPadLeft());
			tableBounds.y += layout.getToolkit().width(layout, layout.getPadBottom());
			tableBounds.width -= tableBounds.x + layout.getToolkit().width(layout, layout.getPadRight());
			tableBounds.height -= tableBounds.y + layout.getToolkit().width(layout, layout.getPadTop());
		}
		ScissorStack.calculateScissors(getStage().getCamera(), transform, tableBounds, scissors);
	}

	public void invalidate () {
		layout.invalidate();
		super.invalidate();
	}

	public float getPrefWidth () {
		if (backgroundDrawable != null) return Math.max(layout.getPrefWidth(), (int)backgroundDrawable.getMinWidth());
		return layout.getPrefWidth();
	}

	public float getPrefHeight () {
		if (backgroundDrawable != null) return Math.max(layout.getPrefHeight(), (int)backgroundDrawable.getMinHeight());
		return layout.getPrefHeight();
	}

	public float getMinWidth () {
		return layout.getMinWidth();
	}

	public float getMinHeight () {
		return layout.getMinHeight();
	}

	/** Sets the background drawable and sets the table's padding to {@link Drawable#getBottomHeight()} ,
	 * {@link Drawable#getTopHeight()}, {@link Drawable#getLeftWidth()}, and {@link Drawable#getRightWidth()}.
	 * @param background If null, no background will be set and all padding is removed. */
	public void setBackground (Drawable background) {
		if (this.backgroundDrawable == background) return;
		this.backgroundDrawable = background;
		if (background == null)
			pad(null);
		else {
			padBottom((int)background.getBottomHeight());
			padTop((int)background.getTopHeight());
			padLeft((int)background.getLeftWidth());
			padRight((int)background.getRightWidth());
			invalidate();
		}
	}

	public Drawable getBackground () {
		return backgroundDrawable;
	}

	/** Causes the contents to be clipped if they exceed the table bounds. Enabling clipping will set {@link #setTransform(boolean)}
	 * to true. */
	public void setClip (boolean enabled) {
		clip = enabled;
		setTransform(enabled);
		invalidate();
	}

	/** Returns the row index for the y coordinate. */
	public int getRow (float y) {
		return layout.getRow(y);
	}

	public TableLayout getTableLayout () {
		return layout;
	}

	/** Removes all actors and cells from the table. */
	public void clear () {
		super.clear();
		layout.clear();
		invalidate();
	}

	public Actor register (String name, Actor widget) {
		return layout.register(name, widget);
	}

	public Cell add (String text) {
		if (layout.skin == null) throw new IllegalStateException("Table must have a skin set to use this method.");
		return add(new Label(text, layout.skin));
	}

	/** Adds a cell with a placeholder actor. */
	public Cell add () {
		return add((Actor)null);
	}

	/** Adds a new cell to the table with the specified actor.
	 * @see TableLayout#add(Actor)
	 * @param actor May be null to add a cell without an actor. */
	public Cell add (Actor actor) {
		return layout.add(actor);
	}

	/** Adds a new cell to the table with the specified actors in a {@link Stack}.
	 * @see TableLayout#stack(Actor...)
	 * @param actor May be null to add a cell without an actor. */
	public Cell stack (Actor... actor) {
		return layout.stack(actor);
	}

	/** Creates a new table with the same Skin and AssetManager as this table. */
	public Table newTable () {
		return layout.getToolkit().newTable(this);
	}

	/** Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
	 * for all cells in the new row.
	 * @see TableLayout#row() */
	public Cell row () {
		return layout.row();
	}

	public void parse (FileHandle tableDescriptionFile) {
		try {
			layout.parse(tableDescriptionFile.readString());
		} catch (ParseException ex) {
			throw new ParseException("Error parsing layout file: " + tableDescriptionFile, ex);
		}
	}

	/** Parses a table description and adds the actors and cells to the table.
	 * @see TableLayout#parse(String) */
	public void parse (String tableDescription) {
		layout.parse(tableDescription);
	}

	/** Gets the cell values that will be used as the defaults for all cells in the specified column.
	 * @see TableLayout#columnDefaults(int) */
	public Cell columnDefaults (int column) {
		return layout.columnDefaults(column);
	}

	/** The cell values that will be used as the defaults for all cells.
	 * @see TableLayout#defaults() */
	public Cell defaults () {
		return layout.defaults();
	}

	/** Positions and sizes children of the actor being laid out using the cell associated with each child.
	 * @see TableLayout#layout() */
	public void layout () {
		layout.layout();
	}

	/** Removes all actors and cells from the table (same as {@link #clear()}) and additionally resets all table properties and
	 * cell, column, and row defaults.
	 * @see TableLayout#reset() */
	public void reset () {
		layout.reset();
	}

	/** Returns the widget with the specified name, anywhere in the table hierarchy. */
	public Actor getWidget (String name) {
		return layout.getWidget(name);
	}

	/** Returns all named widgets, anywhere in the table hierarchy. */
	public List<Actor> getWidgets () {
		return layout.getWidgets();
	}

	/** Returns all widgets with the specified name prefix, anywhere in the table hierarchy. */
	public List<Actor> getWidgets (String namePrefix) {
		return layout.getWidgets(namePrefix);
	}

	/** Returns the cell for the specified actor, anywhere in the table hierarchy.
	 * @see TableLayout#getCell(Actor) */
	public Cell getCell (Actor actor) {
		return layout.getCell(actor);
	}

	/** Returns the cell with the specified name, anywhere in the table hierarchy.
	 * @see TableLayout#getCell(String) */
	public Cell getCell (String name) {
		return layout.getCell(name);
	}

	/** Returns all cells, anywhere in the table hierarchy.
	 * @see TableLayout#getAllCells() */
	public List<Cell> getAllCells () {
		return layout.getAllCells();
	}

	/** Returns all cells with the specified name prefix, anywhere in the table hierarchy.
	 * @see TableLayout#getAllCells(String) */
	public List<Cell> getAllCells (String namePrefix) {
		return layout.getAllCells(namePrefix);
	}

	/** Returns the cells for this table.
	 * @see TableLayout#getCells() */
	public List<Cell> getCells () {
		return layout.getCells();
	}

	/** Sets the actor in the cell with the specified name.
	 * @see TableLayout#setWidget(String, Actor) */
	public void setWidget (String name, Actor actor) {
		layout.setWidget(name, actor);
	}

	/** The fixed size of the table.
	 * @see TableLayout#size(String, String) */
	public Table size (String width, String height) {
		layout.size(width, height);
		return this;
	}

	/** The fixed width of the table, or null.
	 * @see TableLayout#width(String) */
	public Table width (String width) {
		layout.width(width);
		return this;
	}

	/** The fixed height of the table, or null.
	 * @see TableLayout#height(String) */
	public Table height (String height) {
		layout.height(height);
		return this;
	}

	/** The fixed size of the table.
	 * @see TableLayout#size(int, int) */
	public Table size (int width, int height) {
		layout.size(width, height);
		return this;
	}

	/** The fixed width of the table.
	 * @see TableLayout#width(int) */
	public Table width (int width) {
		layout.width(width);
		return this;
	}

	/** The fixed height of the table.
	 * @see TableLayout#height(int) */
	public Table height (int height) {
		layout.height(height);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(String) */
	public Table pad (String pad) {
		layout.pad(pad);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(String, String, String, String) */
	public Table pad (String top, String left, String bottom, String right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top of the table.
	 * @see TableLayout#padTop(String) */
	public Table padTop (String padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left of the table.
	 * @see TableLayout#padLeft(String) */
	public Table padLeft (String padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom of the table.
	 * @see TableLayout#padBottom(String) */
	public Table padBottom (String padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right of the table.
	 * @see TableLayout#padRight(String) */
	public Table padRight (String padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(int) */
	public Table pad (int pad) {
		layout.pad(pad);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(int, int, int, int) */
	public Table pad (int top, int left, int bottom, int right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top of the table.
	 * @see TableLayout#padTop(int) */
	public Table padTop (int padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left of the table.
	 * @see TableLayout#padLeft(int) */
	public Table padLeft (int padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom of the table.
	 * @see TableLayout#padBottom(int) */
	public Table padBottom (int padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right of the table.
	 * @see TableLayout#padRight(int) */
	public Table padRight (int padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Alignment of the table within the actor being laid out. Set to {@link Align#CENTER}, {@link Align#TOP}, {@link Align#BOTTOM}
	 * , {@link Align#LEFT} , {@link Align#RIGHT}, or any combination of those.
	 * @see TableLayout#align(int) */
	public Table align (int align) {
		layout.align(align);
		return this;
	}

	/** Alignment of the table within the actor being laid out. Set to "center", "top", "bottom", "left", "right", or a string
	 * containing any combination of those.
	 * @see TableLayout#align(String) */
	public Table align (String value) {
		layout.align(value);
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#CENTER}.
	 * @see TableLayout#center() */
	public Table center () {
		layout.center();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#TOP}.
	 * @see TableLayout#top() */
	public Table top () {
		layout.top();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#LEFT}.
	 * @see TableLayout#left() */
	public Table left () {
		layout.left();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#BOTTOM}.
	 * @see TableLayout#bottom() */
	public Table bottom () {
		layout.bottom();
		return this;
	}

	/** Sets the alignment of the table within the actor being laid out to {@link Align#RIGHT}.
	 * @see TableLayout#right() */
	public Table right () {
		layout.right();
		return this;
	}

	/** Turns on all debug lines.
	 * @see TableLayout#debug() */
	public Table debug () {
		layout.debug();
		return this;
	}

	/** Turns on debug lines. Set to {@value TableLayout#DEBUG_ALL}, {@value TableLayout#DEBUG_TABLE},
	 * {@value TableLayout#DEBUG_CELL}, {@value TableLayout#DEBUG_WIDGET}, or any combination of those. Set to
	 * {@value TableLayout#DEBUG_NONE} to disable.
	 * @see TableLayout#debug() */
	public Table debug (int debug) {
		layout.debug(debug);
		return this;
	}

	/** Turns on debug lines. Set to "all", "table", "cell", "widget", or a string containing any combination of those. Set to null
	 * to disable.
	 * @see TableLayout#debug(String) */
	public Table debug (String value) {
		layout.debug(value);
		return this;
	}

	public int getDebug () {
		return layout.getDebug();
	}

	public String getLayoutWidth () {
		return layout.getHeight();
	}

	public String getLayoutHeight () {
		return layout.getHeight();
	}

	public String getPadTop () {
		return layout.getPadTop();
	}

	public String getPadLeft () {
		return layout.getPadLeft();
	}

	public String getPadBottom () {
		return layout.getPadBottom();
	}

	public String getPadRight () {
		return layout.getPadRight();
	}

	public int getAlign () {
		return layout.getAlign();
	}

	public void setSkin (Skin skin) {
		layout.skin = skin;
	}

	public void setAssetManager (AssetManager assetManager) {
		layout.assetManager = assetManager;
	}

	/** Draws the debug lines for all TableLayouts in the stage. If this method is not called each frame, no debug lines will be
	 * drawn. If debug is never turned on for any table in the application, calling this method will have no effect. If a table has
	 * ever had debug set, calling this method causes an expensive traversal of all actors in the stage. */
	static public void drawDebug (Stage stage) {
		if (!LibgdxToolkit.drawDebug) return;
		drawDebug(stage.getActors(), stage.getSpriteBatch());
	}

	static private void drawDebug (Array<Actor> actors, SpriteBatch batch) {
		for (int i = 0, n = actors.size; i < n; i++) {
			Actor actor = actors.get(i);
			if (actor instanceof Table) ((Table)actor).layout.drawDebug(batch);
			if (actor instanceof Group) drawDebug(((Group)actor).getChildren(), batch);
		}
	}
}
