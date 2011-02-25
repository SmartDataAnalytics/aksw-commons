/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.aksw.myapp.gwt.client;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * HelloWorld application.
 */
public class Application implements EntryPoint {

    // the main connection to the server
    WorkerServiceAsync worker = WorkerServiceAsync.Util.getInstance();
    final VerticalPanel root = new VerticalPanel();




    public void onModuleLoad() {
        RootPanel.get().add(root);
        root.add(test);
        root.add(testLog);
        root.add(testFile);
    }


    protected WorkerServiceAsync getWorkerService() {
        return worker;
    }

     Button test = new Button("Test", new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            worker.test(new AsyncCallback<String>() {
                public void onFailure(Throwable arg0) {
                    Window.alert("failed" + arg0);
                }

                public void onSuccess(String arg0) {
                    Window.alert(arg0);
                }
            });
        }
    });


     Button testFile = new Button("testFile", new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            worker.testFile(new AsyncCallback<String>() {
                public void onFailure(Throwable arg0) {
                    Window.alert("failed" + arg0);
                }

                public void onSuccess(String arg0) {
                    Window.alert(arg0);
                }
            });
        }
    });

    Button testLog = new Button("testLog", new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            worker.testLog(new AsyncCallback<String>() {
                public void onFailure(Throwable arg0) {
                    Window.alert("failed" + arg0);
                }

                public void onSuccess(String arg0) {
                    Window.alert(arg0);
                }
            });
        }
    });

}