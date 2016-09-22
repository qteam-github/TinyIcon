package org.qteam.tinyicon;

/**
	Sort enum.<br>
	These {@code constants} are used in the {@link org.qteam.tinyicon.TinyIcon#sortIcons(IconSort, IconSort) sortIcons} method.
*/
public enum IconSort
{
	/** Applies NO sort. */
	NONE,

	/** Sorts icons by image area (from smaller to bigger). */
	BY_RESOLUTION_ASCENDING,

	/** Sorts icons by image area (from bigger to smaller). */
	BY_RESOLUTION_DESCENDING,

	/** Sorts icons by Bits Per Pixel (from smaller to bigger). */
	BY_BPP_ASCENDING,

	/** Sorts icons by Bits Per Pixel (from bigger to smaller). */
	BY_BPP_DESCENDING,

	/** Sorts icons by image compression (from uncompressed to compressed). */
	BY_COMPRESSION_ASCENDING,

	/** Sorts icons by image compression (from compressed to uncompressed). */
	BY_COMPRESSION_DESCENDING
}
