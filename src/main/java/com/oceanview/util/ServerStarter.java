package com.oceanview.util;

/**
 * ServerStarter — attempts to start an embedded Jetty server if Jetty is
 * available on the classpath. If Jetty is not present, prints instructions
 * so the WAR can be deployed to a servlet container instead.
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

        try {
            // Try to load Jetty classes reflectively to avoid a hard compile-time
            // dependency on Jetty (some environments may not provide it).
            Class<?> serverClass = Class.forName("org.eclipse.jetty.server.Server");
            Class<?> webAppCtxClass = Class.forName("org.eclipse.jetty.webapp.WebAppContext");

            Object server = serverClass.getConstructor(int.class).newInstance(port);
            Object context = webAppCtxClass.getConstructor().newInstance();

            webAppCtxClass.getMethod("setContextPath", String.class).invoke(context, "/");
            webAppCtxClass.getMethod("setDescriptor", String.class).invoke(context, webappDir + "/WEB-INF/web.xml");
            webAppCtxClass.getMethod("setResourceBase", String.class).invoke(context, webappDir);
            webAppCtxClass.getMethod("setParentLoaderPriority", boolean.class).invoke(context, true);

            serverClass.getMethod("setHandler", Class.forName("org.eclipse.jetty.server.Handler")).invoke(server, context);
            serverClass.getMethod("start").invoke(server);
            System.out.println("Server started: http://localhost:" + port + "/");
            serverClass.getMethod("join").invoke(server);
        } catch (ClassNotFoundException e) {
            System.out.println("Jetty libraries not found on classpath.");
            System.out.println("Build produced a WAR in target/; deploy it to a servlet container or add Jetty dependencies.");
        }
    }
}
