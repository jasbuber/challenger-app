/**
 * Created by Tomash on 04.04.14.
 */
$(document).ready(function () {

    $(".ui-block").css("height", $(".ui-block").width() / 1.6);
    $(".ui-block").show();

    $(window).on("resize", function () {
        $(".ui-block").css("height", $(".ui-block").width() / 1.2);
    });

    $(".ui-block").click(function () {
        $(this).parents("#wrapper").height($(this).parents("#wrapper").height());
        $(this).toggleClass('active');
        $(this).siblings().not(this).toggleClass('hide');
        $(this).fadeOut(1500, function () {
            $(this).removeClass("active");
            $("#" + $(this).attr("id") + "-body").removeClass('hide');
            $("#" + $(this).attr("id") + "-body").fadeIn(1000);

        });
    });

    $("select").selectpicker({style: 'btn-hg btn-info', menuStyle: 'dropdown-inverse'});

    // Focus state for append/prepend inputs
    $('.input-group').on('focus', '.form-control', function () {
        $(this).closest('.input-group, .form-group').addClass('focus');
    }).on('blur', '.form-control', function () {
        $(this).closest('.input-group, .form-group').removeClass('focus');
    });

    $(".backAction").click(function () {
        $(".ui-block-body").hide();
        $(".ui-block").removeClass('hide');
        $(".ui-block").fadeIn(1000);
    });

    var formChallengesRows = function (challenges) {
        var $body = "";


        $.each(challenges, function (i) {
            $body += '<tr>' +
                '<td class="profilePicTd"><a href="' + jsRoutes.controllers.Application.showProfile(challenges[i].creator.username).url + '"><img src="' + ((challenges[i].creator.profilePictureUrl != null) ? challenges[i].creator.profilePictureUrl : "/assets/images/avatar_small.png") + '"/>' + challenges[i].creator.username + '</a></td>' +
                '<td>' + challenges[i].challengeName + '</td><td>' + challenges[i].category + '</td><td>time left</td>' +
                '<td><div class="switch switch-square"><input type="checkbox" unchecked data-toggle="switch" /><input type="hidden" class="challenge-id" value="' + challenges[i].id + '"/></div></td></tr>';
        });
        return $body;
    };

    $("#browse-block-body .form-wrapper form").submit(function (e) {

        var phrase = $(".challenge-search-phrase").val(), category = $(".challenge-category").val();

        $(".challenge-search-results").spin();

        jsRoutes.controllers.Application.ajaxGetChallengesForCriteria(phrase, category).ajax({
            success: function (response) {
                var challenges = jQuery.parseJSON(response), body = formChallengesRows(challenges);

                $(".challenge-search-results table tbody").html(body);
                $(".switch").bootstrapSwitch();
                $(".challenge-search-results").spin(false);
            }
        });

        e.preventDefault();
    });

    $("#browse-block").click(function (e) {

        $(".challenge-search-results").spin();

        jsRoutes.controllers.Application.ajaxGetLatestChallenges().ajax({
            success: function (response) {
                var challenges = jQuery.parseJSON(response), body = formChallengesRows(challenges);

                $(".challenge-search-results table tbody").html(body);
                $(".switch").bootstrapSwitch();
                $(".challenge-search-results").spin(false);
            }
        });

        e.preventDefault();
    });

    $(document).on("change", ".challenge-search-results .switch input[type=checkbox]", function () {

        var $this = $(this), id = $(this).parents(".switch").find(".challenge-id").val(), state = $(this).is(":checked");
        $(".challenge-search-results").spin();

        jsRoutes.controllers.Application.ajaxChangeChallengeParticipation(id, state).ajax({
            success: function (response) {
                if (response === "success" && $this.is(":checked")) {
                    alertify.success("You joined the competition!");
                }
                else {
                    alertify.error("You left the competition...");
                }
                $(".challenge-search-results").spin(false);
            }
        });
    });

    var wrapper = $('<div/>').css({height: 0, width: 0, 'display': 'none'});
    var fileInput = $(':file').wrap(wrapper);

    fileInput.change(function () {
        var $this = $(this);
        if ($this.val() != '') {
            $('#video-input-wrapper').html(' <div id="video-screenshot"><img src="/assets/images/video.png"/>' + $this.val().replace(/.*(\/|\\)/, '') + "</div>");
        }
        else {
            $('#video-input-wrapper').html('<button id="upload-video-action" type="button" class="btn btn-warning btn-hg">Upload a video description...</button>');
        }
    });

    var uploadResponse = $(':file').wrap(wrapper);

    $('#video-input-wrapper').click(function () {
        fileInput.click();
    }).show();

    uploadResponse.change(function () {
        var $this = $(this);
        if ($this.val() != '') {
            $('#upload-response-wrapper').html(' <div><img src="/assets/images/video.png"/>' + $this.val().replace(/.*(\/|\\)/, '') + "</div>");
        }
        else {
            $('#upload-response-wrapper').html('<button type="button" class="btn btn-warning">Submit a response.</button>');
        }
    });

    $('#upload-response-wrapper').click(function () {
        uploadResponse.click();
    }).show();

    $('#create-challenge-form').submit(function (e) {

        $("#challenge-block-body").spin();
        $(this).ajaxSubmit({
            success: function (response) {
                if (response == "success") {
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
                $("#challenge-block-body").spin(false);
            }
        });
        e.preventDefault();

    });

    $("#challenge-visibility").change(function (e) {

        if ($(this).val() == 0) {
            $("#challenge-participants-wrapper").show();
            $("#challenge-participants-wrapper").spin();

            jsRoutes.controllers.Application.ajaxGetFacebookFriends().ajax({
                success: function (response) {
                    var participants = jQuery.parseJSON(response), $body = "";
                    $("#challenge-participants-wrapper").spin(false);

                    $.each(participants, function (i) {

                        var pictureUrl = jQuery.parseJSON(participants[i].picture).data.url;
                        $body +=
                            '<li class="friend-item"><a href="#"><img src="' + pictureUrl + '"/><span class="friends-name">' + participants[i].name +
                            '</span></a><div class="switch switch-square"><input type="checkbox" value="' + participants[i].username + '" name="participants[]" unchecked data-toggle="switch" /></div></li>';
                    });
                    $("#challenge-participants").html($body);
                    $(".switch").bootstrapSwitch();
                }
            });
        }
        else {
            $("#challenge-participants-wrapper").hide();
        }
    });

    $(".challenge-category").change(function (e) {

        var category = $(".challenge-category").val();

        $(".challenge-search-results").spin();

        $.ajax({
            url: "/challenge/ajax/searchbycategory",
            data: {
                category: $(".challenge-category").val()
            },
            method: "get"
        }).done(function (response) {
            var challenges = jQuery.parseJSON(response), body = formChallengesRows(challenges);

            $(".challenge-search-results table tbody").html(body);
            $(".switch").bootstrapSwitch();
            $(".challenge-search-results").spin(false);
        });

        jsRoutes.controllers.Application.ajaxGetChallengesForCategory(category).ajax({
            success: function (response) {
                var challenges = jQuery.parseJSON(response), body = formChallengesRows(challenges);

                $(".challenge-search-results table tbody").html(body);
                $(".switch").bootstrapSwitch();
                $(".challenge-search-results").spin(false);
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

    /*
    $(".challenge-tab-button").click(function () {
        var $parentId = $(this).parent().attr("id"), $tab = $("." + $parentId + "-body"), $tbody = $tab.find("table tbody");

        $(".challenge-tab-button").removeClass("selected");
        $(this).addClass("selected");
        $(".challenges-body").hide();
        $tab.fadeIn(300);

        if (!$.trim($tbody.html())) {
            $tab.spin();

            if ($parentId == "participating-tab") {
                jsRoutes.controllers.Application.ajaxGetUserParticipations().ajax({
                    success: function (data) {
                        $tab.spin(false);
                        $tbody.html(formParticipationsRows(jQuery.parseJSON(data)));
                    }
                });
            }
            else if ($parentId == "completed-tab") {
                jsRoutes.controllers.Application.ajaxGetCompletedChallenges().ajax({
                    success: function (data) {
                        $tab.spin(false);
                        $tbody.html(formCompletedChallengeRows(jQuery.parseJSON(data)));
                    }
                });
            }
        }

    });
*/
    $(document).on("change", ".challenge-status", function () {
        var $this = $(this), id = $this.parents(".switch").find(".challenge-id").val();

        if (!$(this).is(":checked")) {
            alertify.confirm("Are you sure you want to complete the challenge ? Successful responses will be rewarded with points immediately.", function (e) {
                if (e) {
                    $("#challenges-created").spin();

                    jsRoutes.controllers.Application.ajaxCloseChallenge(id).ajax({
                        success: function (response) {
                            $("#challenges-created").spin(false);
                            $this.parents("tr").hide();
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

    /*
    $(".show-challenge-events").click(function (e) {

        var $this = $(this), challengeId = $this.attr("href");

        $("#challenges-created").spin();

        jsRoutes.controllers.Application.ajaxGetResponsesForChallenge(challengeId).ajax({
            success: function (response) {
                var responses = jQuery.parseJSON(response);
                $("#challenges-created").spin(false);

                $("#responses-div").html(formResponses(responses));
                $(".challenge-events").show();
                $(".challenge-events").find(".challenge-events-title").text($this.text());
            }
        });

        e.preventDefault();
    });*/

    $(".close-window").click(function (e) {
        e.preventDefault();
    });

    $(document).on("click", ".play-video-response", function (e) {

        var $vid_obj = _V_("video-response-player"), responseId = $(this).attr("href"), $parent = $(this).parents(".challenge-response-wrapper");

        $("#video-panel").spin();
        jsRoutes.controllers.Application.ajaxGetResponse(responseId).ajax({
            success: function (response) {
                var challengeResponse = jQuery.parseJSON(response);

                // hide the current loaded poster
                $("img.vjs-poster").hide();

                // hide the video UI
                $("#video-response-player_html5_api").hide();

                // and stop it from playing
                $vid_obj.pause();

                // assign the targeted videos to the source nodes
                $("video:nth-child(1)").attr("src", challengeResponse.videoResponseUrl);

                // replace the poster source
                $("#video-response-player_html5_api").attr("poster", challengeResponse.thumbnailUrl);

                // reset the UI states
                //$(".vjs-big-play-button").show();

                $("#video-response-player").removeClass("vjs-playing").addClass("vjs-paused");


                // load the new sources
                $vid_obj.load();
                $("#video-response-player_html5_api").show();

                $("#video-panel").spin(false);

                $(".current-response").removeClass("current-response");
                $parent.addClass("current-response");

                var responseDetails = $(".challenge-response-details");

                if(challengeResponse.challengeParticipation.participator.profilePictureUrl != null){
                    responseDetails.find(".medium-profile-picture").attr("src", challengeResponse.challengeParticipation.participator.profilePictureUrl);
                }else{
                    responseDetails.find(".medium-profile-picture").attr("src", "/assets/images/avatar_big.png");
                }
                responseDetails.find(".medium-profile-picture").parents("a").attr('href', jsRoutes.controllers.Application.showProfile(challengeResponse.challengeParticipation.participator.username).url);

                responseDetails.find(".current-response-submitted").html(challengeResponse.submitted);
                responseDetails.find(".response-id").val(challengeResponse.id);
                responseDetails.find(".current-response-author").html('<a href="' + jsRoutes.controllers.Application.showProfile(challengeResponse.challengeParticipation.participator.username).url + '">' + challengeResponse.challengeParticipation.participator.username+ '</a>');

                if(challengeResponse.isAccepted != 'Y' && challengeResponse.isAccepted != 'N' ){
                    responseDetails.find(".rate-current-response").show();
                }else{
                    responseDetails.find(".rate-current-response").hide();
                }

            }
        });

        e.preventDefault();
    });

    $(document).on("click", ".decline-response", function (e) {
        var $id = $(this).siblings(".response-id").val(), $parent = $(this).parents(".rate-response");

        $(".challenge-events").spin();

        jsRoutes.controllers.Application.ajaxDeclineResponse($id).ajax({
            success: function (data) {
                $(".challenge-events").spin(false);
            }
        });

        $parent.fadeOut(1200);
    });

    $(document).on("click", ".accept-response", function (e) {
        var $id = $(this).siblings(".response-id").val(), $parent = $(this).parents(".rate-response");

        $(".challenge-events").spin();

        jsRoutes.controllers.Application.ajaxAcceptResponse($id).ajax({
            success: function (data) {
                $(".challenge-events").spin(false);
            }
        });

        //$parent.fadeOut(1200);
        $(".response-id[value='" + $id +"']").parents(".rate-response").fadeOut(1200);
    });

    /*
    var formParticipationsRows = function (participations) {
        var $body = "";

        $.each(participations, function (i) {
            $body += '<tr><td>' + participations[i][0].challenge.challengeName + '</td><td>' + $.formatDateTime('mm/dd/y g:ii a', new Date(participations[i][0].joined)) +
                '</td><td>time left</td>';
            if (participations[i][1] != null) {
                $body += '<td><img src="/assets/images/done.png"/></td><td></td>';
            } else {
                $body += '<td><button type="button" class="btn btn-warning show-upload-response">Submit a response</button></td>' +
                    '<td><button type="button" class="btn btn-danger leave-challenge">Forfeit</button><input type="hidden" class="challenge-id" value="' + participations[i][0].challenge.id + '"/></div></td></tr>';
            }
        });
        return $body;
    };*/

    $(document).on("click", ".leave-challenge", function () {
        var challengeId = $(this).siblings(".challenge-id").val(), parent = $(this).parents("tr");

        alertify.confirm("Are you sure you want to forfeit this challenge ?", function (e) {
            if (e) {
                $(".participating-tab-body").spin();

                jsRoutes.controllers.Application.ajaxLeaveChallenge(challengeId).ajax({
                    success: function (data) {
                        parent.remove();
                        $(".participating-tab-body").spin(false);
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

        $("#send-response-wrapper").spin();
        $(this).ajaxSubmit({
            success: function (response) {
                if (response == "success") {

                    var $parentTd = $(".active-participation").find(".leave-challenge").parents("td");

                    $("#send-response-wrapper").hide();
                    $(".active-participation").find(".leave-challenge").remove();
                    $(".active-participation").find(".show-upload-response").remove();

                    $parentTd.html('<img src="/assets/images/done.png"/>');
                    alertify.alert("Response send!");
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
                $("#send-response-wrapper").spin(false);
            }
        });
        e.preventDefault();

    });

    /*
    var formCompletedChallengeRows = function (completedChallenges) {
        var $body = "";

        $.each(completedChallenges, function (i) {
            $body += '<tr><td>' + completedChallenges[i].creator.username + '</td><td>' + completedChallenges[i].challengeName + '</td><td>' +
                $.formatDateTime('mm/dd/y g:ii a', new Date(completedChallenges[i].creationDate)) + '</td><td>' + $.formatDateTime('mm/dd/y g:ii a', new Date(completedChallenges[i].endingDate)) + '</td><tr/>';
        });
        return $body;
    };*/

    $(document).on("click", ".stop-player", function (e) {
        var $vid_obj = _V_("video-response-player");
        $vid_obj.pause();
        e.preventDefault();
    });

    $(".show-challenges").click(function (e) {
        var $parent = $(this).parents(".challenges-body"), options = {top: "40%", left: "36%"};

        $parent.effect("bounce", 300);

        if ($("#my-challenges-wrapper").size() == 0) {
            console.log("ble1");
            $(".account-content:visible").effect("drop", 600, function () {
                $("#content-wrapper").spin(options);
                $("#my-challenges-wrapper").effect("slide", 500);
                jsRoutes.controllers.Application.ajaxGetChallengesContent().ajax({
                    success: function (data) {
                        $("#content-wrapper").spin(false);
                        $("#content-wrapper").append(data);
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
        var $parent = $(this).parents(".challenges-body"), options = {top: "40%", left: "36%"};

        $parent.effect("bounce", 300);

        if ($("#participations-wrapper").size() == 0) {
            $(".account-content:visible").effect("drop", 600, function () {
                $("#content-wrapper").spin(options);
                $("#participations-wrapper").effect("slide", 500);
                jsRoutes.controllers.Application.ajaxGetParticipationsContent().ajax({
                    success: function (data) {
                        $("#content-wrapper").spin(false);
                        $("#content-wrapper").append(data);
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
        var $parent = $(this).parent(), options = {top: "40%", left: "36%"};

        if ($("#profile-wrapper").size() == 0) {
            $(".account-content:visible").effect("drop", 600, function () {
                $("#content-wrapper").spin(options);
                $("#profile-wrapper").effect("slide", 500);
                jsRoutes.controllers.Application.ajaxGetCurrentProfileContent().ajax({
                    success: function (data) {
                        $("#content-wrapper").spin(false);
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

        jsRoutes.controllers.Application.ajaxJoinChallenge(challengeId).ajax({
            success: function (response) {
                $this.remove();
                $parent.html('<a class="respond-challenge-action" href="' + challengeId + '"><input type="button" class="btn btn-hg btn-warning" value="Respond"/></a>');
                alertify.success("You joined the competition!");
            }
        });

    });
/*
    $(document).on("click", ".show-challenge", function (e) {
        var $parent = $(this).parent(), options = {top: "40%", left: "36%"}, id = $(this).attr("href");

        $(".account-content:visible").effect("drop", 600, function () {
            $("#challenge-wrapper").remove();
            $("#content-wrapper").spin(options);
            $("#challenge-wrapper").effect("slide", 500);
            jsRoutes.controllers.Application.ajaxGetChallengeContent(id).ajax({
                success: function (data) {
                    $("#content-wrapper").spin(false);
                    $("#content-wrapper").append(data);
                }
            });

        });

        e.preventDefault();
        e.stopPropagation();
    });*/

    $(document).on("click", ".remove-participant", function () {
        var $parent = $(this).parents("tr"), participantUsername = $parent.find(".participant-username").val(), challengeId = $(".current-challenge-id").val();

        alertify.confirm("Are you sure you want to remove " + participantUsername + " from this challenge ?", function (e) {
            if (e) {
                $(".padded-glyph-challenge-data-wrapper").spin();

                jsRoutes.controllers.Application.ajaxRemoveParticipantFromChallenge(challengeId, participantUsername).ajax({
                    success: function (response) {
                        $parent.remove();
                        $(".padded-glyph-challenge-data-wrapper").spin(false);
                        alertify.success(participantUsername + " was dismissed from the challenge.");
                    }
                });
            }
        });

    });

});