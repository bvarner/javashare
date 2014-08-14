package org.beShare.gui.text;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * StyledString is a wrapper around a LinkedHashMap, which retains object insertion order.
 *
 * @author Bryan Varner
 */

public class StyledString extends LinkedHashMap<String, SimpleAttributeSet> {
	public static final Map<String, SimpleAttributeSet> KEYWORD_STYLES =
			Collections.synchronizedMap(new HashMap<String, SimpleAttributeSet>());

	public static final SimpleAttributeSet PLAIN = new SimpleAttributeSet();
	public static final SimpleAttributeSet LOCAL = new SimpleAttributeSet();
	public static final SimpleAttributeSet SYSTEM_MESSAGE = new SimpleAttributeSet();
	public static final SimpleAttributeSet REMOTE_USER = new SimpleAttributeSet();
	public static final SimpleAttributeSet USER_MENTIONED = new SimpleAttributeSet();
	public static final SimpleAttributeSet USER_ACTION = new SimpleAttributeSet();
	public static final SimpleAttributeSet PRIVATE = new SimpleAttributeSet();
	public static final SimpleAttributeSet SYSTEM_ERROR = new SimpleAttributeSet();
	public static final SimpleAttributeSet WATCH_PATTERN = new SimpleAttributeSet();
	public static final SimpleAttributeSet URI = new SimpleAttributeSet();

	static {
		StyleConstants.setBold(PLAIN, false);
		StyleConstants.setItalic(PLAIN, false);
		StyleConstants.setForeground(PLAIN, new Color(0, 0, 0));

		StyleConstants.setForeground(USER_MENTIONED, new Color(255, 128, 0));

		StyleConstants.setBold(SYSTEM_MESSAGE, true);
		StyleConstants.setForeground(SYSTEM_MESSAGE, new Color(0, 0, 128));

		StyleConstants.setBold(REMOTE_USER, true);
		StyleConstants.setForeground(REMOTE_USER, new Color(0, 0, 0));

		StyleConstants.setForeground(LOCAL, new Color(0, 128, 0));

		StyleConstants.setForeground(USER_ACTION, new Color(128, 0, 128));

		StyleConstants.setForeground(PRIVATE, new Color(0, 128, 128));

		StyleConstants.setForeground(SYSTEM_ERROR, new Color(255, 60, 0));
		StyleConstants.setForeground(WATCH_PATTERN, new Color(128, 0, 0));

		StyleConstants.setForeground(URI, new Color(0, 0, 255));
		StyleConstants.setUnderline(URI, true);

		KEYWORD_STYLES.put(".*http://.*", URI);
		KEYWORD_STYLES.put(".*beshare:.*", URI);
		KEYWORD_STYLES.put(".*audio://.*", URI);
	}

	/**
	 * Constructs a StyledString given the text run (to parse for keywords) and a default style for the message.
	 *
	 * @param text
	 * @param defaultStyle
	 */
	public StyledString(String text, final SimpleAttributeSet defaultStyle) {
		super();
		append(text, defaultStyle);
	}

	/**
	 * Appends the given text in a PLAIN format.
	 *
	 * @param text
	 * @return This, for chaining.
	 */
	public StyledString append(final String text) {
		return append(text, PLAIN);
	}

	/**
	 * Appends the given text with the default given format. Keywords are given the proper treatment.
	 *
	 * @param text
	 * @param defaultStyle
	 * @return This, for chaining.
	 */
	public StyledString append(final String text, SimpleAttributeSet defaultStyle) {
		boolean containsKeyword = false;
		for (String keyword : KEYWORD_STYLES.keySet()) {
			if (text.matches(keyword)) {
				containsKeyword = true;
				break;
			}
		}

		// Shortcut if we can by putting the entire run in one style.
		if (!containsKeyword) {
			put(text, defaultStyle);
		} else {
			// Begin the long process of tokenizing and determining where to colorize.
			String[] tokens = text.split("\\s+");

			for (int i = 0; i < tokens.length; i++) {
				boolean keywordMatched = false;

				for (Map.Entry<String, SimpleAttributeSet> keyword : KEYWORD_STYLES.entrySet()) {
					if (tokens[i].matches(keyword.getKey())) {
						// Add the current token to the string with the proper style.
						put(tokens[i], keyword.getValue());
						keywordMatched = true;

						// URL styles use special handling.
						// To make it easier for the Document to parse the StyledString, we always add two URI
						// entries. One for the URI, one for the label.
						// If there is no explicit label, we create one using the URI and duplicate the data.
						if (keyword.getValue().equals(URI)) {
							int labelend = i;
							int labelstart = i + 1;

							// Does the next token (if there is one) start with a '['?
							if ((labelstart < tokens.length) && tokens[labelstart].startsWith("[")) {
								for (int j = labelstart; j < tokens.length; j++) {
									if (tokens[j].endsWith("]")) {
										labelend = j;
										break;
									}
								}
							} else {
								labelend = i;
							}

							// No end of label found. Duplicate the URI text as the label.
							if (labelend == i) {
								put(tokens[i], keyword.getValue());
							} else {
								// Combine all tokens between labelstart and labelend into a single space separated string'
								StringBuilder label = new StringBuilder();
								for (int j = labelstart; j < labelend; j++) {
									label.append(tokens[j]).append(" ");
								}
								// trim excess space, then trim the [] from around the label.
								put(label.toString().trim().substring(1, label.length() - 2), keyword.getValue());

								// Move the current 'i' pointer to labelend, so we'll start with the default style on the next non-label token.
								i = labelend;
							}
						}
						break;
					}
				}

				if (!keywordMatched) {
					if (i + 1 < tokens.length) {
						put(tokens[i] + " ", defaultStyle);
					} else {
						put(tokens[i], defaultStyle);
					}
				}
			}
		}
		return this;
	}
}
