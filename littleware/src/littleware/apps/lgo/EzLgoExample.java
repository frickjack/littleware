/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

/**
 * Simple POJO
 */
public class EzLgoExample implements LgoExample {

    private final String os_title;
    public String getTitle() {
        return os_title;
    }

    private final String os_description;
    public String getDescription() {
        return os_description;
    }

    /**
     * Constructor injects parameter values.
     */
    public EzLgoExample( String s_title, String s_description ) {
        os_title = s_title;
        os_description = s_description;
    }
}
