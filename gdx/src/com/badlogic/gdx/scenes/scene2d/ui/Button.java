
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

/** A button is a {@link Table} with a checked state and additional {@link ButtonStyle style} fields for pressed, unpressed, and
 * checked. Being a table, a button can contain any other actors.
 * <p>
 * The preferred size of the button is determined by the background ninepatch and the button contents.
 * @author Nathan Sweet */
public class Button extends Table {
	private ButtonStyle style;
	boolean isChecked;
	ButtonGroup buttonGroup;
	private ClickListener clickListener;

	public Button (Skin skin) {
		super(skin);
		initialize();
		setStyle(skin.getStyle(ButtonStyle.class));
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());
	}

	public Button (ButtonStyle style) {
		initialize();
		setStyle(style);
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());
	}

	public Button (Actor child, ButtonStyle style) {
		initialize();
		add(child);
		setStyle(style);
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());
	}

	private void initialize () {
		addListener(clickListener = new ClickListener() {
			public void clicked (ActorEvent event, float x, float y) {
				boolean wasChecked = isChecked;
				setChecked(!isChecked);
				if (wasChecked != isChecked && fire(new ChangeEvent())) setChecked(wasChecked);
			}
		});
	}

	public Button (TextureRegion region) {
		this(new ButtonStyle(new TextureRegionDrawable(region), null, null, 0f, 0f, 0f, 0f));
	}

	public Button (TextureRegion regionUp, TextureRegion regionDown) {
		this(new ButtonStyle(new TextureRegionDrawable(regionUp), new TextureRegionDrawable(regionDown), null, 0f, 0f, 0f, 0f));
	}

	public Button (TextureRegion regionUp, TextureRegion regionDown, TextureRegion regionChecked) {
		this(new ButtonStyle(new TextureRegionDrawable(regionUp), new TextureRegionDrawable(regionDown), new TextureRegionDrawable(
			regionChecked), 0f, 0f, 0f, 0f));
	}

	public Button (NinePatch patch) {
		this(new ButtonStyle(new NinePatchDrawable(patch), null, null, 0f, 0f, 0f, 0f));
	}

	public Button (NinePatch patchUp, NinePatch patchDown) {
		this(new ButtonStyle(new NinePatchDrawable(patchUp), new NinePatchDrawable(patchDown), null, 0f, 0f, 0f, 0f));
	}

	public Button (NinePatch patchUp, NinePatch patchDown, NinePatch patchChecked) {
		this(new ButtonStyle(new NinePatchDrawable(patchUp), new NinePatchDrawable(patchDown), new NinePatchDrawable(patchChecked),
			0f, 0f, 0f, 0f));
	}

	public Button (Actor child, Skin skin) {
		this(child, skin.getStyle(ButtonStyle.class));
	}

	public void setChecked (boolean isChecked) {
		if (buttonGroup != null && !buttonGroup.canCheck(this, isChecked)) return;
		this.isChecked = isChecked;
	}

	public boolean isChecked () {
		return isChecked;
	}

	public boolean isPressed () {
		return clickListener.isPressed();
	}

	public void setStyle (ButtonStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		setBackground((clickListener.isPressed() && style.down != null) ? style.down : style.up);
		invalidateHierarchy();
	}

	/** Returns the button's style. Modifying the returned style may not have an effect until {@link #setStyle(ButtonStyle)} is
	 * called. */
	public ButtonStyle getStyle () {
		return style;
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		float offsetX = 0, offsetY = 0;
		if (clickListener.isPressed()) {
			setBackground(style.down == null ? style.up : style.down);
			offsetX = style.pressedOffsetX;
			offsetY = style.pressedOffsetY;
		} else {
			if (style.checked == null)
				setBackground(style.up);
			else
				setBackground(isChecked ? style.checked : style.up);
			offsetX = style.unpressedOffsetX;
			offsetY = style.unpressedOffsetY;
		}
		validate();
		Array<Actor> children = getChildren();
		for (int i = 0; i < children.size; i++)
			children.get(i).translate(offsetX, offsetY);
		super.draw(batch, parentAlpha);
		for (int i = 0; i < children.size; i++)
			children.get(i).translate(-offsetX, -offsetY);
	}

	public float getPrefWidth () {
		float width = getTableLayout().getPrefWidth();
		if (style.up != null) width = Math.max(width, style.up.getMinWidth());
		if (style.down != null) width = Math.max(width, style.down.getMinWidth());
		if (style.checked != null) width = Math.max(width, style.checked.getMinWidth());
		return width;
	}

	public float getPrefHeight () {
		float height = getTableLayout().getPrefHeight();
		if (style.up != null) height = Math.max(height, style.up.getMinHeight());
		if (style.down != null) height = Math.max(height, style.down.getMinHeight());
		if (style.checked != null) height = Math.max(height, style.checked.getMinHeight());
		return height;
	}

	public float getMinWidth () {
		return getPrefWidth();
	}

	public float getMinHeight () {
		return getPrefHeight();
	}

	/** The style for a button, see {@link Button}.
	 * @author mzechner */
	static public class ButtonStyle {
		/** Optional. */
		public Drawable down, up, checked;
		/** Optional. */
		public float pressedOffsetX, pressedOffsetY;
		/** Optional. */
		public float unpressedOffsetX, unpressedOffsetY;

		public ButtonStyle () {
		}

		public ButtonStyle (Drawable up, Drawable down, Drawable checked, float pressedOffsetX, float pressedOffsetY,
			float unpressedOffsetX, float unpressedOffsetY) {
			this.down = down;
			this.up = up;
			this.checked = checked;
			this.pressedOffsetX = pressedOffsetX;
			this.pressedOffsetY = pressedOffsetY;
			this.unpressedOffsetX = unpressedOffsetX;
			this.unpressedOffsetY = unpressedOffsetY;
		}

		public ButtonStyle (ButtonStyle style) {
			this.down = style.down;
			this.up = style.up;
			this.checked = style.checked;
			this.pressedOffsetX = style.pressedOffsetX;
			this.pressedOffsetY = style.pressedOffsetY;
			this.unpressedOffsetX = style.unpressedOffsetX;
			this.unpressedOffsetY = style.unpressedOffsetY;
		}
	}
}
