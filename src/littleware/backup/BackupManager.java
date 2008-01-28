package littleware.backup;

import java.io.*;
import java.util.List;

/**
 * Interface to simple backup manager
 */
public interface BackupManager {
	/**
	 * Backup the specified list of files and directories
	 * relative to the given root.
	 * Implementation will do an existence check before
	 * collecting backup.
	 *
	 * @param fi_root that list of paths ot backup is relative to
	 * @param v_backup list of relative-paths to root to backup
	 * @return reference to a temporary archive file that the caller
	 *               may copy off to a repository
	 * @exception BackupException on archive-collection failure
	 * @exception FileNotFoundException if specified file does not exist or is not readable
	 */
	public File archiveFiles ( File fi_root, 
							   List<File> v_backup 
							   ) throws BackupException, FileNotFoundException;
	
	/**
	 * Find files recursively under the given root that pass through the given filter
	 *
	 * @param fi_root to search under
	 * @param x_filter to apply to File object with absolute path
	 *                 referencing file under the root
	 * @return List of files relative to the given root
	 * @exception FileNotFoundException if root does not exist
	 * @exception BackupException on unexpected failures
	 */
	public List<File> findFiles ( File fi_root, FileFilter x_filter ) throws BackupException, FileNotFoundException;

}
