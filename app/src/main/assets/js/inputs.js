
const inputStart = document.getElementById('start');
const inputStop = document.getElementById('stop');
const inputSpeed = document.getElementById('speed');

$('#url').on('change', function () {
    var inputValue = $(this).val();
    videoId = getVideoId(inputValue);
    player.destroy();
    player = new YT.Player('player', {
        videoId: videoId,
        events: {
            'onReady': onPlayerReady,
            'onStateChange': onPlayerStateChange
        },
    });
});


function checkMinSliderValue(event) {
    player.seek
    let value = Number(event.target.value);
    if (value < Number(slider2.value - 1)) {
        inputStart.value = fancyTimeFormat(value);
        slider1.value = value;
        startTime = value;
        player.seekTo(value);
    } else {
        inputStart.value = fancyTimeFormat(Number(slider2.value) - 1);
        slider1.value = Number(slider2.value) - 1;
    }
}
function checkMaxSliderValue(event) {
    let value = Number(event.target.value);
    if (value > Number(slider1.value) + 1) {
        inputStop.value = fancyTimeFormat(value);
        slider2.value = value;
        endTime = value;
    } else {
        inputStop.value = fancyTimeFormat(Number(slider1.value) + 1);
        slider2.value = Number(slider1.value) + 1;
    }
}
function checkStartValue(event) {
    var input = event.target;
    let value = timeToSeconds(event.target.value);
    if (!input.checkValidity()) {
        alert("Mal formato de tiempo");
        inputStart.value = fancyTimeFormat(Number(slider1.value));
        return;
    }
    if (value < 0 || value >= Number(slider2.value)) {
        inputStart.value = fancyTimeFormat(Number(slider1.value));
    } else {
        slider1.value = value;
        startTime = value;
        player.seekTo(value);
    }
}
function checkStopValue(event) {
    var input = event.target;
    let value = timeToSeconds(event.target.value);
    if (!input.checkValidity()) {
        alert("Mal formato de tiempo");
        inputStop.value = fancyTimeFormat(Number(slider2.value));
        return;
    }
    if (value > Number(slider2.max) || value <= Number(slider1.value)) {
        inputStop.value = fancyTimeFormat(Number(slider2.value));
    } else {
        slider2.value = value;
        endTime = value;
    }
}

function checkSliderValue(event) {
    let value = event.target.value;
    inputSpeed.value = Number(value);
    player.setPlaybackRate(Number(slider3.value)); // set to half speed
}
function checkSpeedValue(event) {
    let value = event.target.value;
    slider3.value = value;
    player.setPlaybackRate(Number(slider3.value)); // set to half speed

}
