package littleware.base;

import java.util.logging.Logger;

/**
 * Set of XML special characters that must be
 * encoded/decoded
 */
public enum XmlSpecial {

    lt {

        /** @return "&lt;" */
        public String getEncoding() {
            return "&lt;";
        }

        /** @return '<' */
        public char getChar() {
            return '<';
        }
    },
    gt {

        /** @return "&gt;" */
        public String getEncoding() {
            return "&gt;";
        }

        /** @return '>' */
        public char getChar() {
            return '>';
        }
    },
    apos {

        /** @return "&apos;" */
        public String getEncoding() {
            return "&apos;";
        }

        /** @return '\'' */
        public char getChar() {
            return '\'';
        }
    },
    quot {

        /** @return "&quot;" */
        public String getEncoding() {
            return "&quot;";
        }

        /** @return '"' */
        public char getChar() {
            return '"';
        }
    },
    amp {

        /** @return "&amp;" */
        public String getEncoding() {
            return "&amp;";
        }

        /** @return '&' */
        public char getChar() {
            return '&';
        }
    };

    /** get the XML encoding of the member */
    public abstract String getEncoding();

    /** get the unencoded character */
    public abstract char getChar();
    private static Logger olog_generic = Logger.getLogger("littleware.base.XmlSpecial");

    /**
     * Check whether the given character matches one
     * of the XmlSpecial getChar values via a fast switch.
     * Return the match or null if no match
     *
     * @param c_in character to check if it is special
     * @return XmlSpecial such that getChar() == c_in
     */
    public static XmlSpecial encode(char c_in) {
        switch (c_in) {
            case '<': {
                return lt;
            }
            case '>': {
                return gt;
            }
            case '\'': {
                return apos;
            }
            case '"': {
                return quot;
            }
            case '&': {
                return amp;
            }
            default: {
                return null;
            }
        }
    }

    /**
     * Scan the given CharSequence, and return a String
     * that replaces each occurence of an unescaped XmlSpecial.getChar()
     * character with the corresponding XmlSpecial.getEncoding() encoding string.
     *
     * @param v_scan to scan
     * @return string with special characters encoded
     */
    public static String encode(CharSequence v_scan) {
        int i_length = v_scan.length();
        int i_first_special = -1;

        for (int i = 0; (i < i_length) && (i_first_special == -1); ++i) {
            XmlSpecial n_special = encode(v_scan.charAt(i));
            if (null != n_special) {
                i_first_special = i;
            }
        }
        if (i_first_special > -1) {
            StringBuilder s_result = new StringBuilder(i_length * 2);
            s_result.append(v_scan.subSequence(0, i_first_special));
            for (int i = i_first_special; i < i_length; ++i) {
                XmlSpecial n_special = encode(v_scan.charAt(i));
                if (null == n_special) {
                    s_result.append(v_scan.charAt(i));
                } else {
                    s_result.append(n_special.getEncoding());
                }
            }
            return s_result.toString();
        } else {
            return v_scan.toString();
        }
    }

    /**
     * Check whether the CharSequence beginning at v_char[i_start]
     * matches one of the XmlSpecial.getEncoding() encodings,
     * and return the XmlSpecial value if there is a match
     *
     * @param v_char character array
     * @param i_start index into v_char to check for match against
     */
    private static XmlSpecial decode(CharSequence v_char, int i_start) {
        if ((v_char.charAt(i_start) != '&') || (v_char.length() < i_start + 4)) {
            return null;
        }

        switch (v_char.charAt(i_start + 1)) {
            case 'l':
                 {
                    if ((v_char.charAt(i_start + 2) == 't') && v_char.charAt(i_start + 3) == ';') {
                        return lt;
                    }
                }
                break;
            case 'g':
                 {
                    if ((v_char.charAt(i_start + 2) == 't') && v_char.charAt(i_start + 3) == ';') {
                        return gt;
                    }
                }
                break;
            case 'q':
                 {
                    if (v_char.length() > i_start + 5 && (v_char.charAt(i_start + 2) == 'u') && (v_char.charAt(i_start + 3) == 'o') && (v_char.charAt(i_start + 4) == 't') && v_char.charAt(i_start + 5) == ';') {
                        return quot;
                    }
                }
                break;
            case 'a': {
                switch (v_char.charAt(i_start + 2)) {
                    case 'm':
                         {
                            if (v_char.length() > i_start + 4 && v_char.charAt(i_start + 3) == 'p' && v_char.charAt(i_start + 4) == ';') {
                                return amp;
                            }
                        }
                        break;
                    case 'p':
                         {
                            if (v_char.length() > i_start + 5 && v_char.charAt(i_start + 3) == 'o' && v_char.charAt(i_start + 4) == 's' && v_char.charAt(i_start + 5) == ';') {
                                return apos;
                            }
                        }
                        break;
                }
            }
        }
        return null;
    }

    /**
     * Scan the given input string, and return a string
     * with every occurence
     * of an XmlSpecial encoding sequece "&bla;"
     * with the corresponding XmlSpecial unencoded character.
     */
    public static String decode(CharSequence v_char) {
        int i_length = v_char.length();
        int i_last_semi = 0;

        // Find the last semicolon in the CharSequence
        for (int i = i_length - 1; (i > 0) && (0 == i_last_semi); --i) {
            if (v_char.charAt(i) == ';') {
                i_last_semi = i;
            }
        }

        if (i_last_semi > 2) {
            StringBuilder s_result = new StringBuilder(i_length);
            for (int i = 0; i < i_last_semi; ++i) {
                XmlSpecial n_special = decode(v_char, i);
                if (null == n_special) {
                    s_result.append(v_char.charAt(i));
                } else {
                    s_result.append(n_special.getChar());
                    // skip over the rest of the encoding string
                    i += n_special.getEncoding().length() - 1;
                }
            }
            s_result.append(v_char.subSequence(i_last_semi + 1, i_length));
            return s_result.toString();
        } else {
            return v_char.toString();
        }
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

