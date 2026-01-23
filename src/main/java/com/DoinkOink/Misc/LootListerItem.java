package com.DoinkOink.Misc;

import com.DoinkOink.LootListerConfig;
import net.runelite.api.NPCComposition;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class LootListerItem
{
	public final int ID;
	public final String SourceName;
	public final String SourceID;
	public final String ItemText;
	public final int Quantity;
	public final int Price;
	public final BufferedImage Image;

	public Point CurrentPosition = new Point(0,0);
	public Point NextPosition = new Point(0,0);
	public Point StartingPosition = new Point(0, 0);
	public int TextWidth = 0;
	public double TicksDisplayed = 0;
	public boolean JustAdded = true;
	public boolean IsTextMoving = false;
	public boolean MarkedForDelete = false;

	public LootListerItem(int _id, NPCComposition _npc, String _name, int _quantity, int _price, BufferedImage _image)
	{
		ID = _id;
		ItemText = (_quantity > 1 ? _quantity + " " : "") + _name;
		Quantity = _quantity;
		Price = _price * _quantity;
		Image = _image;

		if (_npc != null) {
			SourceName = _npc.getName().toLowerCase();
			SourceID = Integer.toString(_npc.getId());
		} else {
			SourceName = "";
			SourceID = "";
		}
	}

	public void UpdatePosition(LootListerConfig config, boolean isRemoving)
	{
		final int _verticalScrollMod = config.stackDirection() == LootListerStackSide.BOTTOM ? -1 : 1;
		int _horizontalScrollMod = config.sideToAnimateFrom() == LootListerSide.RIGHT ? -1 : 1;

		if (isRemoving)
			_horizontalScrollMod = _horizontalScrollMod == -1 ? 1 : -1;

		// Preemptively set the text to not moving as it will be set true later if it is moving.
		IsTextMoving = false;

		// Vertically animate the text if the CurrentPosition isn't the NextPosition
		if (GetRemainingVerticalDistance() < config.verticalTextSpeed()) {
			CurrentPosition.y = NextPosition.y;
		} else if (GetRemainingVerticalDistance() > 0) {
			CurrentPosition.y += config.verticalTextSpeed() * _verticalScrollMod;
			IsTextMoving = true;
		}

		// Horizontally animate the text if the CurrentPosition isn't the NextPosition
		if (GetRemainingHorizontalDistance() < config.horizontalTextSpeed()) {
			CurrentPosition.x = NextPosition.x;

			if (isRemoving)
				MarkedForDelete = true;
			else
				JustAdded = false;
		} else if (GetRemainingHorizontalDistance() > 0) {
			CurrentPosition.x += config.horizontalTextSpeed() * _horizontalScrollMod;
			IsTextMoving = true;
		}
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
	private double GetRemainingVerticalDistance()
	{
		return StartingPosition.y < NextPosition.y
			? NextPosition.getY() - CurrentPosition.getY()
			: CurrentPosition.getY() - NextPosition.getY();
	}

	private double GetTotalVerticalDistance()
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
