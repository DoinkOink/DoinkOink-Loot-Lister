package com.DoinkOink.Misc;

import net.runelite.api.NPCComposition;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class LootListerItem
{
	public final int ID;
	public NPCComposition npc;
	public final String ItemText;
	public final int Quantity;
	public final int Price;
	public final BufferedImage Image;

	public Point CurrentPosition = new Point(0,0);
	public Point NextPosition = new Point(0,0);
	public Point StartingPosition = new Point(0, 0);
	public int TextWidth = 0;
	public double TimeDisplayed = 0;
	public boolean JustAdded = true;

	public LootListerItem(int _id, NPCComposition _npc, String _name, int _quantity, int _price, BufferedImage _image)
	{
		ID = _id;
		npc = _npc;
		ItemText = (_quantity > 1 ? _quantity + " " : "") + _name;
		Quantity = _quantity;
		Price = _price * _quantity;
		Image = _image;
	}

	public void SetNextPosition(Point _nextPos)
	{
		StartingPosition = new Point(CurrentPosition.x, CurrentPosition.y);
		NextPosition = _nextPos;
	}

	public void SetFirstPosition(Point _start, Point _end)
	{
		CurrentPosition = _start;
		SetNextPosition(_end);
	}

	public void SetHorizontalPosition(int _x)
	{
		CurrentPosition.x = StartingPosition.x = NextPosition.x = _x;
	}

	public double GetRemainingHorizontalDistance()
	{
		return StartingPosition.x < NextPosition.x
			? NextPosition.getX() - CurrentPosition.getX()
			: CurrentPosition.getX() - NextPosition.getX();
	}
	public double GetRemainingVerticalDistance()
	{
		return StartingPosition.y < NextPosition.y
			? NextPosition.getY() - CurrentPosition.getY()
			: CurrentPosition.getY() - NextPosition.getY();
	}

	public double GetTotalVerticalDistance()
	{
		return StartingPosition.y < NextPosition.y
			? NextPosition.getY() - StartingPosition.getY()
			: StartingPosition.getY() - NextPosition.getY();
	}

	public double GetVerticalDistancePercentage()
	{
		return GetRemainingVerticalDistance() / GetTotalVerticalDistance();
	}
}
