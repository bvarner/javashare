package org.beShare.data;

import com.meyer.muscle.message.Message;

/**
 *  Description of the Class
 *
 * @author     Administrator
 * @created    March 8, 2002
 */
public class BeShareDefaultSettings {
	/**
	 *  Description of the Method
	 *
	 * @return    a Message contianing the default settings.
	 */
	public final static Message createDefaultSettings() {
		Message   defaults  = new Message();

        long installID = (((long)(Math.random() * Integer.MAX_VALUE)) << 32)|((long)(Math.random() * Integer.MAX_VALUE));
        defaults.setLong("installid", installID);

        String[]  servers   = {""};
		defaults.setStrings("servers", servers);
		defaults.setInt("curServer", 0);
		String[]  users     = {"Binky"};
		defaults.setStrings("nicks", users);
		defaults.setInt("curNick", 0);
		String[]  status    = {"Here", "Away"};
		defaults.setStrings("status", status);
		defaults.setInt("curStatus", 0);
		defaults.setBoolean("isAway", false);
		defaults.setInt("awayStatus", 1);
		defaults.setBoolean("autoUpdServers", true);
		defaults.setInt("awayTime", 300000);
		defaults.setInt("awayTimeIndex", 2);
		defaults.setBoolean("firewalled", false);
		defaults.setBoolean("autoLogin", false);
		defaults.setBoolean("dispTime", true);
		defaults.setBoolean("dispUser", true);
		defaults.setBoolean("dispUpload", true);
		defaults.setBoolean("dispChat", true);
		defaults.setBoolean("dispPriv", true);
		defaults.setBoolean("dispInfo", true);
		defaults.setBoolean("dispWarn", true);
		defaults.setBoolean("dispError", true);
		defaults.setBoolean("tryNativeLaF", true);
        defaults.setString("soundPack", "Default");
		defaults.setBoolean("sndUName", true);
		defaults.setBoolean("sndWPat", true);
		defaults.setBoolean("prvWSnd", true);
		defaults.setBoolean("prvMSnd", true);
		defaults.setBoolean("miniBrowser", true);
		defaults.setInt("chatDivider", 500);
		
		// Added during File-sharing implementation.
		defaults.setBoolean("fileSharingEnabled", true);
		defaults.setString("downloadPath", System.getProperty("user.dir") + "/Download");
		defaults.setString("sharedPath", System.getProperty("user.dir") + "/Shared");
		defaults.setBoolean("autoShareUpdate", true);
		defaults.setInt("autoShareDelay", 300);
		
		defaults.setInt("transferDivider", 100);
		defaults.setInt("chatTransDivider", 250);
		defaults.setInt("shareUpdateDelay", 120);	// Seconds between shared file updates.
		
		// The default File Extension to Mime-type mapping database!
		String[] extensions = {".html .htm", ".txt", ".rtf", // Text types.
								".gif", ".xbm", ".xpm", ".png", ".jpeg .jpg .jpe", ".tiff .tif", ".g3f", ".pict", ".ppm", ".pgm", ".pbm", ".pnm", ".bmp", ".pcd", //Image Types
								".au .snd", ".aif .aiff .aifc", ".wav", ".mpa .mp2 .mp3", ".mid .midi", ".mod", // Audio Types
								".mpeg .mpg .mpe", ".qt .mov", ".avi", // Video Types
								".eps .ps", ".pdf", ".gtar", ".tar", ".zip", ".hqx", ".sit .sea", ".lha", ".lzx", ".bin .uu", ".exe", ".js .mocha", ".pl", ".so" // Application types.
							  };
		// Text Types
		defaults.setStrings("fileExtensions", extensions);
		defaults.setString(".html .htm", "text/html");
		defaults.setString(".txt", "text/plain");
		defaults.setString(".rtf", "text/richtext");
		defaults.setString(".cpp .h .java .c .cc", "text/x-source-code");
		
		// Image types.
		defaults.setString(".gif", "image/gif");
		defaults.setString(".xbm", "image/x-xbitmap");
		defaults.setString(".xpm", "image/x-xpixmap");
		defaults.setString(".png", "image/x-png");
		defaults.setString(".jpeg .jpg .jpe", "image/jpeg");
		defaults.setString(".tiff .tif", "image/tiff");
		defaults.setString(".g3f", "image/g3fax");
		defaults.setString(".pict", "image/x-pict");
		defaults.setString(".ppm", "image/x-portable-pixmap");
		defaults.setString(".pgm", "image/x-portable-graymap");
		defaults.setString(".pbm", "image/x-portable-bitmap");
		defaults.setString(".pnm", "image/x-portable-anymap");
		defaults.setString(".bmp", "image/x-ms-bmp");
		defaults.setString(".pcd", "image/x-photo-cd");
		
		// AudioTypes.
		defaults.setString(".au .snd", "audio/basic");
		defaults.setString(".aif .aiff .aifc", "audio/x-aiff");
		defaults.setString(".wav", "audio/x-wav");
		defaults.setString(".mpa .mp2 .mp3", "audio/x-mpeg");
		defaults.setString(".mid .midi", "audio/midi");
		defaults.setString(".mod", "audio/x-mod");
		
		// Video Types
		defaults.setString(".mpeg .mpg .mpe", "video/mpeg");
		defaults.setString(".qt .mov", "video/quicktime");
		defaults.setString(".avi", "video/x-msvideo");
		
		//Application Types
		defaults.setString(".eps .ps", "application/postscript");
		defaults.setString(".rtf", "application/rtf");
		defaults.setString(".pdf", "application/pdf");
		defaults.setString(".gtar", "application/x-gtar");
		defaults.setString(".tar", "application/x-tar");
		defaults.setString(".zip", "application/zip");
		defaults.setString(".hqx", "application/mac-binhex40");
		defaults.setString(".sit .sea", "application/x-stuffit");
		defaults.setString(".lha", "application/x-lha");
		defaults.setString(".lzx", "application/x-lzx");
		defaults.setString(".bin .uu", "application/octet-stream");
		defaults.setString(".exe", "application/octet-stream");
		defaults.setString(".js .mocha", "application/x-javascript");
		defaults.setString(".pl", "application/x-perl");
		defaults.setString(".pkg", "application/x-scode-UPkg");
		defaults.setString(".so", "");
		
		// Added during File-sharing implementation.
		defaults.setBoolean("fileSharingEnabled", true);
		defaults.setString("downloadPath", System.getProperty("user.dir") + "/Downloads");
		defaults.setString("sharedPath", System.getProperty("user.dir") + "/Shared");
		defaults.setBoolean("autoShareUpdate", true);
		defaults.setInt("autoShareDelay", 300);
		
		defaults.setInt("transferDivider", 100);
		defaults.setInt("chatTransDivider", 250);
		defaults.setInt("shareUpdateDelay", 120);	// Seconds between shared file updates.

        return defaults;
	}
}

