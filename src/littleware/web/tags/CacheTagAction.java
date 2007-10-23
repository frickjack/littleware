package littleware.web.tags;

import java.util.Date;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Code for JSP cache-tag stolen from onjava.com
 *    <a href="http://www.onjava.com/pub/a/onjava/2005/01/05/jspcache.html"> 
 *          http://www.onjava.com/pub/a/onjava/2005/01/05/jspcache.html
 *    </a>
 * , and customized.
 */
public class CacheTagAction extends SimpleTagSupport {
    private String  os_id = null;
    private int     oi_scope = PageContext.APPLICATION_SCOPE;
    private int     oi_ageout_secs = 30;
 
    /** Do nothing */
    public CacheTagAction () {
    }

    /**
     * Stash the name of the variable under which to stash
     * our cache.
     */
    public void setId(String s_id) {
        os_id = s_id;
    }

    /** Default to APPLICATION */
    public void setScope(String s_scope) {
        oi_scope = JspUtils.checkScope( s_scope );
    }
    
    /** 
     * Set timeout on cache - default to 30 secs
     *
     * @param i_ageout_secs in seconds
     */
    public void setAgeoutSecs ( int i_ageout_secs ) {
        oi_ageout_secs = i_ageout_secs;
    }

    /**
     * Little cache-entry data bucket
     */
    public static class CacheData {
        private String os_data = null;
        private Date   ot_update = new Date ();
        
        /**
         * Constructor initializes the cache-data,
         * and sets the last-update to t_now
         *
         * @param s_data to cache
         */
        public CacheData ( String s_data ) {
            os_data = s_data;
        }
        
        /**
         * Update the data in the cache, and reset last update time to t_now
         */
        public void setData ( String s_data ) {
            os_data = s_data;
            ot_update = new Date ();
        }
        
        /** Get the data in the cache         */
        public String getData () { return os_data; }
        
        /**
         * When was the data last updated ?
         */
        public Date getLastUpdate () { return ot_update; }
    }
    
    public void doTag() throws JspException, IOException {
        PageContext jsp_context = (PageContext) getJspContext();
        ServletContext jsp_application = jsp_context.getServletContext();
        CacheData cache_data  = (CacheData) jsp_context.getAttribute( os_id, oi_scope );
        Date      t_now = new Date ();
        
        if ( (cache_data == null) 
             || (cache_data.getLastUpdate ().getTime () + (oi_ageout_secs*1000) < t_now.getTime ())
             ) {
            StringWriter write_buffer = new StringWriter();
            getJspBody().invoke( write_buffer );
            if ( null == cache_data ) {
                cache_data = new CacheData ( write_buffer.toString () );
                jsp_context.setAttribute( os_id, cache_data, oi_scope );
            } else {
                cache_data.setData ( write_buffer.toString () );
            }
        }
        
        jsp_context.getOut().print( cache_data.getData () );
    }


}
