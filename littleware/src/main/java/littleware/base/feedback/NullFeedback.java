package littleware.base.feedback;


import littleware.base.event.helper.SimpleLittleTool;
import littleware.base.event.LittleListener;
import littleware.base.event.LittleTool;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;

/**
 * Feedback just fires PropertyChangeEvents and LittleEvents
 * on method calls - doesn't do anything else.
 */
public class NullFeedback implements Feedback, LittleTool, java.io.Serializable {
    private static final long serialVersionUID = -8172928920832788511L;

    private final SimpleLittleTool  osupport = new SimpleLittleTool( this );

    private int oi_progress = 0;

    @Override
    public int getProgress() {
        return oi_progress;
    }

    @Override
    public void setProgress(int i_progress) {
        int i_clean = i_progress;
        if ( i_clean < 0 ) {
            i_clean = 0;
        } else if ( i_clean > 100 ) {
            i_clean = 100;
        }
        if ( i_clean != oi_progress ) {
            int i_old = oi_progress;
            oi_progress = i_clean;
            osupport.firePropertyChange("progress", i_old, i_clean);
        }
    }

    @Override
    public void setProgress(int i_progress, int i_max) {
        setProgress( (int) (100 * (float) i_progress / (float) i_max) );
    }

    private String os_title = "";

    @Override
    public String getTitle() {
        return os_title;
    }

    @Override
    public void setTitle(String s_title) {
        String s_old = os_title;
        os_title = s_title;
        osupport.firePropertyChange( "title", s_old, s_title );
    }

    @Override
    public void publish( Object x_result ) {
        osupport.fireLittleEvent( new UiPublishEvent( this, x_result ) );
    }
    
    @Override
    public void log(Level level, String s_info) {
        osupport.fireLittleEvent( new UiMessageEvent( this, level, s_info ) );
    }

    @Override
    public void info(String s_info) {
        this.log( Level.INFO, s_info );
    }

    @Override
    public void addLittleListener(LittleListener listen_action) {
        osupport.addLittleListener( listen_action );
    }

    @Override
    public void removeLittleListener(LittleListener listen_action) {
        osupport.removeLittleListener( listen_action );
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listen_props) {
        osupport.addPropertyChangeListener( listen_props );
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listen_props) {
        osupport.removePropertyChangeListener( listen_props );
    }

    private static class NestedFeedback extends NullFeedback {
        private final NullFeedback parent;
        private final double scale;
        private final int offset;

        public NestedFeedback( NullFeedback parent, double scale ) {
            this.parent = parent;
            this.scale = scale;
            this.offset = parent.getProgress();
        }

        @Override
        public void setProgress(int progress) {
            super.setProgress( progress );
            parent.setProgress( (int) (offset + progress * scale));
        }

        @Override
        public void setProgress(int progress, int max) {
            super.setProgress( progress, max );
            parent.setProgress( (int) (offset + getProgress() * scale));
        }

        @Override
        public Feedback nested(int progress, int max) {
            return super.nested( progress, max );
        }

        @Override
        public void setTitle(String s_title) {
            super.setTitle( s_title );
            parent.info( "Nested title: " + s_title );
        }

        @Override
        public void publish(Object result) {
            super.publish( result );
            parent.publish( result );
        }

        @Override
        public void log(Level level, String info) {
            parent.log(level, info);
        }

        @Override
        public void info(String info) {
            parent.info( info );
        }
    }

    @Override
    public Feedback nested( int progress, int max) {
        // bla
        return new NestedFeedback( this, progress/ (double) max );
    }

}
