package littleware.asset.client.internal;

import com.google.gson.Gson;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.gson.LittleGsonFactory;
import littleware.base.Whatever;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

/**
 * Little helper class to wrap up Apache httpclient interaction
 */
public class HttpHelper {

    private static final Logger log = Logger.getLogger(HttpHelper.class.getName());
    private final HttpClient httpClient;
    private final LittleGsonFactory gsonFactory;

    public static class Result {

        private final String content;
        private final StatusLine status;

        public Result(String content, StatusLine status) {
            this.content = content;
            this.status = status;
        }

        public String getContent() {
            return content;
        }

        public StatusLine getStatus() {
            return status;
        }
        
        @Override
        public String toString() { 
            return status.toString() + ": " + content;
        }
    }

    public static class JsonResult {

        private final Result result;
        private final Object jsContent;

        public JsonResult(Result result, Object jsContent) {
            this.result = result;
            this.jsContent = jsContent;
            if ( null == jsContent ) {
                throw new IllegalArgumentException( "Attempt to set NULL JsonResult from content: " + result );
            }
        }
        
        
        public Result getHttpResult() {
            return result;
        }

        @SuppressWarnings("unchecked")
        public <T> T getJsContent() {
            return (T) jsContent;
        }
    }

    @Inject
    public HttpHelper(HttpClient httpClient, LittleGsonFactory gsonFactory) {
        this.httpClient = httpClient;
        this.gsonFactory = gsonFactory;
    }

    /**
     * Get the URL contents as a String (UTF8)
     *
     * @param url
     * @return
     * @throws IOException
     */
    public Result get(java.net.URL url) throws IOException {
        final HttpGet getMethod = new HttpGet(url.toString());
        try {
            final HttpResponse response = httpClient.execute(getMethod);
            final HttpEntity entity = response.getEntity();
            if (null != entity) {
                try {
                    final int status = response.getStatusLine().getStatusCode();
                    final Reader reader = new InputStreamReader(entity.getContent(), Whatever.UTF8);
                    final String content = com.google.common.io.CharStreams.toString(reader);
                    final Result result = new Result(content, response.getStatusLine());
                    if (status == javax.servlet.http.HttpServletResponse.SC_OK) {
                        return result;
                    } else {
                        log.log(Level.FINE, "Failed to access {0}, got response status: {1}: {2}",
                                new Object[]{url.toString(), response.getStatusLine().toString(), content});
                        return result;
                    }
                } finally {
                    org.apache.http.util.EntityUtils.consume(entity);
                }
            } else {
                return new Result("", response.getStatusLine());
            }
        } catch (RuntimeException ex) {
            log.log(Level.WARNING, "HTTP request failed", ex);
            getMethod.abort();
            throw ex;
        }
    }

    public JsonResult getFromJSON(java.net.URL url, Type t) throws IOException {
        final Gson gtool = gsonFactory.get();
        final Result result = get( url );
        return new JsonResult( result, gtool.fromJson( result.getContent(), t ) );
    }
}
