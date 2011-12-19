package org.fern.rest.android;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

public class UriEncoder {
    private static Pattern mUriPattern = Pattern.compile(
            "([a-z](?:[a-z0-9\\+\\-\\.])*)" // connection type
                    + "://" // :// literal
                    + "(?:([a-z0-9]+)@)?" // Optional user
                    + "((?:\\d{1,3}\\.){3}\\d{1,3}|(?:[a-z]+\\.)+(?:[a-z]+))" // Host
                    + "(?::(\\d{1,5}))?" // Optional port
                    + "(?:(/(?:[^\\?#\\.])+)" //Optional path
                    + "(\\.[^\\?#]+)?)?" // Optional file type
                    + "(?:\\?([^#]*))?" // Optional query
                    + "#?(.*)", // Optional fragment
            Pattern.CASE_INSENSITIVE);

    /**
     * Attempts to make a valid URI out of one that needs Url entities like %20
     * for spaces. It has a few issues, I'm sure, and is not meant to be
     * perfect, only do a decent job at fixing somewhat poor URIs
     * 
     * @param uri
     * @return
     * @throws URISyntaxException
     */
    public static URI encode(String uri) throws URISyntaxException {
        Matcher matcher = mUriPattern.matcher(uri);
        if (!matcher.matches()) {
            return null;
        } else {
            String scheme = matcher.group(1);
            String user = matcher.group(2);
            String host = matcher.group(3);
            String port = matcher.group(4);
            String path = matcher.group(5);
            String filetype = matcher.group(6);
            String query = matcher.group(7);
            String fragment = matcher.group(8);

            //if (path != null) path = pathEncode(path);
            if (query != null) query = URLEncoder.encode(query);
            if (fragment != null) fragment = URLEncoder.encode(fragment);
            if (filetype == null) filetype = "";
            int portNum;
            try {
                portNum = TextUtils.isEmpty(port) ? 80 : Integer.parseInt(port);
            } catch (NumberFormatException ex) {
                portNum = 80;
            }
            return new URI(scheme, user, host, portNum, path + filetype, query,
                    fragment);
        }
    }

    public static String pathEncode(String path) {
        StringBuilder outpath = new StringBuilder();
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')
                    || ('0' <= c && c <= '9') || (c == '/')) {
                outpath.append(c);
            } else {
                outpath.append("%").append(Integer.toString(c, 16));
            }
        }
        return outpath.toString();
    }
}
