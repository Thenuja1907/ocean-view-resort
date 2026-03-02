package com.oceanview.util;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Simple embedded Jetty starter for local development.
 * Run with: mvn compile exec:java -Dexec.mainClass=com.oceanview.util.ServerStarter
 */
public class ServerStarter {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        String webappDir = "src/main/webapp";

        Server server = new Server(port);

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setDescriptor(webappDir + "/WEB-INF/web.xml");
        context.setResourceBase(webappDir);
        context.setParentLoaderPriority(true);

        server.setHandler(context);

        server.start();
        System.out.println("Server started: http://localhost:" + port + "/");
        server.join();
    }
}
