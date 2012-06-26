
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.esotericsoftware.tablelayout.Cell;

/** A button with a child {@link Image} to display an image. This is useful when the button must be larger than the image and the
 * image centered on the button. If the image is the size of the button, a {@link Button} without any children can be used, where
 * the {@link Button.ButtonStyle#up}, {@link Button.ButtonStyle#down}, and {@link Button.ButtonStyle#checked} nine patches define
 * the image.
 * @author Nathan Sweet */
public class ImageButton extends Button {
	private final Image image;
	private ImageButtonStyle style;

	public ImageButton (Skin skin) {
		this(skin.getStyle("default", ImageButtonStyle.class));
	}

	public ImageButton (ImageButtonStyle style) {
		super(style);
		image = new Image();
		image.setScaling(Scaling.fit);
		add(image);
		setStyle(style);
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());
	}

	public ImageButton (TextureRegion region) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, new TextureRegionDrawable(region), null, null));
	}

	public ImageButton (TextureRegion regionUp, TextureRegion regionDown) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, new TextureRegionDrawable(regionUp), new TextureRegionDrawable(
			regionDown), null));
	}

	public ImageButton (TextureRegion regionUp, TextureRegion regionDown, TextureRegion regionChecked) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, new TextureRegionDrawable(regionUp), new TextureRegionDrawable(
			regionDown), new TextureRegionDrawable(regionChecked)));
	}

	public ImageButton (NinePatch patch) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, new NinePatchDrawable(patch), null, null));
	}

	public ImageButton (NinePatch patchUp, NinePatch patchDown) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, new NinePatchDrawable(patchUp),
			new NinePatchDrawable(patchDown), null));
	}

	public ImageButton (NinePatch patchUp, NinePatch patchDown, NinePatch patchChecked) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, new NinePatchDrawable(patchUp),
			new NinePatchDrawable(patchDown), new NinePatchDrawable(patchChecked)));
	}

	public void setStyle (ButtonStyle style) {
		if (!(style instanceof ImageButtonStyle)) throw new IllegalArgumentException("style must be an ImageButtonStyle.");
		super.setStyle(style);
		this.style = (ImageButtonStyle)style;
		if (image != null) updateImage();
	}

	public ImageButtonStyle getStyle () {
		return style;
	}

	private void updateImage () {
		boolean isPressed = isPressed();
		if (isPressed && style.imageDown != null)
			image.setDrawable(style.imageDown);
		else if (isChecked && style.imageChecked != null)
			image.setDrawable(style.imageChecked);
		else if (style.imageUp != null) //
			image.setDrawable(style.imageUp);
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		updateImage();
		super.draw(batch, parentAlpha);
	}

	public Image getImage () {
		return image;
	}

	public Cell getImageCell () {
		return getCell(image);
	}

	/** The style for an image button, see {@link ImageButton}.
	 * @author Nathan Sweet */
	static public class ImageButtonStyle extends ButtonStyle {
		public Drawable imageUp, imageDown, imageChecked;

		public ImageButtonStyle () {
		}

		public ImageButtonStyle (Drawable down, Drawable up, Drawable checked, float pressedOffsetX, float pressedOffsetY,
			float unpressedOffsetX, float unpressedOffsetY, Drawable imageUp, Drawable imageDown, Drawable imageChecked) {
			super(down, up, checked, pressedOffsetX, pressedOffsetY, unpressedOffsetX, unpressedOffsetY);
			this.imageUp = imageUp;
			this.imageDown = imageDown;
			this.imageChecked = imageChecked;
		}

		public ImageButtonStyle (ImageButtonStyle style) {
			super(style);
			this.imageUp = style.imageUp;
			this.imageDown = style.imageDown;
			this.imageChecked = style.imageChecked;
		}
	}
}
