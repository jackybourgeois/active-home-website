package org.activehome.service.home;

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
import org.activehome.com.error.Error;
import org.activehome.service.RequestHandler;
import org.activehome.service.Service;
import org.activehome.tools.file.FileHelper;
import org.activehome.tools.file.TypeMime;
import org.kevoree.annotation.ComponentType;

import java.util.Date;

/**
 * @author Jacky Bourgeois
 * @version %I%, %G%
 */
@ComponentType
public class Home extends Service implements RequestHandler {

    public RequestHandler getRequestHandler(Request request) {
        return this;
    }

    public void html(RequestCallback callback) {
        // ask to WebSocket component (ws) to send/redirect message, it sends back the address and port
        sendRequest(new Request(getFullId(),"ah.ws",getCurrentTime(),"getURI"), new RequestCallback() {
            public void success(Object wsURI) {
                sendRequest(new Request(getFullId(), getNode() + ".http",
                        getCurrentTime(),"getURI"), new RequestCallback() {
                    public void success(Object httpURI) {
                        String content = FileHelper.fileToString("home.html", getClass().getClassLoader());
                        content = content.replaceAll("\\$\\{id\\}", getId());
                        content = content.replaceAll("\\$\\{\\?t=\\}","?t="+new Date().getTime());
                        content = content.replaceAll("\\$\\{wsURI\\}", wsURI + "");
                        content = content.replaceAll("\\$\\{url\\}", httpURI + "");
                        JsonObject json = new JsonObject();
                        json.add("content", content);
                        json.add("mime", "text/html");
                        callback.success(json);
                    }
                    public void error(Error result) { callback.error(result); }
                });
            }
            public void error(Error result) { callback.error(result); }
        });
    }

    public Object file(String str) {
        String content = FileHelper.fileToString(str, getClass().getClassLoader());
        if (str.compareTo("home.html")==0) {
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
                    "addHandler", new Object[]{"/", getFullId(), false}), null);
        }
        super.modelUpdated();
    }
}
