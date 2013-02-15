package net.zeroinstall.pom2feed.service;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 * Responds to HTTP requests and returns Zero Install feeds.
 */
public class FeedServlet extends HttpServlet {

    private final FeedProvider feedProvider = new FeedCache(new FeedGenerator());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo().equals("/")) {
            respondWelcome(resp);
            return;
        }

        String artifactPath = req.getPathInfo().substring(1);
        if (ArtifactUtils.validatePath(artifactPath)) {
            respondXmlData(resp, feedProvider.getFeed(artifactPath));
        } else {
            respondError(resp);
        }
    }

    /**
     * Responds with a human-readable welcocme page.
     */
    private void respondWelcome(HttpServletResponse resp) throws IOException {
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

    /**
     * Responds with an error page.
     */
    private void respondError(HttpServletResponse resp) throws IOException {
        resp.sendError(400, "Not a valid Maven URI");
    }

    /**
     * Responds with XML data.
     */
    private void respondXmlData(HttpServletResponse resp, String data) throws IOException {
        resp.setContentType("application/xml");
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().write(data);
    }
}
