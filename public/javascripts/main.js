/**
 * Created by Tomash on 04.04.14.
 */
$(document).ready(function () {

    $(':checkbox').checkbox();
    $(".has-tooltip").tooltip();
    var $slider = $(".slider");
    jQuery("span.timeago").timeago();

    if ($slider.length > 0) {
        $slider.slider({
            min: 0,
            max: 3,
            value: 0,
            orientation: "horizontal",
            range: "min"
        });

        $(document).on("mousedown", ".slider", function(){
            $("body").mouseup(function(){
                $("input[name='difficulty']").val($slider.slider("option", "value"));
                $(this).off("mouseup");
            });
        });
    }

    var $uiBlock = $(".ui-block");
    $uiBlock.css("height", $uiBlock.width() / 1.6);
    $uiBlock.show();

    $(window).on("resize", function () {
        $uiBlock.css("height", $(".ui-block").width() / 1.2);
    });

    $uiBlock.click(function () {
        if($(this).attr("id") != "share-block") {
            $(this).parents("#wrapper").height($(this).parents("#wrapper").height());
            $(this).toggleClass('active');
            $(this).siblings().not(this).toggleClass('hide');
            $(this).fadeOut(1500, function () {
                var $blockBody = $("#" + $(this).attr("id") + "-body");
                $(this).removeClass("active");
                $blockBody.removeClass('hide');
                $blockBody.fadeIn(1000);

                $(this).parents("#wrapper").removeAttr("style");
            });
        }
    });

    $("select").selectpicker({style: 'btn-hg btn-info', menuStyle: 'dropdown-inverse'});

    // Focus state for append/prepend inputs
    $('.input-group').on('focus', '.form-control', function () {
        $(this).closest('.input-group, .form-group').addClass('focus');
    }).on('blur', '.form-control', function () {
        $(this).closest('.input-group, .form-group').removeClass('focus');
    });
/*
    $(".backAction").click(function (e) {
        $(".ui-block-body").hide();
        $uiBlock.removeClass('hide');
        $uiBlock.fadeIn(1000);

        e.preventDefault();
    });
*/
    var formChallengesRows = function (challenges, username) {
        var $body = "";

        $.each(challenges, function (i) {

            var $formattedName = challenges[i].creator.username;

            if(challenges[i].creator.firstName) {
                $formattedName = challenges[i].creator.firstName + ' ' + challenges[i].creator.lastName.substring(0, 3);
            }

            $body += '<tr><td class="profilePicTd"><a href="' + jsRoutes.controllers.Application.showProfile(challenges[i].creator.username).url + '">';
            if(challenges[i].creator.profilePictureUrl != undefined) {
                $body += '<img class="smallProfilePicture" src="' + challenges[i].creator.profilePictureUrl + '"/>';
            } else {
                $body += '<img src="/assets/images/avatar_small.png"/>';
            }
            $body += $formattedName + '</a></td><td><a href="' + jsRoutes.controllers.Application.showChallenge(challenges[i].id).url + '">' + challenges[i].challengeName +
                '</a></td><td>' + formatRating(challenges[i].rating) + '</td><td>' + formatDifficulty(challenges[i].difficulty) + '</td><td>';
                if(challenges[i].creator.username != username){ $body += '<input type="button" class="btn btn-hg btn-success browse-challenge-join-action" value="Join"/>' +
                '<input class="challenge-id" type="hidden" value="' + challenges[i].id + '"/>'; }
            $body += '</td></tr>';
        });

        return $body;
    };

    var formatDifficulty = function(index){
        var levels = ["easy", "medium", "hard", "special"];
        return levels[index];
    }

    var formatRating = function(rating){
        var $stars = "";
        for ( var i = 0; i < 5; i++ ) {

            if((rating - i) <= 0.3){
                $stars += ' <a class="rating-star"></a>';
            }else if((rating - i) >= 0.8){
                $stars += ' <a class="rating-star full-star"></a>';
            }else{
                $stars += ' <a class="rating-star half-star"></a>';
            }
        }
        return $stars;
    }

    $(".browse-challenges-form").submit(function (e) {

        var phrase = $(this).find(".challenge-search-phrase").val(), category = $(".challenge-category").val(), username = $(".current-username").val();

        NProgress.start();

        jsRoutes.controllers.Application.ajaxGetChallengesForCriteria(phrase, category).ajax({
            success: function (response) {
                var challenges = jQuery.parseJSON(response), body = formChallengesRows(challenges, username);

                $(".challenge-search-results table tbody").html(body);
                NProgress.done();

            }
        });

        e.preventDefault();
    });

    $("#browse-block").click(function (e) {

        var username = $(".current-username").val();
        NProgress.start();

        jsRoutes.controllers.Application.ajaxGetLatestChallenges().ajax({
            success: function (response) {
                var challenges = jQuery.parseJSON(response), body = formChallengesRows(challenges, username);

                $(".challenge-search-results table tbody").html(body);
                $(".switch").bootstrapSwitch();
                NProgress.done();
            }
        });

        e.preventDefault();
    });

    $(document).on("click", ".browse-challenge-join-action", function(){
        NProgress.start();
        var $this = $(this), $parent = $this.parents("td"), challengeId = $parent.find(".challenge-id").val();
        jsRoutes.controllers.Application.ajaxJoinChallenge(challengeId).ajax({
            success: function (response) {
                $this.remove();
                $parent.append('<input type="button" class="btn btn-hg btn-danger browse-challenge-leave-action" value="Leave"/>');
                alertify.success("You joined the competition!");
                NProgress.done();
            }
        });

    });

    $(document).on("click", ".browse-challenge-leave-action", function(){

        var $this = $(this), $parent = $this.parents("td"), challengeId = $parent.find(".challenge-id").val();

        NProgress.start();
        jsRoutes.controllers.Application.ajaxLeaveChallenge(challengeId).ajax({
            success: function (response) {
                $this.remove();
                $parent.append('<input type="button" class="btn btn-hg btn-success browse-challenge-join-action" value="Join"/>');
                alertify.error("You left the competition...");
                NProgress.done();
            }
        });

    });

    $(document).on("click", ".challenge-details-leave-action", function(){

        var challengeId = $("#content-wrapper").find(".challenge-id").val();

        alertify.confirm("Are you sure you want to leave this challenge ?", function (e) {
            if (e) {
                NProgress.start();

                jsRoutes.controllers.Application.ajaxLeaveChallenge(challengeId).ajax({
                    success: function (response) {
                        window.location.href = jsRoutes.controllers.Application.showMyParticipations().url;
                        NProgress.done();
                    }
                });
            }
        });

    });

    var wrapper = $('<div/>').css({height: 0, width: 0, 'display': 'none'}), $file = $(':file');
    var fileInput = $file.wrap(wrapper);

    fileInput.change(function () {
        var $this = $(this);
        if ($this.val() != '') {
            $('#video-input-wrapper').html('<div id="video-screenshot">' + $this.val().replace(/.*(\/|\\)/, '') + '<img src="/assets/images/correct.png"/></div>');
        }
        else {
            $('#video-input-wrapper').html('<button id="upload-video-action" type="button" class="btn btn-warning btn-hg">Upload a video description...</button>');
        }
    });

    var uploadResponse = $file.wrap(wrapper);

    $('#video-input-wrapper').click(function () {
        fileInput.click();
    }).show();

    uploadResponse.change(function () {
        var $this = $(this);
        if ($this.val() != '') {
            $('#upload-response-wrapper').html('<div id="video-screenshot">' + $this.val().replace(/.*(\/|\\)/, '') + '<img src="/assets/images/correct.png"/></div>');
        }
        else {
            $('#upload-response-wrapper').html('<button type="button" class="btn btn-warning">Submit a response.</button>');
        }
    });

    $('#upload-response-wrapper').click(function () {
        uploadResponse.click();
    }).show();

    $('#create-challenge-form').submit(function (e) {

        NProgress.start();

        $(this).ajaxSubmit({
            success: function (response) {
                var customResponse = jQuery.parseJSON(response);

                if (customResponse.hasOwnProperty("status")) {

                    if(customResponse.rewardedPoints > 0) {
                        rewardAllPoints(customResponse.messages, customResponse.points);
                    }

                    alertify.alert("Challenge created and ready to join ! ", function (e) {
                        if (e) {
                            window.location = "/";
                        }
                    });
                }
                else {
                    var fields = jQuery.parseJSON(response);

                    $.each(fields, function (i) {
                        var errors = fields[i];
                        $.each(errors, function (j) {
                            alertify.error(errors[j].message);
                        });
                    });
                }
                NProgress.done();
            }
        });
        e.preventDefault();

    });

    $("#challenge-visibility").change(function () {

        if ($(this).val() == 0) {

            var ids = [];
            $(".friend-item").each(function(){
                var $arr = $(this).find(".selected-user-data").val().split(",");
                ids.push($arr[0]);
            });
            FB.ui({method: 'apprequests',
                message: 'I created a challenge! Will you be able to complete it ? ;]',
                exclude_ids: ids
            }, function(fbresponse){


                if(fbresponse != undefined && !jQuery.isEmptyObject(fbresponse)) {
                    NProgress.start();
                    jsRoutes.controllers.Application.ajaxGetFacebookUsers(fbresponse.to.join()).ajax({
                        success: function (response) {

                            var participants = jQuery.parseJSON(response), $body = "";

                            $.each(participants, function (i) {
                                $body +=
                                    '<div class="friend-item"><a href="#"><img class="smallProfilePicture" src="' + participants[i].picture + '"/><span class="friends-name">' + participants[i].name +
                                    '</span></a><input class="selected-user-data" type="hidden" name="participants[]" value="' + participants[i].id + ',' + participants[i].firstName + ',' + participants[i].lastName + ',' + participants[i].picture + '"/>' +
                                        '<a class="remove-selected" href="#"><img src="/assets/images/close.png"/></a></div>';
                            });
                            $(".challenge-participants-div").html($body);
                            $(':checkbox').checkbox();
                            $("#challenge-participants-wrapper").show();
                            NProgress.done()
                        }
                    });
                }
            });
        }
        else {
            $("#challenge-participants-wrapper").hide();
        }
    });

    $(document).on("click", ".remove-selected", function (e) {
        $(this).parents(".friend-item").remove();

        e.preventDefault();
    });

    $(".challenge-category").change(function () {

        var category = $(".challenge-category").val(), username = $(".current-username").val();

        NProgress.start();

        jsRoutes.controllers.Application.ajaxGetChallengesForCategory(category).ajax({
            success: function (response) {
                var challenges = jQuery.parseJSON(response), body = formChallengesRows(challenges, username);

                $(".challenge-search-results table tbody").html(body);
                $(".switch").bootstrapSwitch();
                NProgress.done();
            }
        });
    });

    $("#friends-filter-input").keyup(function (e) {

        var $value = $(this).val().toLowerCase().replace(/\s+/g, "");

        if ($value.length >= 3) {

            if (e.keyCode == 8) {
                $(".friend-item:hidden").each(function () {
                    if ($(this).find(".friends-name").text().toLowerCase().replace(/\s+/g, "").indexOf($value) >= 0) {
                        $(this).show();
                    }
                });
            }
            else {
                $(".friend-item:visible").each(function () {
                    if ($(this).find(".friends-name").text().toLowerCase().replace(/\s+/g, "").indexOf($value) < 0) {
                        $(this).hide();
                    }
                });
            }
        }
        else {
            $(".friend-item:hidden").show();
        }

    });

    $(document).on("change", ".challenge-status", function () {
        var $this = $(this), id = $this.parents(".switch").find(".challenge-id").val();

        if (!$(this).is(":checked")) {
            alertify.confirm("Are you sure you want to complete the challenge ? Successful responses will be rewarded with points immediately.", function (e) {
                if (e) {
                    NProgress.start();

                    jsRoutes.controllers.Application.ajaxCloseChallenge(id).ajax({
                        success: function (response) {
                            $("#challenges-created").spin(false);
                            NProgress.done();
                        }
                    });

                } else {
                    $this.parents(".switch").bootstrapSwitch('toggleState');
                }
            });
        }
    });


    var formResponses = function (responses) {

        var $body = '';

        $.each(responses, function (i) {
            $body += '<div><a href="' + jsRoutes.controllers.Application.showProfile(responses[i].challengeParticipation.participator.username).url + '">' + responses[i].challengeParticipation.participator.username + '</a><a class="play-video-response" href="#"><img src="' + responses[i].thumbnailUrl + '"/></a>';
            if (typeof responses[i].isAccepted == 'undefined') {
                $body += '<div class="rate-response"><button class="btn btn-danger decline-response">Decline</button><button class="btn btn-success accept-response">Accept</button><input class="response-id" type="hidden" value="' + responses[i].id + '"/><input class="video-id" type="hidden" value="' + responses[i].videoResponseUrl + '"/> </div>';
            }
            $body += '</div>';
        });

        return $body;
    }

    $(".close-window").click(function (e) {
        e.preventDefault();
    });

    $(document).on("click", ".play-video-response", function (e) {

        var $vid_obj = _V_("video-response-player"), responseId = $(this).attr("href"), $parent = $(this).parents(".challenge-response-wrapper");

        NProgress.start();

        jsRoutes.controllers.Application.ajaxGetResponse(responseId).ajax({
            success: function (response) {
                var challengeResponse = jQuery.parseJSON(response), $playerBody = $("#video-response-player_html5_api");

                // hide the current loaded poster
                $("img.vjs-poster").hide();

                // hide the video UI
                $playerBody.hide();

                // and stop it from playing
                $vid_obj.pause();

                // assign the targeted videos to the source nodes
                $("video:nth-child(1)").attr("src", challengeResponse.videoResponseUrl);

                // replace the poster source
                $playerBody.attr("poster", challengeResponse.thumbnailUrl);

                // reset the UI states
                //$(".vjs-big-play-button").show();

                $("#video-response-player").removeClass("vjs-playing").addClass("vjs-paused");


                // load the new sources
                $vid_obj.load();
                $playerBody.show();

                NProgress.done();

                $(".current-response").removeClass("current-response");
                $parent.addClass("current-response");

                var responseDetails = $(".challenge-response-details"), $username = challengeResponse.challengeParticipation.participator.username;

                if(challengeResponse.challengeParticipation.participator.firstName) {
                    $username = challengeResponse.challengeParticipation.participator.firstName + ' ' + challengeResponse.challengeParticipation.participator.lastName.substring(0, 3);
                }

                if(challengeResponse.challengeParticipation.participator.profilePictureUrl != null){
                    responseDetails.find(".medium-profile-picture").attr("src", challengeResponse.challengeParticipation.participator.profilePictureUrl);
                }else{
                    responseDetails.find(".medium-profile-picture").attr("src", "/assets/images/avatar_big.png");
                }
                responseDetails.find(".medium-profile-picture").parents("a").attr('href', jsRoutes.controllers.Application.showProfile(challengeResponse.challengeParticipation.participator.username).url);

                responseDetails.find(".current-response-submitted").html(challengeResponse.submitted);
                responseDetails.find(".response-id").val(challengeResponse.id);
                responseDetails.find(".current-response-author").html('<a href="' + jsRoutes.controllers.Application.showProfile(challengeResponse.challengeParticipation.participator.username).url + '">' + $username + '</a>');
                responseDetails.find(".current-response-description").html(challengeResponse.message);

                if(challengeResponse.isAccepted != 'Y' && challengeResponse.isAccepted != 'N' ){
                    responseDetails.find(".rate-current-response").show();
                }else{
                    responseDetails.find(".rate-current-response").hide();
                }

            }
        });

        e.preventDefault();
    });

    $(document).on("click", ".decline-response", function () {
        var $id = $(this).siblings(".response-id").val(), $parent = $(this).parents(".rate-response");

        NProgress.start();

        jsRoutes.controllers.Application.ajaxDeclineResponse($id).ajax({
            success: function (response) {
                var customResponse = jQuery.parseJSON(response);
                if(customResponse.status == "success") {
                    NProgress.done();
                    $parent.fadeOut(1200);

                    if(customResponse.rewardedPoints > 0) {
                        rewardAllPoints(customResponse.messages, customResponse.points);
                    }
                }
            }
        });


    });

    $(document).on("click", ".accept-response", function () {
        var $id = $(this).siblings(".response-id").val(), $parent = $(this).parents(".rate-response");

        NProgress.start();

        jsRoutes.controllers.Application.ajaxAcceptResponse($id).ajax({
            success: function (response) {
                var customResponse = jQuery.parseJSON(response);
                if(customResponse.status == "success"){
                    NProgress.done();
                    $(".response-id[value='" + $id +"']").parents(".rate-response").fadeOut(1200);

                    if(customResponse.rewardedPoints > 0) {
                        rewardAllPoints(customResponse.messages, customResponse.points);
                    }
                }
            }
        });


    });

    $(document).on("click", ".leave-challenge", function () {
        var challengeId = $(this).siblings(".challenge-id").val(), parent = $(this).parents("tr");

        alertify.confirm("Are you sure you want to forfeit this challenge ?", function (e) {
            if (e) {
                NProgress.start();

                jsRoutes.controllers.Application.ajaxLeaveChallenge(challengeId).ajax({
                    success: function (data) {
                        parent.remove();
                        NProgress.done();
                    }
                });
            }
        });

    });

    $(".close-window-upper-div").click(function (e) {
        e.preventDefault();
        $(".active-participation").removeClass("active-participation");
        $(this).parent().parent().hide();
    });

    $(document).on("click", ".show-upload-response", function () {
        $('#upload-response-wrapper').html('<button type="button" class="btn btn-warning btn-hg">Upload a video response...</button>');
        $("#send-response-wrapper").show();
    });

    $('#upload-response-form').submit(function (e) {

        NProgress.start();

        $(this).ajaxSubmit({
            success: function (response) {
                var customResponse = jQuery.parseJSON(response);

                if (customResponse.hasOwnProperty("status")) {

                    var $activeParticipation = $(".active-participation"), $parentTd = $activeParticipation.find(".leave-challenge").parents("td"),
                        $star = $(".rating-star");

                    $("#send-response-wrapper").hide();
                    $activeParticipation.find(".leave-challenge").remove();
                    $activeParticipation.find(".show-upload-response").remove();

                    $(".show-upload-response").parents("span").remove();
                    $(".challenge-details-name-wrapper").append('<span><img src="/assets/images/correct.png"></span>');
                    $(".timer-parent").remove();

                    $parentTd.html('<img src="/assets/images/done.png"/>');
                    alertify.alert("Response send!");

                    $star.parents('div').tooltip('show');
                    $star.addClass("active-rating-star");

                    if(customResponse.rewardedPoints > 0) {
                        rewardAllPoints(customResponse.messages, customResponse.points);
                    }
                }
                else {
                    var fields = jQuery.parseJSON(response);

                    $.each(fields, function (i) {
                        var errors = fields[i];
                        $.each(errors, function (j) {
                            alertify.error(errors[j].message);
                        });
                    });
                }
                NProgress.done();
            }
        });
        e.preventDefault();

    });

    $(document).on("click", ".stop-player", function (e) {
        var $vid_obj = _V_("video-response-player");
        $vid_obj.pause();
        e.preventDefault();
    });

    $(".show-challenges").click(function (e) {
        var $parent = $(this).parents(".challenges-body");

        $parent.effect("bounce", 300);

        if ($("#my-challenges-wrapper").size() == 0) {
            $(".account-content:visible").effect("drop", 600, function () {
                NProgress.start();
                $("#my-challenges-wrapper").effect("slide", 500);
                jsRoutes.controllers.Application.ajaxGetChallengesContent().ajax({
                    success: function (data) {
                        NProgress.done();
                        $("#content-wrapper").append(data);
                        $(".has-tooltip").tooltip();
                    }
                });
            });
        }else if ($("#my-challenges-wrapper:visible").size() == 0){
            $(".account-content:visible").effect("drop", 600, function () {
                $("#my-challenges-wrapper").effect("slide", 500);
            });
        }

        e.preventDefault();
        e.stopPropagation();
    });

    $(".show-participations").click(function (e) {
        var $parent = $(this).parents(".challenges-body");

        $parent.effect("bounce", 300);

        if ($("#participations-wrapper").size() == 0) {
            $(".account-content:visible").effect("drop", 600, function () {
                NProgress.start();

                $("#participations-wrapper").effect("slide", 500);
                jsRoutes.controllers.Application.ajaxGetParticipationsContent().ajax({
                    success: function (data) {
                        NProgress.done();
                        $("#content-wrapper").append(data);
                        $('.counter').each( function() {
                            var $timeLeft = $(this).parents(".padded-glyph-challenge").find(".time-left").val();
                            $(this).FlipClock($timeLeft, {
                                countdown: true
                            });
                        });
                        $(".has-tooltip").tooltip();
                    }
                });

            });
        }else if ($("#participations-wrapper:visible").size() == 0){
            $(".account-content:visible").effect("drop", 600, function () {
                $("#participations-wrapper").effect("slide", 500);
            });
        }

        e.preventDefault();
        e.stopPropagation();
    });

    $(document).on("click", ".show-profile", function (e) {
        var $parent = $(this).parent();

        if ($("#profile-wrapper").size() == 0) {
            $(".account-content:visible").effect("drop", 600, function () {
                NProgress.start();
                $("#profile-wrapper").effect("slide", 500);
                jsRoutes.controllers.Application.ajaxGetCurrentProfileContent().ajax({
                    success: function (data) {
                        NProgress.done();
                        $("#content-wrapper").append(data);
                    }
                });

            });
        }else if ($("#profile-wrapper:visible").size() == 0){
            $(".account-content:visible").effect("drop", 600, function () {
                $("#profile-wrapper").effect("slide", 500);
            });
        }

        e.preventDefault();
        e.stopPropagation();
    });

    $(document).on("click", ".join-challenge-action", function () {

        var $this = $(this), $parent = $this.parents("span"), challengeId = $("#challenge-details").find(".challenge-id").val();

        NProgress.start();

        jsRoutes.controllers.Application.ajaxJoinChallenge(challengeId).ajax({
            success: function (response) {
                $this.remove();
                $parent.html('<input type="button" class="btn btn-hg btn-warning show-upload-response" value="Respond"/>');
                NProgress.done();
                alertify.success("You joined the competition!");
            }
        });

    });

    $(document).on("click", ".remove-participant", function () {
        var $parent = $(this).parents("tr"), participantUsername = $parent.find(".participant-username").val(), challengeId = $(".current-challenge-id").val(), $name = $(this).parents("tr").find(".participant-name-span").text();

        alertify.confirm("Are you sure you want to remove " + $name + " from this challenge ?", function (e) {
            if (e) {
                NProgress.start();

                jsRoutes.controllers.Application.ajaxRemoveParticipantFromChallenge(challengeId, participantUsername, $name).ajax({
                    success: function (response) {
                        $parent.remove();
                        NProgress.done();
                        alertify.success($name + " was dismissed from the challenge.");
                    }
                });
            }
        });

    });

    $(".quick-search-action").click(function(){
        var $phrase = $(".challenge-search-phrase").val();

        if(!$phrase.trim()){
            window.location.href = jsRoutes.controllers.Application.showBrowseChallenges().url;
        }else {
            window.location.href = jsRoutes.controllers.Application.showBrowseChallengesWithData($phrase).url;
        }
    });

    $(".go-to-step-two").click(function(){
        $( ".step-one-arrow" ).show("blind", 500, function(){
            $( ".step-two-block" ).show("blind", 500);
        });
    });

    $(".go-to-step-three").click(function(){
        $( ".step-two-arrow" ).show("blind", 500, function(){
            $( ".step-three-block" ).show("blind", 500);
        });
    });


    $('.counter').each( function() {
        var $timeLeft = $(this).parents(".timer-parent").find(".time-left").val();
        $(this).FlipClock($timeLeft, {
            countdown: true
        });
    });

    $('.small-counter').each( function() {
        var $timeLeft = $(this).parents(".timer-parent").find(".time-left").val();
        $(this).FlipClock($timeLeft, {
            countdown: true
        });
    });

    var rewardPoints = function(rewarded, message){

        $(".rewarded-points-wrapper").fadeOut(300);

        var $counter = $(".menu-point-counter"), $points = parseInt($counter.text()) + rewarded;

        $counter.clearQueue();

        $(".current-points-wrapper").fadeOut(300);

        $(".points-number-wrapper").animate({width: "300px"}, 300, function(){

            $(".rewarded-points-message").text(message);
            $(".rewarded-points-number").text("+" + rewarded);

            $(".rewarded-points-wrapper").fadeIn(300);

            $(".menu-point-counter").text($points);

            //$(".menu-point-counter").effect("bounce", { distance: 25}, 500);
        });
    }

    var rewardAllPoints = function(messages, points){

        var $endTimeout = 5000 * messages.length;

        var $timeout = 5000;

        if(messages.length > 0){
            rewardPoints(points[0], messages[0]);

            if(messages.length > 1){
                setTimeout(function(){rewardPoints(points[1], messages[1])}, 5000);

                if(messages.length > 2){
                    setTimeout(function(){rewardPoints(points[2], messages[2])}, 10000);
                }
            }
        }

        setTimeout(function(){
            $(".rewarded-points-wrapper").fadeOut(300, function(){
                $(".points-number-wrapper").css("width", "auto");
                $(".current-points-wrapper").fadeIn(300);
            });
        }, $endTimeout);
    }

    $(document).on("mouseenter", ".active-rating-star", function(){
        $(this).prevAll(".active-rating-star").addClass("active-full-star");
        $(this).nextAll(".active-rating-star").addClass("active-no-star");
        $(this).addClass("active-full-star");
    });

    $(document).on("mouseleave", ".active-rating-star", function(){
        $(this).prevAll(".active-rating-star").removeClass("active-full-star");
        $(this).nextAll(".active-rating-star").removeClass("active-no-star");
        $(this).removeClass("active-full-star");
    });

    $(document).on("click", ".active-rating-star", function(){
        var $challengeId = $(".challenge-id").val(), $rating = $(".active-full-star").size();

        $(document).off("mouseenter", ".active-rating-star");
        $(document).off("mouseleave", ".active-rating-star");
        $(document).off("click", ".active-rating-star");

        jsRoutes.controllers.Application.ajaxRateChallenge($challengeId, $rating).ajax({
            success: function (response) {}
        });
    });

    $(".new-comment-form").submit(function (e) {

        $(this).ajaxSubmit({
            success: function (response) {
                var customResponse = jQuery.parseJSON(response);

                if (customResponse.hasOwnProperty("status")) {
                    var $comments = $(".comments-block"), $newComment = "", $currentName = $("#current-username").val(),
                        $profilePictureUrl = $("#menu-wrapper").find("img.smallProfilePicture").attr("src"),
                        $message = $(".new-comment-form").find("textarea").val();

                    $newComment += '<div class="comment newly-added-comment" style="display: none"><div class="comment-author">' +
                        '<a href="' + jsRoutes.controllers.Application.showProfile($currentName).url + '">' +
                        '<img class="smallProfilePicture" src="' + $profilePictureUrl + '"/>' + $currentName +
                        '</a></div><div class="comment-message">' + $message + '</div></div>';

                    $comments.prepend($newComment);
                    $comments.find(".newly-added-comment").show("slow");
                }
                else {
                    var fields = jQuery.parseJSON(response);

                    $.each(fields, function (i) {
                        var errors = fields[i];
                        $.each(errors, function (j) {
                            alertify.error(errors[j].message);
                        });
                    });
                }
            }
        });
        e.preventDefault();
    });

    $(document).on("click", ".show-more-participants", function(){

        var $this= $(this), $challengeId = $(".current-challenge-id").val(), $offset = $(".current-offset").val(), $body = ""
            , $newOffset = parseInt($offset)+ 1, $participantsNr = $("#participants-number").val(), $currentParticipants = $("#current-participants-number");

        jsRoutes.controllers.Application.ajaxShowMoreParticipants($challengeId, $offset).ajax({
            success: function (response) {
                var $currentCreator = $(".remove-participant").size() > 0,
                    $table = $(".challenge-participants-wrapper").find("table tbody");

                $(".current-offset").val($newOffset);

                $table.append(response);
                $table.find(".appended-row").show("normal");

                $currentParticipants.val($table.find("tr").length);

                $(".has-tooltip").tooltip();

                if($participantsNr <= $currentParticipants.val()){
                    $this.hide();
                }
            }
        });
    });

    $(document).on("click", ".show-more-participations", function(){

        var $this= $(this), $offset = $(".current-offset").val(), $body = "", $newOffset = parseInt($offset)+ 1,
            $participationsNr = $("#participations-number").val(), $currentParticipations = $("#current-participations-number"),
            $wrapper = $(".padded-glyph-challenges-wrapper");

        jsRoutes.controllers.Application.ajaxShowMoreParticipations($offset).ajax({
            success: function (response) {

                $(".current-offset").val($newOffset);

                $wrapper.append(response);
                $wrapper.find(".padded-glyph-challenge").show("normal");

                $currentParticipations.val($wrapper.find(".padded-glyph-challenge").length);

                if($participationsNr <= $currentParticipations.val()){
                    $this.hide();
                }

                $(".has-tooltip").tooltip();

                $('.counter').not(".flip-clock-wrapper").each( function() {
                    var $timeLeft = $(this).parents(".padded-glyph-challenge").find(".time-left").val();
                    $(this).FlipClock($timeLeft, {
                        countdown: true
                    });
                });
            }
        });
    });

    $(document).on("click", ".show-more-challenges", function(){

        var $this= $(this), $offset = $(".current-offset").val(), $body = "", $newOffset = parseInt($offset)+ 1,
            $challengesNr = $("#challenges-number").val(), $currentChallenges = $("#current-challenges-number"),
            $wrapper = $(".padded-glyph-challenges-wrapper");

        jsRoutes.controllers.Application.ajaxShowMoreChallenges($offset).ajax({
            success: function (response) {

                $(".current-offset").val($newOffset);

                $wrapper.append(response);
                $wrapper.find(".padded-glyph-challenge").show("normal");

                $currentChallenges.val($wrapper.find(".padded-glyph-challenge").length);

                if($challengesNr <= $currentChallenges.val()){
                    $this.hide();
                }

                $(".has-tooltip").tooltip();
            }
        });
    });

    $(document).on("click", ".show-more-comments", function(){

        var $this= $(this), $offset = $(".current-offset").val(), $body = "", $newOffset = parseInt($offset)+ 1,
            $commentsNr = $("#comments-number").val(), $currentComments = $("#current-comments-number"),
            $wrapper = $(".comments-block"), $challengeId = $(".current-challenge-id").val();

        jsRoutes.controllers.Application.ajaxShowMoreComments($challengeId, $offset).ajax({
            success: function (response) {
                $(".current-offset").val($newOffset);

                $wrapper.append(response);
                $wrapper.find(".comment").show("normal");

                $currentComments.val($wrapper.find(".comment").length);

                if($commentsNr <= $currentComments.val()){
                    $this.hide();
                }
            }
        });
    });

    $(document).on("click", ".show-more-notifications", function(){

        var $this= $(this), $offset = $(".current-offset").val(), $body = "", $newOffset = parseInt($offset)+ 1,
            $notificationsNr = $("#notifications-number").val(), $currentNotifications = $("#current-notifications-number"),
            $wrapper = $(".user-notification-wrapper").find("table tbody");

        jsRoutes.controllers.Application.ajaxShowMoreNotifications($offset).ajax({
            success: function (response) {
                $(".current-offset").val($newOffset);

                $wrapper.append(response);
                $wrapper.find("tr").show("normal");

                $currentNotifications.val($wrapper.find("tr").length);

                if($notificationsNr <= $currentNotifications.val()){
                    $this.hide();
                }
            }
        });
    });

});