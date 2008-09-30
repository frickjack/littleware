import sys
import os
import string
import re
import unittest

# $Header: /Users/pasquini/Code/cvsRoot/littleware/tomcat_install/webapps/littleware/en/home/blogs/littleware_news/2007/macUtil.py,v 1.1 2007/02/10 16:11:19 pasquini Exp $
# 
# Notes:
#    reload() to reload module in interactive interpreter
#    readonly strings
#    re.search
#

#--------- Test Cases -------------------------

class GenericTest( unittest.TestCase ):
    """
        Generic test suite for macUtil module.
        Run with:

import littleware.base.macUtil
import unittest
x_suite = unittest.defaultTestLoader.loadTestsFromModule( littleware.base.macUtil )
x_result = unittest.TestResult()
x_suite.run ( x_result )
print x_result

    """
    def setUp( self ):
        """ No setup necessary """

    def tearDown ( self ):
        """ No teardown necessary """

    def testMacPath ( self ):
        """ Check if a few paths convert correctly """
        print "\n\nRunning testMacPath ...\n"
        self.failUnless ( "/Users/pasquini/chubs.jpg" == 
                             macosToUnixFilename ( "Macintosh HD:Users:pasquini:chubs.jpg" ),
                         "Failed to convert chubs.jpg to unix path"
                       )
        self.failUnless ( 
                           unixToMacosFilename ( "/Users/pasquini/chubs.jpg" )  == 
                             "Macintosh HD:Users:pasquini:chubs.jpg",
                         "Failed to convert chubs.jpg to Mac path"
                       )


    def testPopup ( self ):
        """ Check if Finder popup dialog is processed correctly """
        print "Please press OK in the Finder now ...\n"
        self.failUnless ( "OK" == popupFinderDialog( "python test:\nPlease press OK" ),
                          "testPopup OK failed" );
        print "Please press CANCEL in the Finder now ...\n"
        self.failUnless ( "CANCEL" == popupFinderDialog( "python test:\nPlease press CANCEL" ),
                          "testPopup CANCEL failed" );

    def testImageStuff ( self ):
        """ Scale a test image from the finder """
        print "Please press OK after selecting a test image in the Finder to make a 1/4 scaled test-copy of ...\n"
        self.failUnless ( "OK" == popupFinderDialog( "python test:\nSelect test image and press OK" ),
                          "testImage OK failed" 
                       )
        v_selection = getFinderSelection()
        print "testImageScale got finder selection: " + str( v_selection ) + "\n"
        self.failUnless ( len(v_selection) > 0, 
                          "No test image selected in Finder" )
        v_dimensions = (0,0)
        try:
	    v_dimensions = getImageDimensions ( v_selection[ 0 ] )
            print ( "Got image dimensions for " + v_selection[ 0 ] +
                  ": " + str( v_dimensions ) )
        except Exception, e:
            self.failUnless( False, "Caught exception: " + str(e) )
        
        self.failUnless( (v_dimensions[0] > 0) and (v_dimensions[1] > 0), 
                          "Got invalid image dimensions for " + v_selection[0] +
                          ": " + str(v_dimensions) )
        s_testfile = "/tmp/bla.png"
        os.umask( 0 )
        if os.path.exists( s_testfile ):
            os.unlink ( s_testfile )
        try:
            scaleImage( v_selection[ 0 ], 0.25, s_testfile )
        except Exception, e:
            self.failUnless( False, "Caught exception: " + str(e) )
        self.failUnless( os.path.exists( s_testfile ),
                         "scaleImage failed to generate output: " + s_testfile )
        os.spawnl( os.P_WAIT, "/usr/bin/open", "open", s_testfile )

    def testCopy ( self ):
        try:
            print "\n\nRunning testCopy ...\n"
            s_testfile = "/tmp/bla.input"
            s_destination = "/tmp/bla.testcase.output"
            os.umask( 0 )
            if os.path.exists( s_testfile ):
                os.unlink ( s_testfile )
            if os.path.exists( s_destination ):
                os.unlink ( s_destination )
            fh_tmp = open( s_testfile, "w" )
            fh_tmp.write ( s_testfile + "\n" )
            fh_tmp.close ()
            s_result = copyFinder( s_testfile, s_destination )
            print "Result is: " + s_result
            self.failUnless( os.path.exists( s_destination ),
                         "copyFinder failed to generate output: " + s_destination )
        except Exception, e:
            self.failUnless( False, "Caught exception: " + str(e) )

#--------- Exceptions -------------------------

class BadArgumentException( Exception ):
    """ Bad arguments past to some MacUtil function """
    def __init__(self, value):
        self.value = value

    def __str__(self):
        return `self.value`

class ApplescriptException( Exception ):
    """ Some failure executing applescript call """
    def __init__(self, value):
        self.value = value

    def __str__(self):
        return `self.value`

class ImageException( Exception ):
    """ 
        Image handling failed for some unknown reason.
    """ 
    def __init__(self, value):
        self.value = value

    def __str__(self):
        return `self.value`

# .............................................................

def runApplescript( s_script_contents ):
    """ Return the stdout from executing the given Applescript """
    s_tmp = "/tmp/python_" + str( os.getpid() ) + ".applescript"
    fh_tmp = open( s_tmp, "w" )
    fh_tmp.write ( s_script_contents + "\n" )
    fh_tmp.close ()
    fh_pipe = os.popen( "/usr/bin/osascript " + s_tmp )
    s_result = fh_pipe.read()
    fh_pipe.close()
    os.unlink( s_tmp )
    return s_result


def getFinderSelection():
    """ Get the path to the current selection in the finder """
    s_applescript = """
        tell the application \"Finder\"
            set pathlist to selection
        end tell
        set path_summary to ""
        repeat with path in pathlist
            set path_summary to (path_summary & path as string) & "\n"
        end repeat
        path_summary as string
        """
    s_result = string.strip( runApplescript( s_applescript ) )
    re_split = re.compile( r"[\r\n]+" )
    v_macos_paths = re_split.split( s_result )
    v_result = []
    for s_path in v_macos_paths:
        v_result.append( macosToUnixFilename( s_path ) )
    
    return v_result

def copyFinder( s_source_path, s_destination_path ):
    """ Copy a to b in the finder """
    s_source_path = os.path.abspath ( s_source_path )
    s_destination_path = os.path.abspath ( s_destination_path )
    s_type = "file"
    if os.path.isdir( s_source_path ):
        s_type = "dir"

    if s_source_path == s_destination_path:
        raise BadArgumentException( "Same source and destination: " + s_source_path )
    s_applescript = """
        tell the application "Finder"
            set ref_in to %s "%s"
            set ref_out to duplicate ref_in to folder "%s"
            set name of ref_out to "%s"
        end tell
        """ % ( s_type, 
                unixToMacosFilename( s_source_path ), 
                unixToMacosFilename( os.path.dirname( s_destination_path ) ),
                os.path.basename( s_destination_path )
              )
    #print "Running: " + s_applescript
    s_result = string.strip( runApplescript( s_applescript ) )
    return s_result

def popupFinderDialog( s_message ):
    """ Popup a dialog in the Finder. 

     Display the given message in the popup.
     Return "OK" if user hit "OK", or "CANCEL"
    """
    re_newlines = re.compile ( r"\n" )
    s_clean_message = re_newlines.sub ( r"\\n", s_message )
    s_applescript = """
        tell the application \"Finder\"
            display dialog \"""" + s_message + """\"
        end tell
        \"OK\"
        """
    s_result = string.strip( runApplescript ( s_applescript ) )
    if not s_result == "OK":
        s_result = "CANCEL"
    return s_result

def userGzip ( s_unix_filename ):
    """ gzip the given file.
     Verify the given filename refers to a file in the
     user's home directory.  Invoke gzip on the filename.
     Throw an exception if file is invalid or if gzip fails.
    """
    s_login = os.getlogin ()
    re_pattern = re.compile( r"^/Users/" + s_login )
    if not re_pattern.search( s_unix_filename ):
        raise BadArgumentException( "userGzip filename must be absolute path under /Users/" + s_login + ": " + s_unix_filename )
    os.spawnl( os.P_WAIT, "/usr/bin/gzip", "-f", s_unix_filename )

def unixToMacosFilename ( s_unix_filename ):
    """
     Convert the given UNIX format filename '/bla/bla/bla...',
     or relative (bla/bla/bla)
     to a macos format filename "Driver:bla:bla:bla"
    """
    s_result = os.path.realpath ( s_unix_filename )
    s_drive = "Macintosh HD"
    re_pattern = re.compile( "^/Volumes/([^/]+)/" )
    re_data = re_pattern.search( s_unix_filename )
    if re_data:
        s_drive = re_data.group( 1 )
    s_result = re.sub( "^/(Volumes/[^/]+/)?", s_drive + ":", s_result )
    s_result = re.sub( "/", ":", s_result )
    return s_result
    

def macosToUnixFilename ( s_macos_filename ):
    """
     Convert the given macos format filename 'Drive:Folder:Folder:File',
     to a unix format filename
    """
    re_pattern = re.compile( r"^Macintosh HD:" )
    if re_pattern.search( s_macos_filename ):
        s_tmp = re_pattern.sub ( "/", s_macos_filename )
    else:
        s_tmp = "/Volumes/" + s_macos_filename

    s_result = re.sub( ":", "/", s_tmp )
    return s_result
    

def scaleImage ( s_input_path, f_factor, s_png_output_path ):
    """
       Scale the given input image by the specified factor,
       and write the output to the specified output path
       in PNG form.
    """
    if not os.path.exists ( s_input_path ):
        raise BadArgumentException ( "Input does not exist: " + s_input_path )
    if os.path.exists ( s_png_output_path ):
        raise BadArgumentException ( s_png_output_path + " already exists" )
    s_applescript = '\n\
tell the application "Image Events"\n\
    set x_photo to open file "' + s_input_path + '"\n\
    scale x_photo by factor ' + str( f_factor ) + '\n\
    save x_photo as PNG in file "' + s_png_output_path + '"\n\
end tell\n\
"OK"\n\
'
    s_result = string.strip( runApplescript( s_applescript ) )
    if (not s_result == "OK") or (not os.path.exists( s_png_output_path )):
        raise ImageException ( "Failed to scale " + s_input_path +
                                     " by " + str( f_factor ) +
                                     " to " + s_png_output_path )


def getImageDimensions ( s_image_path ):
    """
       Get the width, height dimensions of an image.
       Return 2 element pair
    """
    if not os.path.exists ( s_image_path ):
        raise BadArgumentException ( "Input does not exist: " + s_image_path )
    s_applescript = '\n\
tell application "Image Events"\n\
	set x_image to open file "' + s_image_path + '"\n\
	set v_info to dimensions of x_image\n\
end tell\n\
set s_dimensions to ""\n\
repeat with f_info in v_info\n\
	set s_dimensions to (s_dimensions & f_info as string) & "\n"\n\
end repeat\n\
s_dimensions as string\n\
'
    s_result = string.strip( runApplescript( s_applescript ) )
    re_split = re.compile( r"[\r\n]+" )
    v_dimensions = re_split.split( s_result )
    if 2 != len( v_dimensions ):
        raise ImageException ( "Failed to extract image dimensions for " +
                               s_input_image + " from " + s_result )
    return (float( v_dimensions[0] ), float( v_dimensions[1] ))
