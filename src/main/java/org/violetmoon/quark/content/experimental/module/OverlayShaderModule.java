package org.violetmoon.quark.content.experimental.module;

import org.violetmoon.quark.base.config.Config;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "experimental", enabledByDefault = false)
public class OverlayShaderModule extends ZetaModule {
	
	@Config(description = "Sets the name of the shader to load on a regular basis. This can load any shader the Camera module can (and requires the Camera module enabled to apply said logic).\n"
			+ "Some useful shaders include 'desaturate', 'oversaturate', 'bumpy'\n"
			+ "Colorblind simulation shaders are available in the form of 'deuteranopia', 'protanopia', 'tritanopia', and 'achromatopsia'")
	public String shader = "none";
	
}
