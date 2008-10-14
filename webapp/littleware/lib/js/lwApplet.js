<!--

/** 
 * lwApplet.js
 *
 * Applet support module
 */

 /**
  * Little document.write/writeln
  * filter that will convert '<' to '&lt;'
  * when b_filter_enabled=true
  */
function lwApplet_Filter ( b_filter_enabled ) {
    function my_filter ( s_in ) {
        var s_out = s_in;
        if ( this.b_filter_enabled  == true) {
            s_out = s_in.replace ( /\</g, "&lt;" );
        } 
	return s_out;
    }
    function my_write () {
	for ( var i=0; i < arguments.length; i++ ) {
	    document.write ( this.filter ( arguments[i] ) );
	}
    }
    function my_writeln () {
	for (var i = 0; i < arguments.length - 1; i++) {
	    this.write ( arguments[ i ] );
	}
	document.writeln ( this.filter ( arguments[ arguments.length - 1] ) );
    }
    this.b_filter_enabled = true;
    this.write = my_write;
    this.filter = my_filter;
    this.writeln = my_writeln;
    return this;
 }

/**
 * document.write() out code to install the
 * applet that runs the given class with
 * the given classpath and parameters.
 * Assumes that all our applet code is in littleClient.jar,
 * and that we want jdk1.5 or better.
 * 
 * @param io_handle object with write() and writeln() methods -
 *             usually document or an lwApplet_Filter
 * @param s_element id of dom-element to toggle
 * @return void
 */
function lwApplet_writeApplet( io_handle, s_appletclass, i_width, i_height, v_params ) {
    var s_app = navigator.appName;

    document.writeln ( "<pre>" );
    if (s_app == 'Netscape') {
        io_handle.writeln('<embed code="' + s_appletclass + '"',
		       ' width="' + i_width + '"',
		       ' height="' + i_height + '"',
		       ' codebase="/littleware/lib/jar"',
		       ' archive="littleClient.jar,mailapi.jar,mail.jar"',
		       ' pluginspage="http://java.com/download/"',
		       ' type="application/x-java-applet;version=1.5.0"'
                   );
    } else if ( s_app == 'Microsoft Internet Explorer') {
        io_handle.writeln('<object ',
		       ' classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"',
		       ' width="' + i_width + '"',
		       ' height="' + i_height + '">'
			 );
	io_handle.writeln ( '<param name="code" value="' + s_appletclass + '"/>' );
	io_handle.writeln ( '<param name="codebase" value="/littleware/lib/jar"/>' );
	io_handle.writeln ( '<param name="archive"  value="littleClient.jar,mailapi.jar,mail.jar" />' );
    } else {
	io_handle.writeln ( '<applet ',
			 ' code="' + s_appletclass + '"',
			 ' codebase="/littleware/lib/jar"',
			 ' archive="littleClient.jar,mailapi.jar,mail.jar"',
			 ' width="' + i_width + '"',
			 ' height="' + i_height + '">'
			 );
    }

    // applet parameters
    if ( s_app == 'Netscape' ) {
	for ( var i=0; i <  v_params.length; ++i ) {
	    io_handle.writeln ( v_params[i].s_name, '="',
                                v_params[i].s_value, '"'
				);
	}
        io_handle.writeln ( "/>" );
    } else {
	for ( var i=0; i <  v_params.length; ++i ) {
	    io_handle.writeln ( '<param name="' + v_params[i].s_name + '" ' +
				'value="' + v_params[i].s_value + '" />'
				);
	}
    }

    // close tags
    if (s_app == 'Netscape') {
        io_handle.writeln( '<noembed>' );
        io_handle.writeln( 'No Java 2 SDK, Standard Edition support installed.' );
        io_handle.writeln( 'Try to <a href="http://java.com/download">download</a> java.' );
        io_handle.writeln( '</noembed>' );
        io_handle.writeln( '</embed>' );
    } else if ( s_app == 'Microsoft Internet Explorer') {
	io_handle.writeln ( '</object>' );
    } else {
	io_handle.writeln ( 'Your browser is completely ignoring the &lt;applet&gt; tag!' );
	io_handle.writeln ( '</applet>' );
    }
    document.writeln ( "</pre>" );
}



// -->
