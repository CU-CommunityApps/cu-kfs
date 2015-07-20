/*
 * Copyright 2005-2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * ==== CU Customization: Added Kuali's CONTRIB-101 frame resizing enhancement ====
 */
var passedFrameName = "";
var passedFocusHeight = "";
var currentHeight = "";
var scrollBarWidth = 0;

//These are set through messaging between the iframes.
var docWidth;
var docHeight;

var safari = navigator.userAgent.toLowerCase().indexOf('safari');


function getIframeContentHeight() {
	var result = 0;
	result = Math.max(
		document.body.scrollHeight,
		document.body.offsetHeight,
		document.body.clientHeight
	);
	
	/*
	We need to use the calculated height of a horizontal scrollbar and add that to the height.
	This is because if we hit a page that introduces a horizontal scrollbar, we don't want the
	screen to constantly resize.
	Later on in the logic, we'll not change the height of the frame if the difference is within this
	calculated scrollbar height.
	*/
	
	result = result + scrollBarWidth;
	
	// ==== CU Customization: Added workaround to prevent lingering inner scrollbar on KRAD pages. ====
	if (window.location.href.indexOf('/kr-krad/') != -1) {
		result = result + scrollBarWidth;
	}
	
	return result;
}

function getIframeContentWidth(doc) {
	return Math.max(
		document.body.scrollWidth,
		document.body.offsetWidth,
		document.body.clientWidth
	);
}

function doTheResize() {
	if(scrollBarWidth == 0) scrollBarWidth = getScrollBarWidth();
	
	if (passedFrameName != "" && passedFocusHeight != ""){
		setFocusedIframeDimensions(passedFrameName, passedFocusHeight, false );
	}
}
window.setInterval(doTheResize,500);

function setFocusedIframeDimensions(iframeName, focusHeight, resetToDefaultWidth ) {
	try {
		passedFrameName = iframeName;
		passedFocusHeight = focusHeight;
		if (currentHeight == "") currentHeight = focusHeight;
		
		var iframe_portlet_container_table = document.getElementById('iframe_portlet_container_table');
		var iframeEl = document.getElementById? document.getElementById(iframeName) : document.all? document.all[iframeName]: null;
  
		if ( iframeEl && iframe_portlet_container_table) {
			iframeEl.contentWindow.postMessage('getWidth', '*');
			iframeEl.contentWindow.postMessage('getHeight', '*');
			
			if ( resetToDefaultWidth ) iframe_portlet_container_table.width = "100%";
						
			//For the width, we want to set it if the scroll width is greater than the offset
			if ( Math.abs( docWidth - iframe_portlet_container_table.width ) > 10 ) {
				if ( docWidth > iframe_portlet_container_table.width ) {
					iframe_portlet_container_table.width = docWidth;
				}
			}

			if ( Math.abs( docHeight - currentHeight ) > scrollBarWidth ) {
				iframeEl.style.height = docHeight + "px";
				currentHeight = docHeight;
			}
		}
	} catch(err) {
		window.status = err.description;
	}
}

function jumpToAnchorName(anchor){
	var anchors = document.getElementsByName(anchor);
	if (anchors != null) location.href = '#'+anchors[0].name;
}

function getScrollBarWidth() {
	var inner = document.createElement('p');
	inner.style.width = "100%";
	inner.style.height = "200px";
	
	var outer = document.createElement('div');
	outer.style.position = "absolute";
	outer.style.top = "0px";
	outer.style.left = "0px";
	outer.style.visibility = "hidden";
	outer.style.width = "200px";
	outer.style.height = "150px";
	outer.style.overflow = "hidden";
	outer.appendChild (inner);
	
	document.body.appendChild (outer);
	var w1 = inner.offsetWidth;
	outer.style.overflow = 'scroll';
	var w2 = inner.offsetWidth;
	if (w1 == w2) w2 = outer.clientWidth;
	
	document.body.removeChild (outer);
	
	return (w1 - w2);
}

function iframeContentResizeListener(event){
	//These methods are executed from the iframe content's perspective
	if(event.data === 'getWidth'){
		event.source.postMessage('getWidth=' + getIframeContentWidth(), event.origin);
	}
	else if(event.data === 'getHeight'){
		event.source.postMessage('getHeight=' + getIframeContentHeight(), event.origin);
	}
	//These methods are executed from the portal's perspective
	else if(event.data.indexOf('getWidth=') > -1){
		docWidth = event.data.substring(9);
	}
	else if(event.data.indexOf('getHeight=') > -1){
		docHeight = event.data.substring(10);
	}
}

if (window.addEventListener){
	addEventListener("message", iframeContentResizeListener, false)
} else {
	attachEvent("onmessage", iframeContentResizeListener)
}