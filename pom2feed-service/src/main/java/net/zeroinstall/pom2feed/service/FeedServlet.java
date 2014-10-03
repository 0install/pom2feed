package net.zeroinstall.pom2feed.service;

import static com.google.common.base.Strings.isNullOrEmpty;
import java.io.*;
import static java.lang.System.getProperty;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.xml.xpath.XPathExpressionException;
import static net.zeroinstall.pom2feed.core.UrlUtils.ensureSlashEnd;
import static net.zeroinstall.publish.FeedUtils.readAll;
import net.zeroinstall.publish.GnuPG;
import org.apache.maven.model.building.ModelBuildingException;
import org.slf4j.*;
import org.xml.sax.SAXException;

/**
 * Responds to HTTP requests and returns Zero Install feeds.
 */
public class FeedServlet
        extends HttpServlet {

    private final static Logger LOGGER = LoggerFactory.getLogger(FeedGenerator.class);
    /**
     * The URL of this service/servlet.
     */
    private final URL serviceURL;
    /**
     * The GnuPG key used to sign feeds.
     */
    private final String gpgKeyData;
    /**
     * The XSL stylesheet for feeds.
     */
    private final String xslData;
    /**
     * The CSS stylesheet for feeds.
     */
    private final String cssData;
    /**
     * Provides Zero Install feeds for specific Maven artifacts.
     */
    private final FeedProvider feedProvider;

    public FeedServlet() throws IOException {
        // Load configuration from Java system properties
        this.serviceURL = ensureSlashEnd(new URL(getProperty("pom2feed-service.serviceURL", "http://maven.0install.net/")));
        LOGGER.info("pom2feed-service.serviceURL=" + serviceURL);
        URL mavenRepository = ensureSlashEnd(new URL(getProperty("pom2feed-service.mavenRepository", "http://repo.maven.apache.org/maven2/")));
        LOGGER.info("pom2feed-service.mavenRepository=" + mavenRepository);
        String gnuPGKey = getProperty("pom2feed-service.gnuPGKey", null);
        LOGGER.info("pom2feed-service.gnuPGKey=" + gnuPGKey);

        // Load files into memory
        this.gpgKeyData = isNullOrEmpty(gnuPGKey) ? null : GnuPG.getPublicKey(gnuPGKey);
        if (isNullOrEmpty(gpgKeyData)) {
            LOGGER.warn("No GnuPG key data loaded!");
        }
        this.xslData = readAll(FeedServlet.class.getResourceAsStream("/feed.xsl"));
        this.cssData = readAll(FeedServlet.class.getResourceAsStream("/feed.css"));

        this.feedProvider = new FeedCache(new FeedGenerator(mavenRepository, serviceURL, gnuPGKey));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = (req.getRequestURI().length() <= req.getContextPath().length())
                ? ""
                : req.getRequestURI().substring(req.getContextPath().length());

        if (path.equals("") || path.equals("/")) {
            respondWelcome(resp);
        } else if (path.endsWith(".gpg")) {
            respondGnuPGKey(resp);
        } else if (path.endsWith("/feed.xsl")) {
            respond(resp, "text/xml", xslData);
        } else if (path.endsWith("/feed.css")) {
            respond(resp, "text/css", cssData);
        } else {
            String artifactPath = path.substring(1);
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
        out.write("<pre>" + serviceURL + "{group-id}/{artifact-id}/</pre>");
        out.write("<p>Replace dots in the group ID with slashes in URL.</p>");
        out.write("</body>");
        out.write("</html>");
    }

    /**
     * Responds with a Zero Install feed.
     */
    private void respondFeed(HttpServletResponse resp, String artifactPath) throws IOException {
        try {
            respond(resp, "application/xml", feedProvider.getFeed(artifactPath));
        } catch (IOException ex) {
            resp.sendError(404, "Not a valid Maven artifact");
        } catch (SAXException ex) {
            resp.sendError(500, "Maven versioning metadata invalid");
        } catch (XPathExpressionException ex) {
            resp.sendError(500, "Maven versioning metadata invalid");
        } catch (ModelBuildingException ex) {
            resp.sendError(500, "Maven project metadata invalid");
        }
    }

    /**
     * Responds with a GnuPG key.
     */
    private void respondGnuPGKey(HttpServletResponse resp) throws IOException {
        if (isNullOrEmpty(gpgKeyData)) {
            resp.sendError(404, "No GnuPG key available");
            return;
        }

        respond(resp, "text/plain", gpgKeyData);
    }

    private void respond(HttpServletResponse resp, String contentType, String data) throws IOException {
        byte[] bytes = data.getBytes();

        resp.setContentType(contentType);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
    }

    /**
     * Responds with an error page.
     */
    private void respondError(HttpServletResponse resp) throws IOException {
        resp.sendError(400, "Not a valid Maven URL");
    }
}
