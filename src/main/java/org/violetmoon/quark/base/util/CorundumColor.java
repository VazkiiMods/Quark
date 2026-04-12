package org.violetmoon.quark.base.util;

import net.minecraft.world.level.material.MapColor;

public enum CorundumColor {
	RED("red", 0xff0000, MapColor.COLOR_RED),
	ORANGE("orange", 0xff8000, MapColor.COLOR_ORANGE),
	YELLOW("yellow", 0xffff00, MapColor.COLOR_YELLOW),
	GREEN("green", 0x00ff00, MapColor.COLOR_GREEN),
	BLUE("blue", 0x00ffff, MapColor.COLOR_LIGHT_BLUE),
	INDIGO("indigo", 0x0000ff, MapColor.COLOR_BLUE),
	VIOLET("violet", 0xff00ff, MapColor.COLOR_MAGENTA),
	WHITE("white", 0xffffff, MapColor.SNOW),
	BLACK("black", 0x000000, MapColor.COLOR_BLACK);

	public final String name;
	public final int beaconColor;
	public final MapColor mapColor;

	CorundumColor(String name, int beaconColor, MapColor mapColor) {
		this.name = name;
		this.beaconColor = beaconColor;
		this.mapColor = mapColor;
	}
}
