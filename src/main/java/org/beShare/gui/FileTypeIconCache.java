package org.beShare.gui;

import javax.swing.ImageIcon;
import java.util.HashMap;

/**
 * Created by bvarner on 9/2/14.
 */
public class FileTypeIconCache {
	private static HashMap<String, ImageIcon> iconCache = new HashMap<>();

	public static ImageIcon getIcon(final String kind) {
		ImageIcon fileIcon;
		if (!iconCache.containsKey(kind)) {
			if ("".equals(kind)) {
				// Load the generic file icon.
				fileIcon = new ImageIcon(FileTypeIconCache.class.getClassLoader().getResource("Images/fileicons/notype.gif"));
				iconCache.put(kind, fileIcon);
			} else {
				// Replace / with ^ and . with &
				String fileName = kind;
				fileName = fileName.replace('/', '^');
				fileName = fileName.replace('.', '&');
				fileName = fileName.concat(".gif");
				try {
					fileIcon =
							new ImageIcon(FileTypeIconCache.class.getClassLoader().getResource("Images/fileicons/" + fileName));
					iconCache.put(kind, fileIcon);
				} catch (NullPointerException npe) {
					fileIcon =
							new ImageIcon(FileTypeIconCache.class.getClassLoader().getResource("Images/fileicons/notype.gif"));
					iconCache.put(kind, fileIcon);
				}
			}
		} else {
			fileIcon = iconCache.get(kind);
		}
		return fileIcon;
	}
}
