package com.DoinkOink.Misc;

import net.runelite.client.util.AsyncBufferedImage;

import java.awt.image.BufferedImage;

public class LootListerItem
{
	public final int ID;
	public final String ItemText;
	public final int Quantity;
	public final int Price;
	public final float TimeDisplayed = 0f;
	public final BufferedImage Image;

	public LootListerItem(int _id, String _name, int _quantity, int _price, BufferedImage _image)
	{
		ID = _id;
		ItemText = (_quantity > 1 ? _quantity + " " : "") + _name;
		Quantity = _quantity;
		Price = _price * _quantity;
		Image = _image;
	}
}
