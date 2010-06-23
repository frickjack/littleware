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
 * Simple tag that simply returns everything between begin/end
 * inclusive.
 */
public class FilterTagAction extends SimpleTagSupport {
    private String  os_begin = null;
    private String  os_end   = null;
    
    /** Do nothing */
    public FilterTagAction () {
    }
    
    /**
     * Set the begin-token
     *
     * @param s_begin
     */
    public void setBegin( String s_begin ) {
        os_begin = s_begin;
    }

    /**
     * Set the end-token
     *
     * @param s_end
     */
    public void setEnd( String s_end ) {
        os_end = s_end;
    }
    
    
    public void doTag() throws JspException, IOException {
        PageContext jsp_context = (PageContext) getJspContext();
        ServletContext jsp_application = jsp_context.getServletContext();        
        StringWriter write_buffer = new StringWriter();
        getJspBody().invoke( write_buffer );
        String s_data = write_buffer.toString ();
        
        int i_start = 0;
        if ( null != os_begin ) {
            i_start = s_data.indexOf ( os_begin );
            if ( i_start < 0 ) {
                // no match found
                return;
            }
        }
        
        int i_end   = s_data.length ();
        if ( null != os_end ) {
            int i_search = i_start + os_begin.length ();
            i_end = s_data.indexOf ( os_end, i_search );
            if ( i_end < 0 ) {
                return;
            }
            i_end += os_end.length ();
        }
        
        String s_result = s_data.substring ( i_start, i_end );
        
        jsp_context.getOut().print( s_result );
    }
    
    
}
