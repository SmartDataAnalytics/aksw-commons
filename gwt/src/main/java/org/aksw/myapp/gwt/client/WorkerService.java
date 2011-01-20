package org.aksw.myapp.gwt.client;


import com.google.gwt.user.client.rpc.RemoteService;

public interface WorkerService extends RemoteService {


    /**
     * An arbitrary test function
     *
     * @return
     */
    public String test();


    /**
     * testing the logger
     * @return
     */

    public String testLog();

    /**
     * testing file paths
     * @return
     */
    public String testFile();


}