// JavaScript Document

	//This AJAX function deletes a stored resume and removes the resume
	//entry from the table of resumes.
	function removeResume(key) {
		var xmlhttp;
		if (window.XMLHttpRequest) { // code for IE7+, Firefox, Chrome, Opera, Safari
	  		xmlhttp = new XMLHttpRequest();
	  	} else {// code for IE6, IE5
	  		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	  	}
		xmlhttp.onreadystatechange=function() {
	  		if (xmlhttp.readyState==4 && xmlhttp.status==200) {
				var element = document.getElementById(key);
				//remove row from table
				element.parentNode.removeChild(element);
			}
	  	}
		//delete resume from GAE Datastore
		xmlhttp.open("GET","/removeResume/" + key,true);
		xmlhttp.send();
	}


	//Using jQuery to simplify the hover effects and a couple modal dialogs.
	$(function() {
		
		function hideAll() {
			//Hides all the dialogs.
			$( "#dialog1" ).hide();
			$( "#dialog2" ).hide();
			$( "#dialog3" ).hide();
			$( "#dialog4" ).hide();
			$( "#dialog5" ).hide();
			$( "#dialog6" ).hide();
			$( "#dialog7" ).hide();
			$( "#dialog8" ).hide();
			//$( "#dialog_notes" ).hide();
			$( "#pop_ups" ).hide();
			$( "#data_display" ).hide();
			$( "#show_link" ).hide();
		};
		
		hideAll();

		
		//Each dialog is associated with a button.
		//When a button is hovered over, the other dialogs are first hidden. Then the
		//appropriate dialog is shown.

		$( "#button0" ).mouseenter(function() {
			hideAll();
			$( "#dialog0" ).show();
			return false;
		});
		$( "#button1" ).mouseenter(function() {
			hideAll();
			$( "#dialog1" ).show();
			return false;
		});
		$( "#button2" ).mouseenter(function() {
			hideAll();
			$( "#dialog2" ).show();
			return false;
		});
		$( "#button3" ).mouseenter(function() {
			hideAll();
			$( "#dialog3" ).show();
			return false;
		});
		$( "#button4" ).mouseenter(function() {
			hideAll();
			$( "#dialog4" ).show();
			return false;
		});	
		$( "#button5" ).mouseenter(function() {
			hideAll();
			$( "#dialog5" ).show();
			return false;
		});	
		$( "#button6" ).mouseenter(function() {
			hideAll();
			$( "#dialog6" ).show();
			return false;
		});	
		$( "#button7" ).mouseenter(function() {
			hideAll();
			$( "#dialog7" ).show();
			return false;
		});	
		$( "#button8" ).mouseenter(function() {
			hideAll();
			$( "#dialog8" ).show();
			return false;
		});	
	
		//These are the two popup modal dialogs for uploading resumes
		//and downloading/deleting resumes.
		$( "#button_resumes" ).click(function() {
			$( "#dialog_resumes" ).dialog({
				width: 450,
				modal: true
			});
			$( "dialog_resumes" ).dialog('open');
			return false;
		});
		$( "#button_upload" ).click(function() {
			$( "#dialog_upload" ).dialog({
				width: 300,
				modal: true
			});
			$( "dialog_upload" ).dialog('open');
			return false;
		});		
		
		//You can also hide the dialogs by clicking on a button.
		$( ".button" ).click(function() {
			//hideAll();
			return false;
		});
		

		//Hide the notes and the menu.
		$( "#button_hide_displays" ).click(function() {
			$( "#dialog1" ).hide();
			$( "#dialog2" ).hide();
			$( "#dialog3" ).hide();
			$( "#dialog4" ).hide();
			$( "#dialog5" ).hide();
			$( "#dialog6" ).hide();
			$( "#dialog7" ).hide();
			$( "#dialog8" ).hide();
			$( "#dialog_notes" ).fadeToggle();
			$( "#menu" ).fadeToggle();
			$( "#show_link" ).fadeToggle();
		});
		
		//Show them again if the user wants to.
		$( "#button_show_displays" ).click(function() {
			$( "#show_link" ).fadeToggle();
			$( "#menu" ).fadeToggle();
			$( "#dialog_notes" ).fadeToggle();
		});

	}); //End custom jQuery for resume usage
