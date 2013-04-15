package net.zeroinstall.pom2feed.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.apache.maven.model.building.ModelBuildingException;
import org.xml.sax.SAXException;

/**
 * Responds to HTTP requests and returns Zero Install feeds.
 */
public class FeedServlet extends HttpServlet {

    /**
     * The base URL of the Maven repository used to provide binaries.
     */
    private final URL mavenRepository;
    /**
     * The base URL of the pom2feed service used to provide dependencies. This
     * is usually the URL of this service itself.
     */
    private final URL pom2feedService;
    private final FeedProvider feedProvider;

    public FeedServlet() throws MalformedURLException {
        // TODO: Make configurable
        this.mavenRepository = new URL("http://repo.maven.apache.org/maven2/");
        this.pom2feedService = new URL("http://0install.de/maven/");

        this.feedProvider = new FeedCache(new FeedGenerator(mavenRepository, pom2feedService));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo().equals("/")) {
            respondWelcome(resp);
            return;
        }

        if (req.getPathInfo().endsWith(".gpg")) {
            respondGnuPGKey(resp);
        } else if (req.getPathInfo().endsWith("/interface.xsd")) {
            respondXSD(resp);
        } else if (req.getPathInfo().endsWith("/interface.css")) {
            respondCSS(resp);
        } else {
            String artifactPath = req.getPathInfo().substring(1);
            if (ArtifactUtils.validatePath(artifactPath)) {
                respondFeed(resp, artifactPath);
            } else {
                respondError(resp);
            }
        }
    }

    /**
     * Responds with a human-readable welcome page.
     */
    private void respondWelcome(HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.write("<html><head><title>Maven Artifact Zero Install Feed Provider</title></head>");
        out.write("<body>");
        out.write("<h2>Maven Artifact Zero Install Feed Provider</h2>");
        out.write("<p>This web service provides Zero Install feeds for Maven artifacts. Usage:</p>");
        out.write("<pre>" + pom2feedService + "{group-id}/{artifact-id}/</pre>");
        out.write("<p>Replace dots in the group and artifact ID with slashes in URL.</p>");
        out.write("</body>");
        out.write("</html>");
    }

    /**
     * Responds with an error page.
     */
    private void respondError(HttpServletResponse resp) throws IOException {
        resp.sendError(400, "Not a valid Maven URL");
    }

    /**
     * Responds with a Zero Install feed.
     */
    private void respondFeed(HttpServletResponse resp, String artifactPath) throws IOException {
        resp.setContentType("application/xml");
        resp.setCharacterEncoding("utf-8");
        try {
            resp.getWriter().write(feedProvider.getFeed(artifactPath));
        } catch (SAXException ex) {
            throw new IOException(ex);
        } catch (ModelBuildingException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Responds with a GnuPG key.
     */
    private void respondGnuPGKey(HttpServletResponse resp) {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("utf-8");
        // TODO: resp.getWriter().write(data);
    }

    /**
     * Responds with an XSD stylesheet.
     */
    private void respondXSD(HttpServletResponse resp) {
        resp.setContentType("text/xml");
        resp.setCharacterEncoding("utf-8");
        // TODO: resp.getWriter().write(data);
    }

    /**
     * Responds with a CSS stylesheet.
     */
    private void respondCSS(HttpServletResponse resp) {
        resp.setContentType("text/css");
        resp.setCharacterEncoding("utf-8");
        // TODO: resp.getWriter().write(data);
    }
}
