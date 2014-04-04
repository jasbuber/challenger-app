/**
 * Created by Tomash on 04.04.14.
 */
$(document).ready(function(){

    $(".ui-block").each(function(){
        $(this).css("height", $(this).width()/1.6);
        $(this).show();
    });

    $(".ui-block").click(function(){
        $(this).parents("#wrapper").height($(this).parents("#wrapper").height());
        $(this).toggleClass('active');
        $(this).siblings().not(this).toggleClass('hide');
        $(this).fadeOut(1500, function(){
            $(this).removeClass("active");
            $("#challenge-block").removeClass('hide');
            $("#challenge-block").fadeIn(1000);

        });
    });

    $("select").selectpicker({style: 'btn-hg btn-primary', menuStyle: 'dropdown-inverse'});

    $(".backAction").click(function(){
        $("#challenge-block").hide();
        $(".ui-block").removeClass('hide');
        $(".ui-block").fadeIn(1000);
    });
});