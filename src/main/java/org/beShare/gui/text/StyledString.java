package org.beShare.gui.text;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * StyledString is a wrapper around a LinkedHashMap, which retains object insertion order.
 *
 * @author Bryan Varner
 */

public class StyledString extends LinkedHashMap<String, SimpleAttributeSet> {
	public static final SimpleAttributeSet PLAIN = new SimpleAttributeSet();
	public static final SimpleAttributeSet LOCAL = new SimpleAttributeSet();
	public static final SimpleAttributeSet SYSTEM_MESSAGE = new SimpleAttributeSet();
	public static final SimpleAttributeSet REMOTE_USER = new SimpleAttributeSet();
	public static final SimpleAttributeSet LOCAL_USER_MENTIONED = new SimpleAttributeSet();
	public static final SimpleAttributeSet USER_ACTION = new SimpleAttributeSet();
	public static final SimpleAttributeSet PRIVATE = new SimpleAttributeSet();
	public static final SimpleAttributeSet SYSTEM_ERROR = new SimpleAttributeSet();
	public static final SimpleAttributeSet WATCH_PATTERN = new SimpleAttributeSet();
	public static final SimpleAttributeSet URI = new SimpleAttributeSet();

	public static final String USERNAME_PATTERN_NAME = "Local_UserName";

	private static final Map<StyleKey, SimpleAttributeSet> KEYWORD_STYLES =
			Collections.synchronizedMap(new HashMap<StyleKey, SimpleAttributeSet>());
	private static final String HTTP_PATTERN_NAME = "HTTP";
	private static final String AUDIO_PATTERN_NAME = "AUDIO";
	private static final String SHARE_PATTERN_NAME = "SHARE";


	static {
		StyleConstants.setBold(PLAIN, false);
		StyleConstants.setItalic(PLAIN, false);
		StyleConstants.setForeground(PLAIN, new Color(0, 0, 0));

		StyleConstants.setForeground(LOCAL_USER_MENTIONED, new Color(255, 128, 0));

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

		KEYWORD_STYLES.put(new StyleKey(HTTP_PATTERN_NAME, ".*http://.*", false), URI);
		KEYWORD_STYLES.put(new StyleKey(SHARE_PATTERN_NAME, ".*beshare:.*", false), URI);
		KEYWORD_STYLES.put(new StyleKey(AUDIO_PATTERN_NAME, ".*audio://.*", false), URI);
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
	 * Adds system-provided pattern matching.
	 *
	 * @param name
	 * @param regex
	 * @param style
	 */
	public static void addSystemPattern(final String name, final String regex, final SimpleAttributeSet style) {
		KEYWORD_STYLES.put(new StyleKey(name, regex, false), style);
	}

	/**
	 * Adds User-Entered pattern matching.
	 *
	 * @param name
	 * @param regex
	 * @param style
	 */
	public static void addUserPattern(final String name, final String regex, final SimpleAttributeSet style) {
		KEYWORD_STYLES.put(new StyleKey(name, regex, true), style);
	}

	/**
	 * Removes a patterns from the KEYWORD_STYLES.
	 *
	 * @param name
	 * @return
	 */
	public static boolean removePattern(final String name) {
		return KEYWORD_STYLES.remove(new StyleKey(name)) != null;
	}

	/**
	 * Gets a list of all the user-added patterns.
	 *
	 * @return
	 */
	public static List<String> getUserPatterns() {
		List<String> userEntered = new ArrayList<>();
		for (StyleKey key : KEYWORD_STYLES.keySet()) {
			if (key.userAdded) {
				userEntered.add(key.description);
			}
		}
		return userEntered;
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

		if (!defaultStyle.equals(LOCAL)) { // Don't do keyword matching against Local Prefix text.
			for (StyleKey key : KEYWORD_STYLES.keySet()) {
				if (key.matches(text)) {
					containsKeyword = true;
					break;
				}
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

				for (Map.Entry<StyleKey, SimpleAttributeSet> entry : KEYWORD_STYLES.entrySet()) {
					if (entry.getKey().matches(tokens[i])) {
						// Add the current token to the string with the proper style.
						keywordMatched = true;
						if (i + 1 < tokens.length) {
							put(tokens[i] + " ", entry.getValue());
						} else {
							put(tokens[i], entry.getValue());
						}

						// URL styles use special handling.
						// To make it easier for the Document to parse the StyledString, we always add two URI
						// entries. One for the URI, one for the label.
						// If there is no explicit label, we create one using the URI and duplicate the data.
						if (entry.getValue().equals(URI)) {
							int labelend = i;
							int labelstart = i + 1;

							// Does the next token (if there is one) start with a '['?
							if ((labelstart < tokens.length) && tokens[labelstart].startsWith("[")) {
								for (int j = labelstart; j < tokens.length; j++) {
									if (tokens[j].contains("]")) {
										labelend = j;
										break;
									}
								}
							} else {
								labelend = i;
							}

							// No end of label found. Duplicate the URI text as the label.
							if (labelend == i) {
								put(tokens[i] + " ", entry.getValue());
							} else {
								// Combine all tokens between labelstart and labelend into a single space separated string'
								StringBuilder label = new StringBuilder();
								for (int j = labelstart; j <= labelend; j++) {
									label.append(tokens[j]).append(" ");
								}

								// The string at this point can look like '[foo bar]: this amen'
								// We need to get the bits between '[]' added with the URL style.
								// Then we need to get the rest of them with the default style.
								int endChar = label.indexOf("]");
								put(label.toString().substring(1, endChar), entry.getValue());

								if (endChar < label.length() - 1) {
									put(label.toString().substring(endChar + 1), defaultStyle);
								}
//								put(label.toString().trim().substring(1, label.length() - 2), keyword.getValue());

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

	/**
	 * Describes a StyleKey.
	 */
	private static class StyleKey {
		boolean userAdded;
		private String description;
		private Pattern pattern;

		StyleKey(final String description) {
			this(description, "", false);
		}

		StyleKey(final String description, final String regex, final boolean userAdded) {
			this.description = description;
			this.pattern = Pattern.compile(regex);
			this.userAdded = userAdded;
		}

		boolean matches(final CharSequence string) {
			return pattern.matcher(string).matches();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (obj instanceof StyleKey) {
				return ((StyleKey) obj).description.equals(this.description);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return description.hashCode();
		}
	}
}
