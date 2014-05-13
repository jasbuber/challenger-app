/**
 * Created by Tomash on 04.04.14.
 */
$(document).ready(function(){

    $(".ui-block").css("height", $(".ui-block").width()/1.6);
    $(".ui-block").show();

    $(window).on("resize", function(){
        $(".ui-block").css("height", $(".ui-block").width()/1.2);
    });

    $(".ui-block").click(function(){
        $(this).parents("#wrapper").height($(this).parents("#wrapper").height());
        $(this).toggleClass('active');
        $(this).siblings().not(this).toggleClass('hide');
        $(this).fadeOut(1500, function(){
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

    $(".backAction").click(function(){
        $(".ui-block-body").hide();
        $(".ui-block").removeClass('hide');
        $(".ui-block").fadeIn(1000);
    });

    var formChallengesRows = function(challenges){
        var $body = "";

        $.each(challenges, function(i) {

            $body += '<tr>' +
                '<td><a href="#"><img src="' + challenges[i].creator.profilePictureUrl + '"/>' + challenges[i].creator.username + '</a></td>' +
                '<td>' + challenges[i].challengeName + '</td><td>' + challenges[i].category + '</td><td>time left</td>' +
                '<td><div class="switch switch-square"><input type="checkbox" unchecked data-toggle="switch" /><input type="hidden" class="challenge-id" value="' + challenges[i].id + '"/></div></td></tr>';
        });
        return $body;
    }

    $("#browse-block-body .form-wrapper form").submit(function(e){

        $(".challenge-search-results").spin();

        $.ajax({
            url: "/challenge/ajax/search",
            data: {
                phrase: $(".challenge-search-phrase").val(),
                category: $(".challenge-category").val()
            },
            method: "get"
        }).done(function(response){
            var challenges = jQuery.parseJSON(response), body = formChallengesRows(challenges);

            $(".challenge-search-results table tbody").html(body);
            $(".switch").bootstrapSwitch();
            $(".challenge-search-results").spin(false);
        })
        e.preventDefault();
    });

    $("#browse-block").click(function(e){

        $(".challenge-search-results").spin();

        $.ajax({
            url: "/challenge/ajax/latest",
            method: "get"
        }).done(function(response){
            var challenges = jQuery.parseJSON(response), body = formChallengesRows(challenges);

            $(".challenge-search-results table tbody").html(body);
            $(".switch").bootstrapSwitch();
            $(".challenge-search-results").spin(false);
        })
        e.preventDefault();
    });

    $(document).on("change", ".challenge-search-results .switch input[type=checkbox]", function(){

        var $this = $(this);
        $(".challenge-search-results").spin();

        $.ajax({
            url: "/challenge/ajax/participate",
            data: {
                id: $(this).parents(".switch").find(".challenge-id").val(),
                state: $(this).is(":checked")
            },
            method: "get"
        }).done(function(response){
            if(response === "success" && $this.is(":checked")) {
                alertify.success("You joined the competition!");
            }
            else{ alertify.error("You left the competition...");}
            $(".challenge-search-results").spin(false);
        })
    });

    var wrapper = $('<div/>').css({height:0,width:0,'display':'none'});
    var fileInput = $(':file').wrap(wrapper);

    fileInput.change(function(){
        $this = $(this);
        if($this.val()!= '') {
            $('#video-input-wrapper').html(' <div id="video-screenshot"><img src="/assets/images/video.png"/>' + $this.val().replace(/.*(\/|\\)/, '') + "</div>");
        }
        else{
            $('#video-input-wrapper').html('<button id="upload-video-action" type="button" class="btn btn-warning btn-hg">Upload a video description...</button>');
        }
    });

    $('#video-input-wrapper').click(function(){
        fileInput.click();
    }).show();

    $('#create-challenge-form').submit(function(e) {

        $("#challenge-block-body").spin();
        $(this).ajaxSubmit({
            success: function(response){
                if(response == "success") {
                    alertify.alert("Challenge created and ready to join ! ", function (e) {
                        if (e) {
                            window.location = "/";
                        }
                    });
                }
                else{
                    var fields = jQuery.parseJSON(response);

                    $.each(fields, function(i) {
                        var errors = fields[i];
                        $.each(errors, function(j) {
                            alertify.error(errors[j].message);
                        });
                    });
                }
                $("#challenge-block-body").spin(false);
            }
        });
        e.preventDefault();

    });

    $("#challenge-visibility").change(function(e) {

        if ($(this).val() == 0) {
            $("#challenge-participants-wrapper").show();
            $("#challenge-participants-wrapper").spin();

            $.ajax({
                url: "/challenge/ajax/participants",
                method: "POST"
            }).done(function(response){
                var participants = jQuery.parseJSON(response), $body = "";
                $("#challenge-participants-wrapper").spin(false);

                $.each(participants, function(i) {

                    var pictureUrl = jQuery.parseJSON(participants[i].picture).data.url;
                    $body +=
                        '<li class="friend-item"><a href="#"><img src="' + pictureUrl + '"/><span class="friends-name">' + participants[i].name +
                        '</span></a><div class="switch switch-square"><input type="checkbox" value="' + participants[i].username + '" name="participants[]" unchecked data-toggle="switch" /></div></li>';
                });
                $("#challenge-participants").html($body);
                $(".switch").bootstrapSwitch();
            })
        }
        else {
            $("#challenge-participants-wrapper").hide();
        }
    });

    $(".challenge-category").change(function(e){

        $(".challenge-search-results").spin();

        $.ajax({
            url: "/challenge/ajax/searchbycategory",
            data: {
                category: $(".challenge-category").val()
            },
            method: "get"
        }).done(function(response){
            var challenges = jQuery.parseJSON(response), body = formChallengesRows(challenges);

            $(".challenge-search-results table tbody").html(body);
            $(".switch").bootstrapSwitch();
            $(".challenge-search-results").spin(false);
        });
    });

    $("#friends-filter-input").keyup(function(e){

        var $value = $(this).val().toLowerCase().replace(/\s+/g, "");

        if($value.length >= 3) {

            if (e.keyCode == 8) {
                $(".friend-item:hidden").each(function () {
                    if ($(this).find(".friends-name").text().toLowerCase().replace(/\s+/g, "").indexOf($value) >= 0) {
                        $(this).show();
                    }
                });
            }
            else{
                $(".friend-item:visible").each(function(){
                    if($(this).find(".friends-name").text().toLowerCase().replace(/\s+/g, "").indexOf($value) < 0){
                        $(this).hide();
                    }
                });
            }
        }
        else{ $(".friend-item:hidden").show(); }

    });

    $(".challenge-tab-button").click(function(){
        var $parentId = $(this).parent().attr("id");

        console.log($parentId);
        $(".challenge-tab-button").removeClass("selected");
        $(this).addClass("selected");
        $(".challenges-body").hide();
        $("." + $parentId + "-body").fadeIn(300);

    });


});