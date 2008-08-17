<!--
/** 
 * lwUtil.js
 *
 * Mixed bag of javascript utility functions
 */

/**
 * Toggle the display style of the dom-element with the given id
 * 
 * @param s_element id of dom-element to toggle
 * @return void
 */
function lwUtil_toggle( s_element ) {
	var dom_element = document.getElementById( s_element )

	if ( dom_element ) {
	    if ( dom_element.style.display == 'none') {
		dom_element.style.display = 'block';
	    } else {
		dom_element.style.display = 'none';
	    }
        }
}

/**
 *  Verify two strings match - popup error otherwise
 *
 * @param dom_form dom hook into form data
 * @param s_label to tell user to re-enter in popup
 * @param s_entry dom-id of form-input initial entry
 * @param s_confirm dom-id of form-input confirmation entry
 * @return true if dom_form[ s_entry ] == dom_form[ s_confirm ]
 */
function lwUtil_check_confirm( dom_form, s_label, s_entry, s_confirm ) {
    if ( dom_form[ s_entry ].value == dom_form[ s_confirm ].value ) {
        return true;
    } else {
        alert ( s_label + " and confirm do not match" );
        return false;
    }
}

function lwUtil_open_popup(url,name,width,height,resizable,scrollbars,menubar,toolbar,location,directories,status) {	
	popup = window.open(url, name, 'width=' + width + ',height=' + height + ',resizable=' + resizable + ',scrollbars=' + scrollbars
	+ ',menubar=' + menubar + ',toolbar=' + toolbar + ',location=' + location + ',directories=' + directories + ',status=' + status
	);
//popup.moveTo(((screen.availWidth-340)/2),((screen.availHeight-360)/2));
	popup.focus();
}



// -->
