package org.activehome.website;

/*
 * #%L
 * Active Home :: Home
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2016 Active Home Project
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.eclipsesource.json.JsonObject;
import org.activehome.com.Request;
import org.activehome.com.RequestCallback;
import org.activehome.com.ShowIfErrorCallback;
import org.activehome.com.error.Error;
import org.activehome.com.error.ErrorType;
import org.activehome.service.RequestHandler;
import org.activehome.service.Service;
import org.activehome.tools.file.FileHelper;
import org.activehome.tools.file.TypeMime;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Param;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jacky Bourgeois
 * @version %I%, %G%
 */
@ComponentType
public class Website extends Service implements RequestHandler {

    private static String BASE_VCS_URL = "https://raw.githubusercontent.com/jackybourgeois";

    @Param(defaultValue = "Active Home Website!")
    private String description;
    @Param(defaultValue = "/active-home-website")
    private String src;

    public RequestHandler getRequestHandler(Request request) {
        return this;
    }

    public void html(final RequestCallback callback) {
        JsonObject wrap = new JsonObject();
        wrap.add("name", "home-page");
        wrap.add("url", getId() + "/home-page.html");
        wrap.add("title", "Active Home");
        wrap.add("description", "Active Home is a platform for in-home interactive energy management." +
                " The objective is to provide an adaptive system for home environment to support householders" +
                " in the management of their energy. This is a distributed system that can be used for real" +
                " world environment or simulation.");

        JsonObject json = new JsonObject();
        json.add("wrap", wrap);
        callback.success(json);
    }

    public void doc(final RequestCallback callback) {
        JsonObject wrap = new JsonObject();
        wrap.add("name", "active-home-doc");
        wrap.add("url", "/" + getId() + "/active-home-doc.html");
        wrap.add("title", "Active Home - Reference Guide");
        wrap.add("description", "Active Home Reference");

        JsonObject json = new JsonObject();
        json.add("wrap", wrap);
        callback.success(json);
    }

    public void doc(final String url,
                    final RequestCallback callback) {
        try {
            callback.success(getContentFrom(
                    java.net.URLDecoder.decode(url, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            callback.error(new Error(ErrorType.METHOD_ERROR, "Unable to parse URL " + url));
        }
    }

    public Object file(final String str) {
        String content = FileHelper.fileToString(str, getClass().getClassLoader());
        if (str.endsWith(".html")) {
            content = content.replaceAll("\\$\\{id\\}", getId());
        }
        JsonObject json = new JsonObject();
        json.add("content", content);
        json.add("mime", TypeMime.valueOf(str.substring(
                str.lastIndexOf(".") + 1, str.length())).getDesc());
        return json;
    }

    @Override
    public void modelUpdated() {
        if (isFirstModelUpdate()) {
            sendRequest(new Request(getFullId(), getNode() + ".http", getCurrentTime(),
                    "addHandler", new Object[]{"/", getFullId(), false}), new ShowIfErrorCallback());
            sendRequest(new Request(getFullId(), getNode() + ".http", getCurrentTime(),
                    "addHandler", new Object[]{"/home", getFullId(), false}), new ShowIfErrorCallback());
        }
        super.modelUpdated();
    }

    public static HashMap<String, Object> sendGet(final String url,
                                                  final List<String> cookieList) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json");
            if (cookieList != null) {
                for (String cookie : cookieList) {
                    con.addRequestProperty("Cookie", cookie);
                }
            }

            HashMap<String, Object> responseMap = new HashMap<>();

            int responseCode = con.getResponseCode();
            Map<String, List<String>> respHeaderMap = con.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : respHeaderMap.entrySet()) {
                if (entry.getKey() != null && entry.getKey().compareTo("Set-Cookie") == 0)
                    responseMap.put("Set-Cookie", entry.getValue());
            }

            responseMap.put("content", inputStreamToString(con.getInputStream()));

            return responseMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String inputStreamToString(InputStream is) {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        StringBuilder response = new StringBuilder();

        try {
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return response.toString();
    }

    public String getContentFrom(String url) {
        System.out.println("url to check: " + url);
        if (url.startsWith("/")) {
            url = BASE_VCS_URL + url;
        }
        HashMap<String, Object> response = sendGet(url, null);
        if (response != null) {
            System.out.println(response.get("content"));
            return (String) response.get("content");
        }
        return "";
    }
}
