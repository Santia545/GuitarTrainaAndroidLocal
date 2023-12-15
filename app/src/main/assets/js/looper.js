let videoId = 'C0ZoySevEEs';
let tag = document.createElement('script');
let externalTag = document.getElementById('myTag');
let startTime = 0;
let endTime = 100;
const slider1 = document.getElementById('slider1');
const slider2 = document.getElementById('slider2');
const slider3 = document.getElementById('slider3');
tag.src = "https://www.youtube.com/iframe_api";
document.body.insertBefore(tag, externalTag);
document.getElementById('btn').onclick = () => {
    player.seekTo(startTime);
}

//Set up Youtube Iframe API.
let player;
function onYouTubeIframeAPIReady() {
    player = new YT.Player('player', {
        videoId: videoId,
        events: {
            'onReady': onPlayerReady,
            'onStateChange': onPlayerStateChange
        },
    });

}

// Resize player on window resize
window.addEventListener('resize', function () {
    const playerWrapper = document.getElementById('player');
    const playerWidth = playerWrapper.getBoundingClientRect().width;
    const playerHeight = playerWidth * 9 / 16;
    player.setSize(playerWidth, playerHeight);
});
let interval = null;
function onPlayerStateChange(event) {
    // Check if player state is PLAYING
    if (event.data === YT.PlayerState.PLAYING) {
        interval = setInterval(function () {
            var currentTime = player.getCurrentTime();
            endTime = document.getElementById('slider2').value;
            //console.log(`current time: ${currentTime}>=endTime: ${endTime}?${currentTime >= endTime}`);
            // Check if current playback time is greater than or equal to the desired time
            if (currentTime >= endTime) {
                // Do something when user reaches the desired point in the video
                player.seekTo(startTime);
            }
        }, 100);
    } else {
        clearInterval(interval);
        interval = null;
    }
    if (event.data == 0) { player.seekTo(startTime) }
}

function onPlayerReady(event) {
    event.target.setPlaybackRate(Number(slider3.value)); // set to half speed
    const playerWrapper = document.getElementById('player');
    const playerWidth = playerWrapper.offsetWidth;
    const playerHeight = playerWidth * 9 / 16;
    player.setSize(playerWidth, playerHeight);
    let length = Number(player.getDuration() - 1);
    slider1.max = length;
    slider1.value = 0;
    slider2.min = 0;
    slider2.max = length;
    slider2.value = length;
    startTime = 0;
    endTime = length;
    document.getElementById('start').value = fancyTimeFormat(0);
    document.getElementById('stop').value = fancyTimeFormat(length);
    document.getElementById('slider2').value = length;

}

function fancyTimeFormat(duration) {
    // Hours, minutes and seconds
    const hrs = ~~(duration / 3600);
    const mins = ~~((duration % 3600) / 60);
    const secs = ~~duration % 60;
    // Output like "1:01" or "4:03:59" or "123:03:59"
    let ret = "";
    if (hrs > 0) {
        ret += "" + hrs + ":" + (mins < 10 ? "0" : "");
    }
    ret += "" + mins + ":" + (secs < 10 ? "0" : "");
    ret += "" + secs;
    return ret;
}
function timeToSeconds(timeString) {
    var timeParts = timeString.split(":"); // Split the time string into parts

    // Initialize hours, minutes, and seconds to 0
    var hours = 0;
    var minutes = 0;
    var seconds = 0;

    // Check the number of time parts and assign values accordingly
    if (timeParts.length === 1) {
        // If there are 1 parts, assume the format is "SS"
        seconds = parseFloat(timeParts[0]); // Extract seconds
    } else if (timeParts.length === 2) {
        // If there are 2 parts, assume the format is "MM:SS"
        minutes = parseFloat(timeParts[0]); // Extract minutes
        seconds = parseFloat(timeParts[1]); // Extract seconds
    } else if (timeParts.length === 3) {
        // If there are 3 parts, assume the format is "HH:MM:SS"
        hours = parseFloat(timeParts[0]); // Extract hours
        minutes = parseFloat(timeParts[1]); // Extract minutes
        seconds = parseFloat(timeParts[2]); // Extract seconds
    } else {
        // If the number of parts is not 2 or 3, return NaN to indicate invalid input
        return NaN;
    }
    var totalSeconds = (hours * 3600) + (minutes * 60) + seconds; // Calculate total seconds
    return totalSeconds; // Return the total seconds
}
function getVideoId(url) {
    const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/;
    const match = url.match(regExp);

    return (match && match[2].length === 11)
        ? match[2]
        : null;
}
