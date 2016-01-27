package org.activehome.home;

/*
 * #%L
 * Active Home :: Home
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2016 org.activehome
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
import org.activehome.service.RequestHandler;
import org.activehome.service.Service;
import org.activehome.tools.file.FileHelper;
import org.activehome.tools.file.TypeMime;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Param;

import java.util.Date;

/**
 * @author Jacky Bourgeois
 * @version %I%, %G%
 */
@ComponentType
public class Home extends Service implements RequestHandler {

    @Param(defaultValue = "Active Home Website!")
    private String description;
    @Param(defaultValue = "/activehome-home/master/docs/home.png")
    private String img;
    @Param(defaultValue = "/activehome-home/master/docs/home.md")
    private String doc;
    @Param(defaultValue = "/activehome-home/master/docs/demo.kevs")
    private String demoScript;

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

    public void doc(final String page,
                    final RequestCallback callback) {
        JsonObject wrap = new JsonObject();
        wrap.add("name", page);
        wrap.add("url", getId() + "/" + page + ".html");
        wrap.add("title", page);
        wrap.add("description", "Active Home - " + page);

        JsonObject json = new JsonObject();
        json.add("wrap", wrap);
        callback.success(json);
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
}
