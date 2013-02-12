package net.zeroinstall.pom2feed.service;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 * Responds to HTTP requests and returns Zero Install feeds.
 */
public class FeedServlet extends HttpServlet {

    private final FeedProvider feedProvider;

    public FeedServlet() {
        this.feedProvider = new FeedCache(new FeedGenerator());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo().equals("/")) {
            respondInfo(resp);
            return;
        }
        String[] artifactPath = req.getPathInfo().substring(1).split("/");
        if (artifactPath.length == 0) {
            repondError(resp);
            return;
        }
        for (String segment : artifactPath) {
            if (!segment.matches("[A-Za-z0-9_\\.-]+")) {
                repondError(resp);
                return;
            }
        }

        String feed = feedProvider.getFeed(artifactPath);
        respondXmlData(resp, feed);
    }

    private void respondInfo(HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.write("<html><head><title>Maven Artifact Zero Install Feed Provider</title></head>");
        out.write("<body>");
        out.write("<h2>Maven Artifact Zero Install Feed Provider</h2>");
        out.write("<p>This web service provides Zero Install feeds for Maven artifacts. Usage:</p>");
        out.write("<pre>{service-uri}/{artifcat-group}/{artifact-id}</pre>");
        out.write("<p>Replace dots with slashes in URI.</p>");
        out.write("</body>");
        out.write("</html>");
    }

    private void repondError(HttpServletResponse resp) throws IOException {
        resp.sendError(400, "Not a valid Maven URI");
    }

    private void respondXmlData(HttpServletResponse resp, String data) throws IOException {
        resp.setContentType("text/xml");
        resp.getWriter().write(data);
    }
}
