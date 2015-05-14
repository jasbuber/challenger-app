/**
 * Created by jasbuber on 2015-04-29.
 */
$(document).ready(function(){
    $(".modal").modal({
        backdrop: 'static',
        keyboard: false
    });
    
    $(".tutorial-next-button").on("click", function(){
        var activeMessage = $(".active-message"), nextMessage = activeMessage.next(".tutorial-message-wrapper"),
            counter = $("span.step-number");

        if(nextMessage.hasClass("next-bg")){
            var $screenshotDiv = $("div.tutorial-screenshot");

            $screenshotDiv.attr("class", "tutorial-screenshot");
            $screenshotDiv.addClass(nextMessage.find(".step").val());
        }
        activeMessage.removeClass("active-message");
        activeMessage.hide();
        nextMessage.addClass("active-message");
        nextMessage.show();
        counter.text(parseInt(counter.text()) + 1);

        if(nextMessage.hasClass("last-message")){
            $(".tutorial-next-button").hide();
            $(".tutorial-complete-button").show();
        }
        $(".tutorial-back-button").show();
    });

    $(".tutorial-back-button").on("click", function(){
        var activeMessage = $(".active-message"), nextMessage = activeMessage.prev(".tutorial-message-wrapper"),
            counter = $("span.step-number");

        if(nextMessage.hasClass("next-bg")){
            var $screenshotDiv = $("div.tutorial-screenshot");

            $screenshotDiv.attr("class", "tutorial-screenshot");
            $screenshotDiv.addClass(nextMessage.find(".step").val());
        }
        activeMessage.removeClass("active-message");
        activeMessage.hide();
        nextMessage.addClass("active-message");
        nextMessage.show();
        counter.text(parseInt(counter.text()) - 1);
        $(".tutorial-next-button").show();
        $(".tutorial-complete-button").hide();

        if(nextMessage.index() == 0){
            $(this).hide();
        }
    });

    $(".tutorial-complete-button").on("click", function(){
        $(this).attr("disabled", "disabled");
        jsRoutes.controllers.Application.ajaxCompleteTutorial().ajax({
            success: function (response) {
                $(".modal").modal("hide");
                $("#tutorial-wrapper").remove();
            }
        });
    });

    $(".close-tutorial").on("click", function() {
        alertify.confirm("Do You want to skip tutorial ?", function (e) {
            if (e) {
                jsRoutes.controllers.Application.ajaxCompleteTutorial().ajax({
                    success: function (response) {
                        $(".modal").modal("hide");
                        $("#tutorial-wrapper").remove();
                    }
                });
            }
        });
    });
});
