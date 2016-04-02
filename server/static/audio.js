        // Global variable to track current file name.
        var currentFile = "";
        function playAudio() {
            // Check for audio element support.
            if (window.HTMLAudioElement) {
 
         // Get file from text box and assign it to the source of the audio element 
                try {
                    var oAudio = document.getElementById('myaudio');
                    var btn = document.getElementById('playbutton');
                    var audioURL = document.getElementById('audiofile');

                    //Skip loading if current file hasn't changed.
                    if (audioURL.value !== currentFile) {
                        oAudio.src = audioURL.value;
                        currentFile = audioURL.value;
                    }

                    // Tests the paused attribute and set state.
                    if (oAudio.paused) {
                        oAudio.play();
                        btn.textContent = "Pause";
                    }
                    else {
                        oAudio.pause();
                        btn.textContent = "Play";
                    }
                }
                catch (e) {
                    // Fail silently but show in F12 developer tools console
                    if (window.console && console.error("Error:" + e));
                }
            }
        }

        // Rewinds the audio file by 3 seconds.

        function rewindAudio() {
            // Check for audio element support.
            if (window.HTMLAudioElement) {
                try {
                    var oAudio = document.getElementById('myaudio');
                    oAudio.currentTime -= 3.0;
                }
                catch (e) {
                    // Fail silently but show in F12 developer tools console
                    if (window.console && console.error("Error:" + e));
                }
            }
        }

        // Fast forwards the audio file by 3 seconds.

        function forwardAudio() {

            // Check for audio element support.
            if (window.HTMLAudioElement) {
                try {
                    var oAudio = document.getElementById('myaudio');
                    oAudio.currentTime += 3.0;
                }
                catch (e) {
                    // Fail silently but show in F12 developer tools console
                    if (window.console && console.error("Error:" + e));
                }
            }
        }


        // Increment playbackRate by 1 

        function increaseSpeed() {

            // Check for audio element support.
            if (window.HTMLAudioElement) {
                try {
                    var oAudio = document.getElementById('myaudio');
                    if (oAudio.playbackRate <= 1) {
                      var temp = oAudio.playbackRate;
                      oAudio.playbackRate = (temp * 2);
                    } else {
                      oAudio.playbackRate += 1;
                    }
                }
                catch (e) {
                    // Fail silently but show in F12 developer tools console
                    if (window.console && console.error("Error:" + e));
                }
            }
        }


        // Cut playback rate in half

        function decreaseSpeed() {

            // Check for audio element support.
            if (window.HTMLAudioElement) {
                try {
                    var oAudio = document.getElementById('myaudio');
                    if (oAudio.playbackRate <= 1) {
                      var temp = oAudio.playbackRate;
                      oAudio.playbackRate = (temp / 2);
                    } else {
                      oAudio.playbackRate -= 1;
                    }
                }
                catch (e) {
                    // Fail silently but show in F12 developer tools console
                    if (window.console && console.error("Error:" + e));
                }
            }
        }

        // Restart the audio file to the beginning.

        function restartAudio() {
            // Check for audio element support.
            if (window.HTMLAudioElement) {
                try {
                    var oAudio = document.getElementById('myaudio');
                    oAudio.currentTime = 0;
                }
                catch (e) {
                    // Fail silently but show in F12 developer tools console
                    if (window.console && console.error("Error:" + e));
                }
            }
        }