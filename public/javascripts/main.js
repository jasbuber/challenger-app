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

    $("select").selectpicker({style: 'btn-hg btn-primary', menuStyle: 'dropdown-inverse'});

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

    $(".form-wrapper form").submit(function(e){

        $(".challenge-search-results").spin();

        $.ajax({
            url: "/challenge/ajax/search",
            data: {
                phrase: $(".challenge-search-phrase").val(),
                category: $(".challenge-category").val()
            },
            method: "get"
        }).done(function(response){
            var challenges = jQuery.parseJSON(response), body = "";

            $.each(challenges, function(i) {

                body += '<tr>' +
                    '<td><a href="#"><img src="/assets/images/facebook.png"/>' + challenges[i].creator.username + '</a></td>' +
                    '<td>' + challenges[i].challengeName + '</td><td>category1</td><td>time left</td>' +
                    '<td><div class="switch switch-square"><input type="checkbox" unchecked data-toggle="switch" /></div></td></tr>';
            });
            $(".challenge-search-results table tbody").html(body);
            $(".switch").bootstrapSwitch();
            $(".challenge-search-results").spin(false);
        })
        e.preventDefault();
    });

});